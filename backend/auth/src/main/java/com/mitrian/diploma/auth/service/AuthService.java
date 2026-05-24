package com.mitrian.diploma.auth.service;

import com.mitrian.diploma.auth.dto.auth.AuthResponseDTO;
import com.mitrian.diploma.auth.dto.auth.LoginRequestDTO;
import com.mitrian.diploma.auth.dto.auth.RegisterRequestDTO;

public interface AuthService {

	AuthResponseDTO register(RegisterRequestDTO request);

	AuthResponseDTO login(LoginRequestDTO request);
}
