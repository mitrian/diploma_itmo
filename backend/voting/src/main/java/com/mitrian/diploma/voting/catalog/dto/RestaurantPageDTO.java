package com.mitrian.diploma.voting.catalog.dto;

import java.util.List;

public record RestaurantPageDTO(
	long totalElements,
	int totalPages,
	int page,
	int size,
	List<RestaurantSummaryDTO> content
) {
}
