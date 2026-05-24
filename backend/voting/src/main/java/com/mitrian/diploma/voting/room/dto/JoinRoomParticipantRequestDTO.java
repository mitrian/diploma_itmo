package com.mitrian.diploma.voting.room.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinRoomParticipantRequestDTO(
	@NotBlank(message = "Room password is required")
	@Size(min = 4, max = 255, message = "Room password length must be between 4 and 255")
	String roomPassword
) {
}
