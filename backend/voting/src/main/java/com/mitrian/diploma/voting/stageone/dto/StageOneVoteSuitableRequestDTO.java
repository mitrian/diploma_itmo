package com.mitrian.diploma.voting.stageone.dto;

import jakarta.validation.constraints.NotNull;

public record StageOneVoteSuitableRequestDTO(
	@NotNull(message = "Suitable flag is required")
	Boolean suitable
) {
}
