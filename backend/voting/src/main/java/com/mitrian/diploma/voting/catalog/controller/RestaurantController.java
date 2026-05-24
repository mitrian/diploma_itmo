package com.mitrian.diploma.voting.catalog.controller;

import com.mitrian.diploma.voting.catalog.dto.RestaurantPageDTO;
import com.mitrian.diploma.voting.catalog.service.RestaurantCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/restaurants")
public class RestaurantController {

	private final RestaurantCatalogService restaurantCatalogService;

	public RestaurantController(RestaurantCatalogService restaurantCatalogService) {
		this.restaurantCatalogService = restaurantCatalogService;
	}

	@GetMapping
	public RestaurantPageDTO list(
		@RequestParam(name = "page", defaultValue = "0") int page,
		@RequestParam(name = "size", defaultValue = "20") int size
	) {
		return restaurantCatalogService.listRestaurants(page, size);
	}
}
