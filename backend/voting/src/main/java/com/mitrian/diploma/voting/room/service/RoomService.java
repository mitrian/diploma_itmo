package com.mitrian.diploma.voting.room.service;

import com.mitrian.diploma.voting.room.dto.CreateRoomRequestDTO;
import com.mitrian.diploma.voting.room.dto.CreateRoomResponseDTO;
import com.mitrian.diploma.voting.room.dto.JoinRoomRequestDTO;
import com.mitrian.diploma.voting.room.dto.JoinRoomResponseDTO;
import com.mitrian.diploma.voting.room.dto.ActiveRoomResponseDTO;
import com.mitrian.diploma.voting.room.dto.RoomDetailsResponseDTO;
import com.mitrian.diploma.voting.room.dto.RoomWinnerResponseDTO;
import com.mitrian.diploma.voting.room.dto.SetRoomReadyRequestDTO;

public interface RoomService {

	CreateRoomResponseDTO createRoom(CreateRoomRequestDTO request, String ownerLogin);

	JoinRoomResponseDTO joinRoom(JoinRoomRequestDTO request, String userLogin);

	void leaveRoom(String roomCode, String userLogin);

	RoomDetailsResponseDTO getRoomDetails(String roomCode, String userLogin);

	RoomWinnerResponseDTO getRoomWinner(String roomCode, String userLogin);

	RoomDetailsResponseDTO setParticipantReady(String roomCode, String userLogin, SetRoomReadyRequestDTO request);

	ActiveRoomResponseDTO getMyActiveRoom(String userLogin);

	RoomDetailsResponseDTO startSession(String roomCode, String userLogin);
}
