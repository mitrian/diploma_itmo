package com.mitrian.diploma.history.dto;

import java.util.List;

public record RoomHistoryStageTwoRowDTO(
	long restaurantId,
	String name,
	String address,
	String openingHours,
	String phone,
	String websiteUrl,
	List<RoomHistoryRestaurantKitchenTagDTO> kitchenTags,
	int stageOnePosition,
	int stageOneApprovalCount,
	String includedBy,
	int rankSum,
	List<RoomHistoryStageTwoRankDTO> ranks
) {
}
