package com.mitrian.diploma.voting.stagetwo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StageTwoRankEntryDTO(
	@NotNull(message = "restaurantId is required")
	Long restaurantId,

	@NotNull(message = "rank is required")
	@Min(value = 1, message = "rank must be at least 1")
	Integer rank
) {
}
