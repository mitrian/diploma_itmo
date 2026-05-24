package com.mitrian.diploma.voting.stagetwo.controller;

import com.mitrian.diploma.voting.stagetwo.dto.StageTwoStatusResponseDTO;
import com.mitrian.diploma.voting.stagetwo.dto.StageTwoSubmitRanksRequestDTO;
import com.mitrian.diploma.voting.stagetwo.service.StageTwoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
public class StageTwoController {

	private final StageTwoService stageTwoService;

	public StageTwoController(StageTwoService stageTwoService) {
		this.stageTwoService = stageTwoService;
	}

	@GetMapping("/{code}/stage-two/status")
	public StageTwoStatusResponseDTO getStatus(
		@PathVariable("code") String roomCode,
		Authentication authentication
	) {
		return stageTwoService.getStatus(roomCode, authentication.getName());
	}

	@PostMapping("/{code}/stage-two/ranks")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void submitRanks(
		@PathVariable("code") String roomCode,
		@Valid @RequestBody StageTwoSubmitRanksRequestDTO request,
		Authentication authentication
	) {
		stageTwoService.submitRanks(roomCode, authentication.getName(), request);
	}
}
