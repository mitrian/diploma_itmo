package com.mitrian.diploma.kudago.client;

import com.mitrian.diploma.kudago.config.KudagoImportProperties;
import com.mitrian.diploma.kudago.dto.KudagoPlaceDetailDto;
import com.mitrian.diploma.kudago.dto.KudagoPlaceListItem;
import com.mitrian.diploma.kudago.dto.KudagoPlacesListDto;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class KudagoPlacesClient {

	private static final String LIST_FIELDS =
		"id,title,address,coords,categories,tags,site_url,is_closed";

	private static final String DETAIL_FIELDS = "id,phone,timetable,foreign_url,site_url";

	private final RestClient restClient;
	private final KudagoImportProperties properties;

	public KudagoPlacesClient(
		@Qualifier("kudagoRestClient") RestClient kudagoRestClient,
		KudagoImportProperties properties
	) {
		this.restClient = kudagoRestClient;
		this.properties = properties;
	}

	public List<KudagoPlaceListItem> fetchAllPlaceSummariesInApiRadius() {
		List<KudagoPlaceListItem> all = new ArrayList<>();
		String url = buildListUrl(1);
		while (url != null) {
			KudagoPlacesListDto page = restClient.get()
				.uri(URI.create(url))
				.retrieve()
				.body(KudagoPlacesListDto.class);
			if (page == null || page.results() == null || page.results().isEmpty()) {
				break;
			}
			all.addAll(page.results());
			url = page.next();
		}
		return all;
	}

	public KudagoPlaceDetailDto fetchPlaceDetail(long placeId) {
		String url = UriComponentsBuilder
			.fromUriString(properties.getBaseUrl() + "/places/" + placeId + "/")
			.queryParam("lang", "ru")
			.queryParam("fields", DETAIL_FIELDS)
			.queryParam("text_format", "text")
			.encode(StandardCharsets.UTF_8)
			.build()
			.toUriString();
		return restClient.get()
			.uri(URI.create(url))
			.retrieve()
			.body(KudagoPlaceDetailDto.class);
	}

	private String buildListUrl(int page) {
		return UriComponentsBuilder
			.fromUriString(properties.getBaseUrl() + "/places/")
			.queryParam("location", properties.getLocation())
			.queryParam("categories", properties.getCategories())
			.queryParam("lat", properties.getCenterLat())
			.queryParam("lon", properties.getCenterLon())
			.queryParam("radius", properties.getRadiusMeters())
			.queryParam("fields", LIST_FIELDS)
			.queryParam("text_format", "text")
			.queryParam("page", page)
			.queryParam("page_size", properties.getListPageSize())
			.encode(StandardCharsets.UTF_8)
			.build()
			.toUriString();
	}
}
