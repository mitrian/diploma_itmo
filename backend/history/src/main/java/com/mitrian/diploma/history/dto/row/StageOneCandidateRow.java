package com.mitrian.diploma.history.dto.row;

public record StageOneCandidateRow(
	Long restaurantId,
	int sortOrder,
	String restaurantName,
	String restaurantAddress,
	String openingHours,
	String phone,
	String websiteUrl
) {
}
