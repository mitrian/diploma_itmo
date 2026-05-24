package com.mitrian.diploma.voting.room.filter.service;

import com.mitrian.diploma.voting.room.dto.AddRoomKitchenTagsRequestDTO;
import com.mitrian.diploma.voting.room.dto.RoomDetailsResponseDTO;
import com.mitrian.diploma.voting.room.dto.SetRoomGeoFilterRequestDTO;
import com.mitrian.diploma.voting.room.filter.dto.KitchenTagDTO;
import java.util.List;

public interface RoomFilterService {

	RoomDetailsResponseDTO setRoomGeoFilter(String roomCode, String userLogin, SetRoomGeoFilterRequestDTO request);

	RoomDetailsResponseDTO confirmRoomGeoFilter(String roomCode, String userLogin);

	List<KitchenTagDTO> listKitchenTagsCatalog();

	RoomDetailsResponseDTO addRoomKitchenTags(String roomCode, String userLogin, AddRoomKitchenTagsRequestDTO request);

	RoomDetailsResponseDTO removeRoomKitchenTag(String roomCode, String userLogin, String tagSlug);

	RoomDetailsResponseDTO confirmRoomKitchenFilters(String roomCode, String userLogin);
}
