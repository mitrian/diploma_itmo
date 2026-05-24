package com.mitrian.diploma.auth.controller;

import com.mitrian.diploma.auth.dto.user.UserMeResponseDTO;
import com.mitrian.diploma.auth.exception.UserNotFoundException;
import com.mitrian.diploma.auth.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

	private final UserRepository userRepository;

	public UserController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@GetMapping("/me")
	public UserMeResponseDTO me(Authentication authentication) {
		String login = authentication.getName();
		var user = userRepository.findByLogin(login)
			.orElseThrow(() -> new UserNotFoundException("User not found"));

		return new UserMeResponseDTO(user.getId(), user.getLogin(), user.getDisplayName());
	}
}
