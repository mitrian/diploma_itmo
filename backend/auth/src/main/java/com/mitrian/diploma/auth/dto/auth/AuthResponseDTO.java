package com.mitrian.diploma.auth.dto.auth;

public record AuthResponseDTO(
	String token,
	String displayName
) {
}
