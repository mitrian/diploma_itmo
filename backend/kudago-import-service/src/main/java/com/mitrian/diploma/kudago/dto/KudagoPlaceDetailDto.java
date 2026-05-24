package com.mitrian.diploma.kudago.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KudagoPlaceDetailDto(
	@JsonProperty("id") long id,
	@JsonProperty("phone") String phone,
	@JsonProperty("timetable") String timetable,
	@JsonProperty("foreign_url") String foreignUrl,
	@JsonProperty("site_url") String siteUrl
) {
}
