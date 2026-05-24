package com.mitrian.diploma.history.service;

import com.mitrian.diploma.history.dto.RoomHistoryFiltersDTO;
import com.mitrian.diploma.history.dto.RoomHistoryOverviewDTO;
import com.mitrian.diploma.history.dto.RoomHistoryParticipantDTO;
import com.mitrian.diploma.history.dto.RoomHistoryStageOneSectionDTO;
import com.mitrian.diploma.history.dto.RoomHistoryStageOneVoteDTO;
import com.mitrian.diploma.history.dto.RoomHistoryStageTwoRowDTO;
import com.mitrian.diploma.history.dto.RoomHistorySummaryDTO;
import java.util.List;

public interface HistoryService {

	List<RoomHistorySummaryDTO> listMyFinishedRoomSummaries(String userLogin);

	RoomHistoryOverviewDTO getOverview(String roomCode, String userLogin);

	List<RoomHistoryParticipantDTO> getParticipants(String roomCode, String userLogin);

	RoomHistoryFiltersDTO getFilters(String roomCode, String userLogin);

	RoomHistoryStageOneSectionDTO getStageOne(String roomCode, String userLogin);

	List<RoomHistoryStageOneVoteDTO> getStageOneRestaurantVotes(
		String roomCode,
		long restaurantId,
		String userLogin
	);

	List<RoomHistoryStageTwoRowDTO> getStageTwo(String roomCode, String userLogin);
}
