package com.mitrian.diploma.kudago.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KudagoPlaceListItem(
	@JsonProperty("id") long id,
	@JsonProperty("title") String title,
	@JsonProperty("address") String address,
	@JsonProperty("coords") KudagoCoords coords,
	@JsonProperty("tags") List<String> tags,
	@JsonProperty("is_closed") boolean closed,
	@JsonProperty("site_url") String siteUrl
) {
}
