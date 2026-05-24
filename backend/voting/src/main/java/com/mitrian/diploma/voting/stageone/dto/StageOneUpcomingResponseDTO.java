package com.mitrian.diploma.voting.stageone.dto;

import java.util.List;

public record StageOneUpcomingResponseDTO(
	boolean completed,
	List<RestaurantCardDTO> cards
) {
}
