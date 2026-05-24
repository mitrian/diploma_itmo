package com.mitrian.diploma.history.dto.row;

import java.time.LocalDateTime;

public record FinishedRoomSummaryRow(
	String roomCode,
	LocalDateTime createdAt,
	LocalDateTime finishedAt,
	LocalDateTime stageOneStartedAt,
	Long chosenRestaurantId,
	String winnerRestaurantName,
	int participantCount,
	boolean viewerWasOwner
) {
}
