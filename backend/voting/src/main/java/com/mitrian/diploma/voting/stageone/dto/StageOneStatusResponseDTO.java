package com.mitrian.diploma.voting.stageone.dto;

import com.mitrian.diploma.voting.room.entity.RoomState;
import java.time.LocalDateTime;
import java.util.List;

public record StageOneStatusResponseDTO(
	RoomState roomState,
	boolean stageOneActive,
	long participantCount,
	int baseQuorum,
	int relaxedQuorum,
	LocalDateTime timeoutAt,
	List<StageOneFinalistRowDTO> finalists
) {
}
