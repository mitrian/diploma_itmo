package com.mitrian.diploma.history.dto;

import java.time.LocalDateTime;

public record RoomHistoryOverviewDTO(
	String roomCode,
	HistoryRoomState roomState,
	LocalDateTime createdAt,
	LocalDateTime finishedAt,
	LocalDateTime stageOneStartedAt,
	Long votingDurationSeconds,
	String ownerDisplayName,
	int participantCount,
	Long chosenRestaurantId,
	String winnerRestaurantName,
	WinnerSelectionPrinciple winnerPrinciple,
	boolean organizerDoubleWeightApplied
) {
}
