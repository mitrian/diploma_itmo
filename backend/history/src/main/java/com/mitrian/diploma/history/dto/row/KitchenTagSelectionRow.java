package com.mitrian.diploma.history.dto.row;

public record KitchenTagSelectionRow(
	Long userId,
	String userLogin,
	String userDisplayName,
	Long kitchenTagId,
	String slug,
	String labelRu
) {
}
