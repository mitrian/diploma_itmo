package com.mitrian.diploma.voting.room.service.impl;

import com.mitrian.diploma.auth.entity.User;
import com.mitrian.diploma.voting.room.filter.exception.InvalidKitchenTagSlugException;
import com.mitrian.diploma.voting.room.filter.repository.RoomKitchenTagSelectionRepository;
import com.mitrian.diploma.voting.room.dto.ActiveRoomResponseDTO;
import com.mitrian.diploma.voting.room.dto.AddRoomKitchenTagsRequestDTO;
import com.mitrian.diploma.voting.room.dto.CreateRoomRequestDTO;
import com.mitrian.diploma.voting.room.dto.CreateRoomResponseDTO;
import com.mitrian.diploma.voting.room.dto.JoinRoomRequestDTO;
import com.mitrian.diploma.voting.room.dto.JoinRoomResponseDTO;
import com.mitrian.diploma.voting.room.dto.RoomDetailsResponseDTO;
import com.mitrian.diploma.voting.room.dto.RoomWinnerResponseDTO;
import com.mitrian.diploma.voting.room.dto.SetRoomGeoFilterRequestDTO;
import com.mitrian.diploma.voting.room.dto.SetRoomReadyRequestDTO;
import com.mitrian.diploma.voting.room.entity.Room;
import com.mitrian.diploma.voting.room.exception.RoomGeoFilterNotAllowedException;
import com.mitrian.diploma.voting.room.exception.RoomKitchenTagsNotAllowedException;
import com.mitrian.diploma.voting.room.exception.RoomNotFoundException;
import com.mitrian.diploma.voting.room.repository.RoomParticipantRepository;
import com.mitrian.diploma.voting.room.repository.RoomRepository;
import com.mitrian.diploma.voting.room.service.RoomCommandService;
import com.mitrian.diploma.voting.room.service.RoomQueryService;
import com.mitrian.diploma.voting.room.service.RoomService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomServiceImpl implements RoomService {

	private final RoomRepository roomRepository;
	private final RoomParticipantRepository roomParticipantRepository;
	private final RoomKitchenTagSelectionRepository roomKitchenTagSelectionRepository;
	private final RoomCommandService roomCommandService;
	private final RoomQueryService roomQueryService;

	public RoomServiceImpl(
		RoomRepository roomRepository,
		RoomParticipantRepository roomParticipantRepository,
		RoomKitchenTagSelectionRepository roomKitchenTagSelectionRepository,
		RoomCommandService roomCommandService,
		RoomQueryService roomQueryService
	) {
		this.roomRepository = roomRepository;
		this.roomParticipantRepository = roomParticipantRepository;
		this.roomKitchenTagSelectionRepository = roomKitchenTagSelectionRepository;
		this.roomCommandService = roomCommandService;
		this.roomQueryService = roomQueryService;
	}

	@Override
	@Transactional
	public CreateRoomResponseDTO createRoom(CreateRoomRequestDTO request, String ownerLogin) {
		return roomCommandService.createRoom(request, ownerLogin);
	}

	@Override
	@Transactional
	public JoinRoomResponseDTO joinRoom(JoinRoomRequestDTO request, String userLogin) {
		return roomCommandService.joinRoom(request, userLogin);
	}

	@Override
	@Transactional
	public void leaveRoom(String roomCode, String userLogin) {
		roomCommandService.leaveRoom(roomCode, userLogin);
	}

	@Override
	@Transactional(readOnly = true)
	public RoomDetailsResponseDTO getRoomDetails(String roomCode, String userLogin) {
		return roomQueryService.getRoomDetails(roomCode, userLogin);
	}

	@Override
	@Transactional(readOnly = true)
	public RoomWinnerResponseDTO getRoomWinner(String roomCode, String userLogin) {
		return roomQueryService.getRoomWinner(roomCode, userLogin);
	}

	@Override
	@Transactional
	public RoomDetailsResponseDTO setParticipantReady(String roomCode, String userLogin, SetRoomReadyRequestDTO request) {
		return roomCommandService.setParticipantReady(roomCode, userLogin, request);
	}

	@Override
	@Transactional
	public RoomDetailsResponseDTO startSession(String roomCode, String userLogin) {
		return roomCommandService.startSession(roomCode, userLogin);
	}

	@Override
	@Transactional(readOnly = true)
	public ActiveRoomResponseDTO getMyActiveRoom(String userLogin) {
		return roomQueryService.getMyActiveRoom(userLogin);
	}

}
