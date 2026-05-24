package com.mitrian.diploma.history.dto.row;

public record RestaurantBaseRow(
	Long restaurantId,
	String name,
	String address,
	String openingHours,
	String phone,
	String websiteUrl
) {
}
