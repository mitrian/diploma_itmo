package com.mitrian.diploma.voting.catalog.service;

import com.mitrian.diploma.voting.catalog.dto.RestaurantPageDTO;

public interface RestaurantCatalogService {

	RestaurantPageDTO listRestaurants(int page, int size);
}
