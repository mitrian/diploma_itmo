package com.mitrian.diploma.auth.dto.user;

public record UserMeResponseDTO(
	Long id,
	String login,
	String displayName
) {
}
