package com.mitrian.diploma.voting.room.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SetRoomGeoFilterRequestDTO(
	@NotNull(message = "Center latitude is required")
	@DecimalMin(value = "-90.0", message = "Center latitude must be between -90 and 90")
	@DecimalMax(value = "90.0", message = "Center latitude must be between -90 and 90")
	Double centerLat,

	@NotNull(message = "Center longitude is required")
	@DecimalMin(value = "-180.0", message = "Center longitude must be between -180 and 180")
	@DecimalMax(value = "180.0", message = "Center longitude must be between -180 and 180")
	Double centerLon,

	@NotNull(message = "Max distance is required")
	@Positive(message = "Max distance must be greater than zero")
	Integer maxDistanceMeters
) {
}
