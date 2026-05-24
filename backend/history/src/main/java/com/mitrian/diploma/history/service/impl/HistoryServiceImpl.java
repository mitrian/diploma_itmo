package com.mitrian.diploma.history.service.impl;

import com.mitrian.diploma.auth.exception.UserNotFoundException;
import com.mitrian.diploma.auth.repository.UserRepository;
import com.mitrian.diploma.history.dto.HistoryRoomState;
import com.mitrian.diploma.history.dto.RoomHistoryFiltersDTO;
import com.mitrian.diploma.history.dto.RoomHistoryOverviewDTO;
import com.mitrian.diploma.history.dto.RoomHistoryParticipantDTO;
import com.mitrian.diploma.history.dto.RoomHistoryStageOneSectionDTO;
import com.mitrian.diploma.history.dto.RoomHistoryStageOneVoteDTO;
import com.mitrian.diploma.history.dto.RoomHistoryStageTwoRowDTO;
import com.mitrian.diploma.history.dto.RoomHistorySummaryDTO;
import com.mitrian.diploma.history.dto.row.RestaurantBaseRow;
import com.mitrian.diploma.history.dto.row.RoomHeaderRow;
import com.mitrian.diploma.history.dto.row.StageOneCandidateRow;
import com.mitrian.diploma.history.dto.row.StageOneFinalistRow;
import com.mitrian.diploma.history.dto.row.StageOneVoteRow;
import com.mitrian.diploma.history.dto.row.StageTwoRankRow;
import com.mitrian.diploma.history.exception.RoomHistoryAccessDeniedException;
import com.mitrian.diploma.history.exception.RoomHistoryNotAvailableException;
import com.mitrian.diploma.history.exception.RoomHistoryNotFoundException;
import com.mitrian.diploma.history.mapper.HistoryMapper;
import com.mitrian.diploma.history.repository.HistoryQueryRepository;
import com.mitrian.diploma.history.service.HistoryService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HistoryServiceImpl implements HistoryService {

	private final HistoryQueryRepository historyQueryRepository;
	private final UserRepository userRepository;
	private final HistoryMapper historyMapper;

	public HistoryServiceImpl(
		HistoryQueryRepository historyQueryRepository,
		UserRepository userRepository,
		HistoryMapper historyMapper
	) {
		this.historyQueryRepository = historyQueryRepository;
		this.userRepository = userRepository;
		this.historyMapper = historyMapper;
	}

	@Override
	@Transactional(readOnly = true)
	public List<RoomHistorySummaryDTO> listMyFinishedRoomSummaries(String userLogin) {
		Long userId = resolveUserId(userLogin);
		return historyQueryRepository.findFinishedRoomsForUser(userId, HistoryRoomState.FINISHED.name()).stream()
			.map(historyMapper::toSummary)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public RoomHistoryOverviewDTO getOverview(String roomCode, String userLogin) {
		RoomHeaderRow header = loadAuthorizedRoom(roomCode, userLogin);

		String winnerName = null;
		if (header.chosenRestaurantId() != null) {
			RestaurantBaseRow card = historyQueryRepository
				.findRestaurantsByIds(List.of(header.chosenRestaurantId()))
				.stream().findFirst().orElse(null);
			if (card != null) {
				winnerName = card.name();
			}
		}

		return historyMapper.toOverview(header, winnerName);
	}

	@Override
	@Transactional(readOnly = true)
	public List<RoomHistoryParticipantDTO> getParticipants(String roomCode, String userLogin) {
		RoomHeaderRow header = loadAuthorizedRoom(roomCode, userLogin);
		return historyMapper.toParticipants(historyQueryRepository.findParticipants(header.roomId()));
	}

	@Override
	@Transactional(readOnly = true)
	public RoomHistoryFiltersDTO getFilters(String roomCode, String userLogin) {
		RoomHeaderRow header = loadAuthorizedRoom(roomCode, userLogin);
		return historyMapper.toFilters(
			header,
			historyQueryRepository.findKitchenTagSelections(header.roomId())
		);
	}

	@Override
	@Transactional(readOnly = true)
	public RoomHistoryStageOneSectionDTO getStageOne(String roomCode, String userLogin) {
		RoomHeaderRow header = loadAuthorizedRoom(roomCode, userLogin);
		List<StageOneCandidateRow> candidates =
			historyQueryRepository.findStageOneCandidates(header.roomId());
		List<StageOneVoteRow> votes =
			historyQueryRepository.findStageOneVotes(header.roomId());
		List<StageOneFinalistRow> finalists =
			historyQueryRepository.findStageOneFinalists(header.roomId());

		var kitchenTagsByRestaurant = historyMapper.mapRestaurantKitchenTagsByRestaurant(
			historyQueryRepository.findRestaurantKitchenTags(
				candidates.stream().map(StageOneCandidateRow::restaurantId).toList()
			)
		);

		return historyMapper.toStageOneSection(header, candidates, votes, finalists, kitchenTagsByRestaurant);
	}

	@Override
	@Transactional(readOnly = true)
	public List<RoomHistoryStageOneVoteDTO> getStageOneRestaurantVotes(
		String roomCode, long restaurantId, String userLogin
	) {
		RoomHeaderRow header = loadAuthorizedRoom(roomCode, userLogin);
		return historyMapper.toStageOneRestaurantVotes(
			historyQueryRepository.findStageOneVotesByRestaurant(header.roomId(), restaurantId)
		);
	}

	@Override
	@Transactional(readOnly = true)
	public List<RoomHistoryStageTwoRowDTO> getStageTwo(String roomCode, String userLogin) {
		RoomHeaderRow header = loadAuthorizedRoom(roomCode, userLogin);
		List<StageOneFinalistRow> finalists =
			historyQueryRepository.findStageOneFinalists(header.roomId());
		if (finalists.isEmpty()) {
			return List.of();
		}
		List<StageTwoRankRow> ranks = historyQueryRepository.findStageTwoRanks(header.roomId());
		if (ranks.isEmpty()) {
			return List.of();
		}

		List<Long> finalistIds = finalists.stream().map(StageOneFinalistRow::restaurantId).toList();
		Map<Long, RestaurantBaseRow> finalistCardsById = historyQueryRepository
			.findRestaurantsByIds(finalistIds).stream()
			.collect(Collectors.toMap(RestaurantBaseRow::restaurantId, java.util.function.Function.identity()));
		var kitchenTagsByRestaurant = historyMapper.mapRestaurantKitchenTagsByRestaurant(
			historyQueryRepository.findRestaurantKitchenTags(finalistIds)
		);

		return historyMapper.toStageTwoRows(header, finalists, ranks, finalistCardsById, kitchenTagsByRestaurant);
	}

	private RoomHeaderRow loadAuthorizedRoom(String roomCode, String userLogin) {
		Long viewerId = resolveUserId(userLogin);
		String normalizedCode = normalizeRoomCode(roomCode);
		RoomHeaderRow header = historyQueryRepository.findRoomHeader(normalizedCode)
			.orElseThrow(() -> new RoomHistoryNotFoundException("Room not found"));
		if (!historyQueryRepository.existsParticipant(header.roomId(), viewerId)) {
			throw new RoomHistoryAccessDeniedException("User is not a participant of this room");
		}
		HistoryRoomState state = HistoryRoomState.fromPersisted(header.state());
		if (state != HistoryRoomState.FINISHED) {
			throw new RoomHistoryNotAvailableException("History is available only for finished rooms");
		}
		return header;
	}

	private Long resolveUserId(String userLogin) {
		return userRepository.findByLogin(userLogin)
			.orElseThrow(() -> new UserNotFoundException("User not found"))
			.getId();
	}

	private static String normalizeRoomCode(String code) {
		return code == null ? "" : code.trim().toUpperCase();
	}
}
