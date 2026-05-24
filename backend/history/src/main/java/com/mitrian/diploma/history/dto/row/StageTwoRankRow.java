package com.mitrian.diploma.history.dto.row;

public record StageTwoRankRow(
	Long userId,
	String userLogin,
	String userDisplayName,
	Long restaurantId,
	int rankValue
) {
}
