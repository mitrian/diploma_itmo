package com.mitrian.diploma.voting.catalog.mapper;

import com.mitrian.diploma.voting.room.filter.dto.KitchenTagDTO;
import com.mitrian.diploma.voting.room.filter.entity.KitchenTag;
import com.mitrian.diploma.voting.room.filter.repository.KitchenTagRepository;
import com.mitrian.diploma.voting.room.mapper.RoomDetailsMapper;
import com.mitrian.diploma.voting.catalog.entity.Restaurant;
import com.mitrian.diploma.voting.catalog.entity.RestaurantKitchenTag;
import com.mitrian.diploma.voting.catalog.repository.RestaurantKitchenTagRepository;
import com.mitrian.diploma.voting.catalog.repository.RestaurantRepository;
import com.mitrian.diploma.voting.stageone.dto.RestaurantCardDTO;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RestaurantCardMapper {

	private final RestaurantRepository restaurantRepository;
	private final RestaurantKitchenTagRepository restaurantKitchenTagRepository;
	private final KitchenTagRepository kitchenTagRepository;
	private final RoomDetailsMapper roomDetailsMapper;

	public RestaurantCardMapper(
		RestaurantRepository restaurantRepository,
		RestaurantKitchenTagRepository restaurantKitchenTagRepository,
		KitchenTagRepository kitchenTagRepository,
		RoomDetailsMapper roomDetailsMapper
	) {
		this.restaurantRepository = restaurantRepository;
		this.restaurantKitchenTagRepository = restaurantKitchenTagRepository;
		this.kitchenTagRepository = kitchenTagRepository;
		this.roomDetailsMapper = roomDetailsMapper;
	}

	public RestaurantCardDTO toCardByRestaurantId(Long restaurantId) {
		if (restaurantId == null) {
			return null;
		}
		Restaurant restaurant = restaurantRepository.findById(restaurantId).orElse(null);
		if (restaurant == null) {
			return null;
		}
		return toCard(restaurant);
	}

	public RestaurantCardDTO toCard(Restaurant restaurant) {
		List<RestaurantKitchenTag> links = restaurantKitchenTagRepository.findByRestaurantIdOrderByKitchenTagIdAsc(
			restaurant.getId()
		);
		List<Long> tagIds = links.stream().map(RestaurantKitchenTag::getKitchenTagId).toList();
		Map<Long, KitchenTag> tagsById = kitchenTagRepository.findAllById(tagIds).stream()
			.collect(Collectors.toMap(KitchenTag::getId, Function.identity()));

		List<KitchenTagDTO> kitchenTags = tagIds.stream()
			.map(tagsById::get)
			.filter(Objects::nonNull)
			.map(roomDetailsMapper::toKitchenTagDto)
			.toList();

		return new RestaurantCardDTO(
			restaurant.getId(),
			restaurant.getName(),
			restaurant.getAddress(),
			restaurant.getOpeningHours(),
			restaurant.getPhone(),
			restaurant.getWebsiteUrl(),
			kitchenTags
		);
	}
}
