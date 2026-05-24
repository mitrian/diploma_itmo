package com.mitrian.diploma.voting.stagetwo.dto;

public record StageTwoFinalistRowDTO(
	long restaurantId,
	int position,
	int approvalCount,
	String includedBy
) {
}
