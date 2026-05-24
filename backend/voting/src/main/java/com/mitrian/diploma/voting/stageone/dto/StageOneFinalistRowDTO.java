package com.mitrian.diploma.voting.stageone.dto;

public record StageOneFinalistRowDTO(
	int position,
	long restaurantId,
	RestaurantCardDTO card,
	int approvalCount,
	String includedBy
) {
}
