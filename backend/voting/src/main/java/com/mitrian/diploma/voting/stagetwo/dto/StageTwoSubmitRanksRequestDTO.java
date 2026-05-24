package com.mitrian.diploma.voting.stagetwo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record StageTwoSubmitRanksRequestDTO(
	@NotEmpty(message = "ranks must not be empty")
	@Valid
	List<StageTwoRankEntryDTO> ranks
) {
}
