package com.mitrian.diploma.voting.room.dto;

import com.mitrian.diploma.voting.room.entity.RoomState;
import com.mitrian.diploma.voting.stageone.dto.RestaurantCardDTO;

public record RoomWinnerResponseDTO(
	RoomState roomState,
	Long chosenRestaurantId,
	RestaurantCardDTO winnerRestaurant
) {
}
