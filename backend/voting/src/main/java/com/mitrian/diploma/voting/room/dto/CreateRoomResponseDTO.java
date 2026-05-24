package com.mitrian.diploma.voting.room.dto;

import com.mitrian.diploma.voting.room.entity.RoomState;
import java.time.LocalDateTime;

public record CreateRoomResponseDTO(
	Long id,
	String code,
	RoomState state,
	LocalDateTime createdAt
) {
}
