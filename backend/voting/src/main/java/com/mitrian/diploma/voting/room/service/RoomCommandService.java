package com.mitrian.diploma.voting.room.service;

import com.mitrian.diploma.voting.room.dto.CreateRoomRequestDTO;
import com.mitrian.diploma.voting.room.dto.CreateRoomResponseDTO;
import com.mitrian.diploma.voting.room.dto.JoinRoomRequestDTO;
import com.mitrian.diploma.voting.room.dto.JoinRoomResponseDTO;
import com.mitrian.diploma.voting.room.dto.RoomDetailsResponseDTO;
import com.mitrian.diploma.voting.room.dto.SetRoomReadyRequestDTO;

public interface RoomCommandService {

	CreateRoomResponseDTO createRoom(CreateRoomRequestDTO request, String ownerLogin);

	JoinRoomResponseDTO joinRoom(JoinRoomRequestDTO request, String userLogin);

	void leaveRoom(String roomCode, String userLogin);

	RoomDetailsResponseDTO setParticipantReady(String roomCode, String userLogin, SetRoomReadyRequestDTO request);

	RoomDetailsResponseDTO startSession(String roomCode, String userLogin);
}
