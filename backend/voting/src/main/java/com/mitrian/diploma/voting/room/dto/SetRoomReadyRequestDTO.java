package com.mitrian.diploma.voting.room.dto;

import jakarta.validation.constraints.NotNull;

public record SetRoomReadyRequestDTO(
	@NotNull(message = "Ready flag is required")
	Boolean ready
) {
}
