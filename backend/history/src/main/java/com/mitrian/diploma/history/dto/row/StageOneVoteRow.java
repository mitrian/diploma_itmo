package com.mitrian.diploma.history.dto.row;

import java.time.LocalDateTime;

public record StageOneVoteRow(
	Long userId,
	String userLogin,
	String userDisplayName,
	Long restaurantId,
	boolean suitable,
	LocalDateTime createdAt
) {
}
