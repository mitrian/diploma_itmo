package com.mitrian.diploma.voting.room.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinRoomRequestDTO(
	@NotBlank(message = "Room code is required")
	@Size(min = 1, max = 32, message = "Room code length must be between 1 and 32")
	String code,

	@NotBlank(message = "Room password is required")
	@Size(min = 4, max = 255, message = "Room password length must be between 4 and 255")
	String roomPassword
) {
}
