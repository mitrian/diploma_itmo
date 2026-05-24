package com.mitrian.diploma.history.dto;

import java.util.List;

public record RoomHistoryStageOneSectionDTO(
	RoomHistoryStageOneOutcomeDTO outcome,
	List<RoomHistoryStageOneRowDTO> restaurants
) {
}
