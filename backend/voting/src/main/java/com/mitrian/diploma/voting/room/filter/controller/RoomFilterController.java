package com.mitrian.diploma.voting.room.filter.controller;

import com.mitrian.diploma.voting.room.dto.AddRoomKitchenTagsRequestDTO;
import com.mitrian.diploma.voting.room.dto.RoomDetailsResponseDTO;
import com.mitrian.diploma.voting.room.dto.SetRoomGeoFilterRequestDTO;
import com.mitrian.diploma.voting.room.filter.service.RoomFilterService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomFilterController {

	private final RoomFilterService roomFilterService;

	public RoomFilterController(RoomFilterService roomFilterService) {
		this.roomFilterService = roomFilterService;
	}

	@PutMapping("/{code}/geo-filter")
	public RoomDetailsResponseDTO setGeoFilter(
		@PathVariable("code") String roomCode,
		@Valid @RequestBody SetRoomGeoFilterRequestDTO request,
		Authentication authentication
	) {
		return roomFilterService.setRoomGeoFilter(roomCode, authentication.getName(), request);
	}

	@PostMapping("/{code}/kitchen-tags")
	public RoomDetailsResponseDTO addKitchenTags(
		@PathVariable("code") String roomCode,
		@Valid @RequestBody AddRoomKitchenTagsRequestDTO request,
		Authentication authentication
	) {
		return roomFilterService.addRoomKitchenTags(roomCode, authentication.getName(), request);
	}

	@DeleteMapping("/{code}/kitchen-tags/{slug}")
	public RoomDetailsResponseDTO removeKitchenTag(
		@PathVariable("code") String roomCode,
		@PathVariable("slug") String tagSlug,
		Authentication authentication
	) {
		return roomFilterService.removeRoomKitchenTag(roomCode, authentication.getName(), tagSlug);
	}

	@PostMapping("/{code}/kitchen-filters/lock")
	public RoomDetailsResponseDTO confirmKitchenFilters(
		@PathVariable("code") String roomCode,
		Authentication authentication
	) {
		return roomFilterService.confirmRoomKitchenFilters(roomCode, authentication.getName());
	}
}
