package com.mitrian.diploma.history.dto.row;

public record RestaurantKitchenTagRow(
	Long restaurantId,
	Long kitchenTagId,
	String slug,
	String labelRu
) {
}
