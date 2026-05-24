package com.mitrian.diploma.voting.room.service;

import com.mitrian.diploma.voting.room.dto.ActiveRoomResponseDTO;
import com.mitrian.diploma.voting.room.dto.RoomDetailsResponseDTO;
import com.mitrian.diploma.voting.room.dto.RoomWinnerResponseDTO;

public interface RoomQueryService {

	RoomDetailsResponseDTO getRoomDetails(String roomCode, String userLogin);

	RoomWinnerResponseDTO getRoomWinner(String roomCode, String userLogin);

	ActiveRoomResponseDTO getMyActiveRoom(String userLogin);
}
