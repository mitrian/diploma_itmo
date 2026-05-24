package com.mitrian.diploma.history.dto;

import java.util.List;

public record RoomHistoryStageOneRowDTO(
	long restaurantId,
	String name,
	String address,
	String openingHours,
	String phone,
	String websiteUrl,
	List<RoomHistoryRestaurantKitchenTagDTO> kitchenTags,
	int sortOrder,
	int totalSuitable,
	int totalUnsuitable,
	String includedBy
) {
}
