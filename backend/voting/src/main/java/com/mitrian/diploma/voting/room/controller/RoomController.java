package com.mitrian.diploma.voting.room.controller;

import com.mitrian.diploma.voting.room.dto.ActiveRoomResponseDTO;
import com.mitrian.diploma.voting.room.dto.CreateRoomRequestDTO;
import com.mitrian.diploma.voting.room.dto.CreateRoomResponseDTO;
import com.mitrian.diploma.voting.room.dto.JoinRoomParticipantRequestDTO;
import com.mitrian.diploma.voting.room.dto.JoinRoomRequestDTO;
import com.mitrian.diploma.voting.room.dto.JoinRoomResponseDTO;
import com.mitrian.diploma.voting.room.dto.RoomDetailsResponseDTO;
import com.mitrian.diploma.voting.room.dto.RoomWinnerResponseDTO;
import com.mitrian.diploma.voting.room.dto.SetRoomReadyRequestDTO;
import com.mitrian.diploma.voting.room.dto.SetRoomStateRequestDTO;
import com.mitrian.diploma.voting.room.entity.RoomState;
import com.mitrian.diploma.voting.room.filter.service.RoomFilterService;
import com.mitrian.diploma.voting.room.exception.RoomStartNotAllowedException;
import com.mitrian.diploma.voting.room.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

	private final RoomService roomService;
	private final RoomFilterService roomFilterService;

	public RoomController(RoomService roomService, RoomFilterService roomFilterService) {
		this.roomService = roomService;
		this.roomFilterService = roomFilterService;
	}

	@GetMapping("/me/active")
	public ActiveRoomResponseDTO getMyActiveRoom(Authentication authentication) {
		return roomService.getMyActiveRoom(authentication.getName());
	}

	@GetMapping("/{code}")
	public RoomDetailsResponseDTO getRoom(
		@PathVariable("code") String roomCode,
		Authentication authentication
	) {
		return roomService.getRoomDetails(roomCode, authentication.getName());
	}

	@GetMapping("/{code}/result")
	public RoomWinnerResponseDTO getWinner(
		@PathVariable("code") String roomCode,
		Authentication authentication
	) {
		return roomService.getRoomWinner(roomCode, authentication.getName());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CreateRoomResponseDTO createRoom(
		@Valid @RequestBody CreateRoomRequestDTO request,
		Authentication authentication
	) {
		return roomService.createRoom(request, authentication.getName());
	}

	@PostMapping("/{code}/participants")
	public JoinRoomResponseDTO joinRoom(
		@PathVariable("code") String roomCode,
		@Valid @RequestBody JoinRoomParticipantRequestDTO request,
		Authentication authentication
	) {
		return roomService.joinRoom(new JoinRoomRequestDTO(roomCode, request.roomPassword()), authentication.getName());
	}

	@DeleteMapping("/{code}/participants/me")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void leaveRoom(
		@PathVariable("code") String roomCode,
		Authentication authentication
	) {
		roomService.leaveRoom(roomCode, authentication.getName());
	}

	@PatchMapping("/{code}/participants/me")
	public RoomDetailsResponseDTO setReady(
		@PathVariable("code") String roomCode,
		@Valid @RequestBody SetRoomReadyRequestDTO request,
		Authentication authentication
	) {
		return roomService.setParticipantReady(roomCode, authentication.getName(), request);
	}

	@PatchMapping("/{code}")
	public RoomDetailsResponseDTO startSession(
		@PathVariable("code") String roomCode,
		@Valid @RequestBody SetRoomStateRequestDTO request,
		Authentication authentication
	) {
		if (request.state() == RoomState.AWAITING_START) {
			return roomFilterService.confirmRoomGeoFilter(roomCode, authentication.getName());
		}
		if (request.state() == RoomState.STAGE_ONE) {
			return roomService.startSession(roomCode, authentication.getName());
		}
		throw new RoomStartNotAllowedException("Only transitions to AWAITING_START or STAGE_ONE are supported");
	}
}
