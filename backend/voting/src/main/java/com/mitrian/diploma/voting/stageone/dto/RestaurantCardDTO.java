package com.mitrian.diploma.voting.stageone.dto;

import com.mitrian.diploma.voting.room.filter.dto.KitchenTagDTO;
import java.util.List;

public record RestaurantCardDTO(
	Long id,
	String name,
	String address,
	String openingHours,
	String phone,
	String websiteUrl,
	List<KitchenTagDTO> kitchenTags
) {
}
