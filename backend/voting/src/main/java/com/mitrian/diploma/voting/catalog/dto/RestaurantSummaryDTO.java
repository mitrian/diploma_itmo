package com.mitrian.diploma.voting.catalog.dto;

public record RestaurantSummaryDTO(
	Long id,
	String name,
	String address,
	String phone,
	Double latitude,
	Double longitude
) {
}
