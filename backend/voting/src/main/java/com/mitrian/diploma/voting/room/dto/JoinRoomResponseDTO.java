package com.mitrian.diploma.voting.room.dto;

import com.mitrian.diploma.voting.room.entity.RoomState;

public record JoinRoomResponseDTO(
	Long roomId,
	String code,
	RoomState state
) {
}
