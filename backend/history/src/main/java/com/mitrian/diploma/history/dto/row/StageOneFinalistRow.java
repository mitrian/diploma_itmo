package com.mitrian.diploma.history.dto.row;

public record StageOneFinalistRow(
	Long restaurantId,
	int approvalCount,
	int position,
	String includedBy
) {
}
