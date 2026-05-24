package com.mitrian.diploma.kudago.service;

import com.mitrian.diploma.kudago.config.KudagoImportProperties;
import com.mitrian.diploma.kudago.client.KudagoPlacesClient;
import com.mitrian.diploma.kudago.dto.KudagoPlaceDetailDto;
import com.mitrian.diploma.kudago.dto.KudagoPlaceListItem;
import com.mitrian.diploma.kudago.support.KudagoTagToKitchenSlugResolver;
import com.mitrian.diploma.voting.catalog.entity.Restaurant;
import com.mitrian.diploma.voting.catalog.entity.RestaurantKitchenTag;
import com.mitrian.diploma.voting.catalog.repository.RestaurantKitchenTagRepository;
import com.mitrian.diploma.voting.catalog.repository.RestaurantRepository;
import com.mitrian.diploma.voting.catalog.util.Haversine;
import com.mitrian.diploma.voting.room.filter.entity.KitchenTag;
import com.mitrian.diploma.voting.room.filter.repository.KitchenTagRepository;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class KudagoCatalogImportService {

	private static final Logger log = LoggerFactory.getLogger(KudagoCatalogImportService.class);

	private static final String FALLBACK_HOURS = "—";

	private static final String FALLBACK_PHONE = "—";

	private final KudagoImportProperties properties;

	private final KudagoPlacesClient kudagoPlacesClient;

	private final RestaurantRepository restaurantRepository;

	private final RestaurantKitchenTagRepository restaurantKitchenTagRepository;

	private final KitchenTagRepository kitchenTagRepository;

	private final TransactionTemplate transactionTemplate;

	private final Object importRunLock = new Object();

	public KudagoCatalogImportService(
		KudagoImportProperties properties,
		KudagoPlacesClient kudagoPlacesClient,
		RestaurantRepository restaurantRepository,
		RestaurantKitchenTagRepository restaurantKitchenTagRepository,
		KitchenTagRepository kitchenTagRepository,
		PlatformTransactionManager transactionManager
	) {
		this.properties = properties;
		this.kudagoPlacesClient = kudagoPlacesClient;
		this.restaurantRepository = restaurantRepository;
		this.restaurantKitchenTagRepository = restaurantKitchenTagRepository;
		this.kitchenTagRepository = kitchenTagRepository;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	public KudagoCatalogImportStats importRestaurantsWithinRadius() {
		synchronized (importRunLock) {
			return importRestaurantsWithinRadiusLocked();
		}
	}

	private KudagoCatalogImportStats importRestaurantsWithinRadiusLocked() {
		List<KitchenTag> allTags = kitchenTagRepository.findAllByOrderByIdAsc();
		Set<String> allowedSlugs = allTags.stream().map(KitchenTag::getSlug).collect(Collectors.toSet());
		Map<String, KitchenTag> tagsBySlug = allTags.stream()
			.collect(Collectors.toMap(KitchenTag::getSlug, Function.identity()));

		List<KudagoPlaceListItem> apiList;
		try {
			apiList = kudagoPlacesClient.fetchAllPlaceSummariesInApiRadius();
		} catch (Exception ex) {
			log.error("KudaGo list fetch failed: {}", ex.getMessage());
			return new KudagoCatalogImportStats(0, 0, 0, 0, 0, 0, 0, 0);
		}

		int skippedClosed = 0;
		int skippedNoCoords = 0;
		int skippedTooFar = 0;
		int detailFailures = 0;
		int persistFailures = 0;
		int withinRadius = 0;
		int saved = 0;

		double centerLat = properties.getCenterLat();
		double centerLon = properties.getCenterLon();
		int radiusM = properties.getRadiusMeters();

		for (KudagoPlaceListItem item : apiList) {
			if (item.closed()) {
				skippedClosed++;
				continue;
			}
			if (item.coords() == null || item.coords().lat() == null || item.coords().lon() == null) {
				skippedNoCoords++;
				continue;
			}
			double meters = Haversine.distanceMeters(
				centerLat,
				centerLon,
				item.coords().lat(),
				item.coords().lon()
			);
			if (meters > radiusM) {
				skippedTooFar++;
				continue;
			}
			withinRadius++;

			KudagoPlaceDetailDto detail = null;
			try {
				detail = kudagoPlacesClient.fetchPlaceDetail(item.id());
			} catch (Exception ex) {
				detailFailures++;
				log.warn("KudaGo detail for place {}: {}", item.id(), ex.getMessage());
			}

			final KudagoPlaceDetailDto detailForTx = detail;
			try {
				transactionTemplate.executeWithoutResult(status ->
					upsertRestaurant(item, detailForTx, allowedSlugs, tagsBySlug)
				);
				saved++;
			} catch (Exception ex) {
				persistFailures++;
				log.warn("Persist place {}: {}", item.id(), ex.getMessage());
			}
		}

		return new KudagoCatalogImportStats(
			apiList.size(),
			withinRadius,
			saved,
			skippedClosed,
			skippedNoCoords,
			skippedTooFar,
			detailFailures,
			persistFailures
		);
	}

	private void upsertRestaurant(
		KudagoPlaceListItem item,
		KudagoPlaceDetailDto detail,
		Set<String> allowedSlugs,
		Map<String, KitchenTag> tagsBySlug
	) {
		Restaurant entity = restaurantRepository.findByKudagoPlaceId(item.id()).orElseGet(Restaurant::new);
		entity.setKudagoPlaceId(item.id());
		entity.setName(trimToLen(blankToFallback(item.title(), "Заведение " + item.id()), 512));
		entity.setAddress(trimToLen(blankToFallback(item.address(), "Санкт-Петербург"), 1024));
		entity.setOpeningHours(trimToLen(resolveOpeningHours(detail), 256));
		entity.setPhone(trimToLen(resolvePhone(detail), 64));
		entity.setWebsiteUrl(trimToLenNullable(resolveWebsite(detail, item), 512));
		entity.setLatitude(item.coords().lat());
		entity.setLongitude(item.coords().lon());

		Restaurant savedRestaurant = restaurantRepository.save(entity);

		restaurantKitchenTagRepository.deleteByRestaurantId(savedRestaurant.getId());

		Set<Long> kitchenTagIds = new LinkedHashSet<>();
		for (String raw : nullSafeTags(item.tags())) {
			Optional<String> slug = KudagoTagToKitchenSlugResolver.resolve(raw, allowedSlugs);
			slug.map(tagsBySlug::get).map(KitchenTag::getId).ifPresent(kitchenTagIds::add);
		}

		if (!kitchenTagIds.isEmpty()) {
			List<RestaurantKitchenTag> links = new ArrayList<>();
			for (Long kitchenTagId : kitchenTagIds) {
				RestaurantKitchenTag link = new RestaurantKitchenTag();
				link.setRestaurantId(savedRestaurant.getId());
				link.setKitchenTagId(kitchenTagId);
				links.add(link);
			}
			restaurantKitchenTagRepository.saveAll(links);
		}
	}

	private static List<String> nullSafeTags(List<String> tags) {
		return tags == null ? List.of() : tags;
	}

	private static String resolveOpeningHours(KudagoPlaceDetailDto detail) {
		if (detail != null && notBlank(detail.timetable())) {
			return detail.timetable().trim();
		}
		return FALLBACK_HOURS;
	}

	private static String resolvePhone(KudagoPlaceDetailDto detail) {
		if (detail != null && notBlank(detail.phone())) {
			return detail.phone().trim();
		}
		return FALLBACK_PHONE;
	}

	private static String resolveWebsite(KudagoPlaceDetailDto detail, KudagoPlaceListItem item) {
		if (detail != null && notBlank(detail.foreignUrl())) {
			return detail.foreignUrl().trim();
		}
		if (detail != null && notBlank(detail.siteUrl())) {
			return detail.siteUrl().trim();
		}
		if (notBlank(item.siteUrl())) {
			return item.siteUrl().trim();
		}
		return null;
	}

	private static boolean notBlank(String s) {
		return s != null && !s.trim().isEmpty();
	}

	private static String blankToFallback(String value, String fallback) {
		return notBlank(value) ? value.trim() : fallback;
	}

	private static String trimToLen(String s, int max) {
		if (s.length() <= max) {
			return s;
		}
		return s.substring(0, max);
	}

	private static String trimToLenNullable(String s, int max) {
		if (s == null) {
			return null;
		}
		return trimToLen(s, max);
	}

	public record KudagoCatalogImportStats(
		int apiListTotal,
		int withinRadius,
		int savedOrUpdated,
		int skippedClosed,
		int skippedNoCoords,
		int skippedTooFar,
		int detailFetchFailures,
		int persistFailures
	) {
	}
}
