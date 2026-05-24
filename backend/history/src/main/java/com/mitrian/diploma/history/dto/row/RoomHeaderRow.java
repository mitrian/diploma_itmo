package com.mitrian.diploma.history.dto.row;

import java.time.LocalDateTime;

public record RoomHeaderRow(
	Long roomId,
	String code,
	String state,
	Long ownerId,
	String ownerLogin,
	String ownerDisplayName,
	Double centerLat,
	Double centerLon,
	Integer maxDistanceMeters,
	Long chosenRestaurantId,
	String winnerSelectionPrinciple,
	boolean organizerDoubleWeightApplied,
	int participantCount,
	Integer stageOneParticipantCountSnapshot,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	LocalDateTime stageOneStartedAt,
	LocalDateTime finishedAt
) {
}
