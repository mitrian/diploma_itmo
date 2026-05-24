package com.mitrian.diploma.history.controller;

import com.mitrian.diploma.history.dto.RoomHistoryFiltersDTO;
import com.mitrian.diploma.history.dto.RoomHistoryOverviewDTO;
import com.mitrian.diploma.history.dto.RoomHistoryParticipantDTO;
import com.mitrian.diploma.history.dto.RoomHistoryStageOneSectionDTO;
import com.mitrian.diploma.history.dto.RoomHistoryStageOneVoteDTO;
import com.mitrian.diploma.history.dto.RoomHistoryStageTwoRowDTO;
import com.mitrian.diploma.history.dto.RoomHistorySummaryDTO;
import com.mitrian.diploma.history.service.HistoryService;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
public class HistoryController {

	private final HistoryService historyService;

	public HistoryController(HistoryService historyService) {
		this.historyService = historyService;
	}

	@GetMapping("/me/history")
	public List<RoomHistorySummaryDTO> listMyFinishedRooms(Authentication authentication) {
		return historyService.listMyFinishedRoomSummaries(authentication.getName());
	}

	@GetMapping("/{code}/history/overview")
	public RoomHistoryOverviewDTO getOverview(
		@PathVariable("code") String roomCode,
		Authentication authentication
	) {
		return historyService.getOverview(roomCode, authentication.getName());
	}

	@GetMapping("/{code}/history/participants")
	public List<RoomHistoryParticipantDTO> getParticipants(
		@PathVariable("code") String roomCode,
		Authentication authentication
	) {
		return historyService.getParticipants(roomCode, authentication.getName());
	}

	@GetMapping("/{code}/history/filters")
	public RoomHistoryFiltersDTO getFilters(
		@PathVariable("code") String roomCode,
		Authentication authentication
	) {
		return historyService.getFilters(roomCode, authentication.getName());
	}

	@GetMapping("/{code}/history/stage-one")
	public RoomHistoryStageOneSectionDTO getStageOne(
		@PathVariable("code") String roomCode,
		Authentication authentication
	) {
		return historyService.getStageOne(roomCode, authentication.getName());
	}

	@GetMapping("/{code}/history/stage-one/restaurants/{restaurantId}/votes")
	public List<RoomHistoryStageOneVoteDTO> getStageOneRestaurantVotes(
		@PathVariable("code") String roomCode,
		@PathVariable("restaurantId") long restaurantId,
		Authentication authentication
	) {
		return historyService.getStageOneRestaurantVotes(roomCode, restaurantId, authentication.getName());
	}

	@GetMapping("/{code}/history/stage-two")
	public List<RoomHistoryStageTwoRowDTO> getStageTwo(
		@PathVariable("code") String roomCode,
		Authentication authentication
	) {
		return historyService.getStageTwo(roomCode, authentication.getName());
	}
}
