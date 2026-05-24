package com.mitrian.diploma.kudago.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KudagoCoords(
	@JsonProperty("lat") Double lat,
	@JsonProperty("lon") Double lon
) {
}
