package com.mitrian.diploma.voting.stageone.controller;

import com.mitrian.diploma.voting.stageone.dto.StageOneCurrentResponseDTO;
import com.mitrian.diploma.voting.stageone.dto.StageOneStatusResponseDTO;
import com.mitrian.diploma.voting.stageone.dto.StageOneUpcomingResponseDTO;
import com.mitrian.diploma.voting.stageone.dto.StageOneVoteSuitableRequestDTO;
import com.mitrian.diploma.voting.stageone.service.StageOneQueryService;
import com.mitrian.diploma.voting.stageone.service.StageOneService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
public class StageOneController {

	private final StageOneService stageOneService;
	private final StageOneQueryService stageOneQueryService;

	public StageOneController(StageOneService stageOneService, StageOneQueryService stageOneQueryService) {
		this.stageOneService = stageOneService;
		this.stageOneQueryService = stageOneQueryService;
	}

	@GetMapping("/{code}/stage-one/current")
	public StageOneCurrentResponseDTO getCurrent(
		@PathVariable("code") String roomCode,
		Authentication authentication
	) {
		return stageOneQueryService.getCurrent(roomCode, authentication.getName());
	}

	@GetMapping("/{code}/stage-one/status")
	public StageOneStatusResponseDTO getStatus(
		@PathVariable("code") String roomCode,
		Authentication authentication
	) {
		return stageOneQueryService.getStatus(roomCode, authentication.getName());
	}

	@GetMapping("/{code}/stage-one/upcoming")
	public StageOneUpcomingResponseDTO getUpcoming(
		@PathVariable("code") String roomCode,
		@RequestParam(name = "limit", defaultValue = "10") int limit,
		Authentication authentication
	) {
		return stageOneQueryService.getUpcoming(roomCode, authentication.getName(), limit);
	}

	@PutMapping("/{code}/stage-one/votes/me/{restaurantId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void vote(
		@PathVariable("code") String roomCode,
		@PathVariable("restaurantId") long restaurantId,
		@Valid @RequestBody StageOneVoteSuitableRequestDTO request,
		Authentication authentication
	) {
		stageOneService.vote(roomCode, authentication.getName(), restaurantId, request);
	}
}
