package com.mitrian.diploma.voting.stageone.dto;

public record StageOneCurrentResponseDTO(
	boolean completed,
	RestaurantCardDTO card
) {
}
