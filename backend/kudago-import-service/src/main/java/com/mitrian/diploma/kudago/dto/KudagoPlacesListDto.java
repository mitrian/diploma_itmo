package com.mitrian.diploma.kudago.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KudagoPlacesListDto(
	@JsonProperty("count") int count,
	@JsonProperty("next") String next,
	@JsonProperty("results") List<KudagoPlaceListItem> results
) {
}
