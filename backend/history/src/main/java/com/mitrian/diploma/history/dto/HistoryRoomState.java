package com.mitrian.diploma.history.dto;

public enum HistoryRoomState {
	LOBBY,
	GEO_FILTERS,
	AWAITING_START,
	STAGE_ONE,
	STAGE_TWO,
	FINISHED;

	public static HistoryRoomState fromPersisted(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Room state is empty");
		}
		return HistoryRoomState.valueOf(value.trim());
	}
}
