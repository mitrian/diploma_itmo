package com.mitrian.diploma.auth.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
	@NotBlank(message = "Login is required")
	@Size(min = 3, max = 255, message = "Login length must be between 3 and 255")
	String login,

	@NotBlank(message = "Password is required")
	@Size(min = 12, max = 255, message = "Password length must be at least 12")
	String password,

	@NotBlank(message = "Display name is required")
	@Size(min = 1, max = 255, message = "Display name length must be between 1 and 255")
	String displayName
) {
}
