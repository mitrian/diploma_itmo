package com.mitrian.diploma.auth.service.impl;

import com.mitrian.diploma.auth.dto.auth.AuthResponseDTO;
import com.mitrian.diploma.auth.dto.auth.LoginRequestDTO;
import com.mitrian.diploma.auth.dto.auth.RegisterRequestDTO;
import com.mitrian.diploma.auth.entity.User;
import com.mitrian.diploma.auth.exception.InvalidCredentialsException;
import com.mitrian.diploma.auth.exception.LoginAlreadyExistsException;
import com.mitrian.diploma.auth.exception.UserNotFoundException;
import com.mitrian.diploma.auth.repository.UserRepository;
import com.mitrian.diploma.auth.security.JwtService;
import com.mitrian.diploma.auth.service.AuthService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	public AuthServiceImpl(
		UserRepository userRepository,
		PasswordEncoder passwordEncoder,
		AuthenticationManager authenticationManager,
		JwtService jwtService
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
	}

	@Override
	@Transactional
	public AuthResponseDTO register(RegisterRequestDTO request) {
		if (userRepository.existsByLogin(request.login())) {
			throw new LoginAlreadyExistsException("Login already exists");
		}

		User user = new User();
		user.setLogin(request.login());
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setDisplayName(request.displayName());
		User saved;
		try {
			saved = userRepository.save(user);
		} catch (DataIntegrityViolationException ex) {
			// Concurrent registration can bypass existsByLogin check and fail on DB unique constraint.
			if (userRepository.existsByLogin(request.login())) {
				throw new LoginAlreadyExistsException("Login already exists");
			}
			throw ex;
		}

		String token = jwtService.generateToken(saved.getLogin());
		return new AuthResponseDTO(
			token,
			saved.getDisplayName()
		);
	}

	@Override
	public AuthResponseDTO login(LoginRequestDTO request) {
		try {
			authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.login(), request.password())
			);
		} catch (AuthenticationException ex) {
			throw new InvalidCredentialsException("Invalid credentials");
		}

		User user = userRepository.findByLogin(request.login())
			.orElseThrow(() -> new UserNotFoundException("User not found"));

		String token = jwtService.generateToken(user.getLogin());
		return new AuthResponseDTO(
			token,
			user.getDisplayName()
		);
	}
}
