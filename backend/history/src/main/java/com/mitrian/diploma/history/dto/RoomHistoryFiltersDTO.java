package com.mitrian.diploma.history.dto;

import java.util.List;

public record RoomHistoryFiltersDTO(
	Double centerLat,
	Double centerLon,
	Integer maxDistanceMeters,
	List<RoomHistoryKitchenTagDTO> kitchenTags
) {
}
