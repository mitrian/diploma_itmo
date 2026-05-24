package com.mitrian.diploma.history.dto;

import java.time.LocalDateTime;

public record RoomHistorySummaryDTO(
	String roomCode,
	LocalDateTime createdAt,
	LocalDateTime finishedAt,
	Long votingDurationSeconds,
	Long chosenRestaurantId,
	String winnerRestaurantName,
	int participantCount,
	boolean viewerWasOwner
) {
}
