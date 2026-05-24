package com.mitrian.diploma.voting.stagetwo.dto;

import com.mitrian.diploma.voting.room.entity.RoomState;
import java.time.LocalDateTime;
import java.util.List;

public record StageTwoStatusResponseDTO(
	RoomState roomState,
	Long chosenRestaurantId,
	LocalDateTime timeoutAt,
	List<StageTwoFinalistRowDTO> finalists,
	List<StageTwoMyRankRowDTO> myRanks
) {
}
