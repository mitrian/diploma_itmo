package com.mitrian.diploma.voting.room.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AddRoomKitchenTagsRequestDTO(
	@NotNull
	List<String> slugs
) {
}
