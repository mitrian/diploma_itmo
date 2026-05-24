package com.mitrian.diploma.voting.catalog.service.impl;

import com.mitrian.diploma.voting.catalog.dto.RestaurantPageDTO;
import com.mitrian.diploma.voting.catalog.dto.RestaurantSummaryDTO;
import com.mitrian.diploma.voting.catalog.entity.Restaurant;
import com.mitrian.diploma.voting.catalog.repository.RestaurantRepository;
import com.mitrian.diploma.voting.catalog.service.RestaurantCatalogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestaurantCatalogServiceImpl implements RestaurantCatalogService {

	static final int MAX_PAGE_SIZE = 50;

	private final RestaurantRepository restaurantRepository;

	public RestaurantCatalogServiceImpl(RestaurantRepository restaurantRepository) {
		this.restaurantRepository = restaurantRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public RestaurantPageDTO listRestaurants(int page, int size) {
		int clampedSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
		int clampedPage = Math.max(page, 0);
		var limited = PageRequest.of(clampedPage, clampedSize, Sort.by(Sort.Direction.ASC, "id"));

		Page<Restaurant> result = restaurantRepository.findAll(limited);
		return new RestaurantPageDTO(
			result.getTotalElements(),
			result.getTotalPages(),
			result.getNumber(),
			result.getSize(),
			result.getContent().stream().map(this::toSummary).toList()
		);
	}

	private RestaurantSummaryDTO toSummary(Restaurant r) {
		return new RestaurantSummaryDTO(
			r.getId(),
			r.getName(),
			r.getAddress(),
			r.getPhone(),
			r.getLatitude(),
			r.getLongitude()
		);
	}
}
