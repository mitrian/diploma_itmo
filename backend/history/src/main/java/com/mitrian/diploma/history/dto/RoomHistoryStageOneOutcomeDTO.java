package com.mitrian.diploma.history.dto;

public record RoomHistoryStageOneOutcomeDTO(
	int participantCount,
	int baseQuorum,
	int relaxedQuorum,
	boolean organizerDoubleWeightApplied
) {
}
