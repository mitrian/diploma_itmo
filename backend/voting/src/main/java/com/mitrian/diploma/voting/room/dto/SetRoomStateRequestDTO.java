package com.mitrian.diploma.voting.room.dto;

import com.mitrian.diploma.voting.room.entity.RoomState;
import jakarta.validation.constraints.NotNull;

public record SetRoomStateRequestDTO(
	@NotNull(message = "State is required")
	RoomState state
) {
}
