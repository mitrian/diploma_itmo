package com.mitrian.diploma.voting.stageone.service.impl;

import com.mitrian.diploma.auth.entity.User;
import com.mitrian.diploma.auth.exception.UserNotFoundException;
import com.mitrian.diploma.auth.repository.UserRepository;
import com.mitrian.diploma.voting.catalog.mapper.RestaurantCardMapper;
import com.mitrian.diploma.voting.catalog.entity.Restaurant;
import com.mitrian.diploma.voting.catalog.repository.RestaurantRepository;
import com.mitrian.diploma.voting.room.entity.Room;
import com.mitrian.diploma.voting.room.entity.RoomState;
import com.mitrian.diploma.voting.room.exception.NotRoomParticipantException;
import com.mitrian.diploma.voting.room.exception.RoomNotFoundException;
import com.mitrian.diploma.voting.room.repository.RoomParticipantRepository;
import com.mitrian.diploma.voting.room.repository.RoomRepository;
import com.mitrian.diploma.voting.stageone.dto.RestaurantCardDTO;
import com.mitrian.diploma.voting.stageone.dto.StageOneCurrentResponseDTO;
import com.mitrian.diploma.voting.stageone.dto.StageOneFinalistRowDTO;
import com.mitrian.diploma.voting.stageone.dto.StageOneStatusResponseDTO;
import com.mitrian.diploma.voting.stageone.dto.StageOneUpcomingResponseDTO;
import com.mitrian.diploma.voting.stageone.entity.RoomStageOneCandidate;
import com.mitrian.diploma.voting.stageone.entity.RoomStageOneFinalist;
import com.mitrian.diploma.voting.stageone.entity.RoomStageOneVote;
import com.mitrian.diploma.voting.stageone.exception.StageOneDataInvariantException;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneCandidateRepository;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneFinalistRepository;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneVoteRepository;
import com.mitrian.diploma.voting.stageone.service.StageOneQueryService;
import com.mitrian.diploma.voting.room.util.RoomCodeHelper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StageOneQueryServiceImpl implements StageOneQueryService {

	private static final int MAX_UPCOMING_LIMIT = 25;

	private final UserRepository userRepository;
	private final RoomRepository roomRepository;
	private final RoomParticipantRepository roomParticipantRepository;
	private final RoomStageOneCandidateRepository roomStageOneCandidateRepository;
	private final RoomStageOneFinalistRepository roomStageOneFinalistRepository;
	private final RoomStageOneVoteRepository roomStageOneVoteRepository;
	private final RestaurantRepository restaurantRepository;
	private final RestaurantCardMapper restaurantCardMapper;

	public StageOneQueryServiceImpl(
		UserRepository userRepository,
		RoomRepository roomRepository,
		RoomParticipantRepository roomParticipantRepository,
		RoomStageOneCandidateRepository roomStageOneCandidateRepository,
		RoomStageOneFinalistRepository roomStageOneFinalistRepository,
		RoomStageOneVoteRepository roomStageOneVoteRepository,
		RestaurantRepository restaurantRepository,
		RestaurantCardMapper restaurantCardMapper
	) {
		this.userRepository = userRepository;
		this.roomRepository = roomRepository;
		this.roomParticipantRepository = roomParticipantRepository;
		this.roomStageOneCandidateRepository = roomStageOneCandidateRepository;
		this.roomStageOneFinalistRepository = roomStageOneFinalistRepository;
		this.roomStageOneVoteRepository = roomStageOneVoteRepository;
		this.restaurantRepository = restaurantRepository;
		this.restaurantCardMapper = restaurantCardMapper;
	}

	@Override
	@Transactional(readOnly = true)
	public StageOneCurrentResponseDTO getCurrent(String roomCode, String userLogin) {
		User user = userRepository.findByLogin(userLogin)
			.orElseThrow(() -> new UserNotFoundException("User not found"));
		Room room = loadRoom(roomCode);
		assertParticipant(room.getId(), user.getId());
		if (room.getState() != RoomState.STAGE_ONE) {
			return new StageOneCurrentResponseDTO(true, null);
		}

		List<Long> pending = pendingRestaurantIdsInOrder(room.getId(), user.getId());
		if (pending.isEmpty()) {
			return new StageOneCurrentResponseDTO(true, null);
		}

		Restaurant restaurant = restaurantRepository.findById(pending.get(0))
			.orElseThrow(() -> new IllegalStateException("Restaurant not found for stage-one candidate"));
		return new StageOneCurrentResponseDTO(false, restaurantCardMapper.toCard(restaurant));
	}

	@Override
	@Transactional(readOnly = true)
	public StageOneUpcomingResponseDTO getUpcoming(String roomCode, String userLogin, int limit) {
		User user = userRepository.findByLogin(userLogin)
			.orElseThrow(() -> new UserNotFoundException("User not found"));
		Room room = loadRoom(roomCode);
		assertParticipant(room.getId(), user.getId());
		if (room.getState() != RoomState.STAGE_ONE) {
			return new StageOneUpcomingResponseDTO(true, List.of());
		}

		int lim = Math.min(Math.max(limit, 1), MAX_UPCOMING_LIMIT);
		List<Long> pending = pendingRestaurantIdsInOrder(room.getId(), user.getId());
		if (pending.isEmpty()) {
			return new StageOneUpcomingResponseDTO(true, List.of());
		}

		List<Long> slice = pending.subList(0, Math.min(lim, pending.size()));
		Map<Long, Restaurant> byId = restaurantRepository.findAllById(slice).stream()
			.collect(Collectors.toMap(Restaurant::getId, Function.identity()));

		List<RestaurantCardDTO> cards = slice.stream()
			.map(byId::get)
			.filter(Objects::nonNull)
			.map(restaurantCardMapper::toCard)
			.toList();
		return new StageOneUpcomingResponseDTO(false, cards);
	}

	@Override
	@Transactional(readOnly = true)
	public StageOneStatusResponseDTO getStatus(String roomCode, String userLogin) {
		User user = userRepository.findByLogin(userLogin)
			.orElseThrow(() -> new UserNotFoundException("User not found"));
		Room room = loadRoom(roomCode);
		assertParticipant(room.getId(), user.getId());

		long participantCount = room.getParticipantCount();
		long quorumBaseCount = room.getStageOneParticipantCountSnapshot() != null
			? room.getStageOneParticipantCountSnapshot()
			: participantCount;
		int n = quorumBaseCount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) quorumBaseCount;
		int baseQuorum = n > 0 ? Math.floorDiv(n, 2) + 1 : 0;
		int relaxedQuorum = n > 0 ? Math.floorDiv(n, 2) : 0;

		List<StageOneFinalistRowDTO> finalists;
		if (room.getState() == RoomState.STAGE_ONE) {
			finalists = List.of();
		} else {
			List<RoomStageOneFinalist> rows = roomStageOneFinalistRepository.findByRoomIdOrderByPositionAsc(room.getId());
			List<Long> restaurantIds = rows.stream().map(RoomStageOneFinalist::getRestaurantId).distinct().toList();
			Map<Long, Restaurant> restaurantsById = restaurantRepository.findAllById(restaurantIds).stream()
				.collect(Collectors.toMap(Restaurant::getId, Function.identity()));

			finalists = rows.stream()
				.map(f -> {
					Restaurant r = restaurantsById.get(f.getRestaurantId());
					if (r == null) {
						throw new StageOneDataInvariantException("Finalist restaurant not found: " + f.getRestaurantId());
					}
					return new StageOneFinalistRowDTO(
						f.getPosition(),
						f.getRestaurantId(),
						restaurantCardMapper.toCard(r),
						f.getApprovalCount(),
						f.getIncludedBy()
					);
				})
				.toList();
		}

		return new StageOneStatusResponseDTO(
			room.getState(),
			room.getState() == RoomState.STAGE_ONE,
			participantCount,
			baseQuorum,
			relaxedQuorum,
			room.getStageOneTimeoutAt(),
			finalists
		);
	}

	private Room loadRoom(String roomCode) {
		String normalizedCode = RoomCodeHelper.normalize(roomCode);
		return roomRepository.findByCode(normalizedCode)
			.orElseThrow(() -> new RoomNotFoundException("Room not found"));
	}

	private void assertParticipant(Long roomId, Long userId) {
		if (!roomParticipantRepository.existsByRoomIdAndUserId(roomId, userId)) {
			throw new NotRoomParticipantException("User is not a participant of this room");
		}
	}

	private List<Long> pendingRestaurantIdsInOrder(Long roomId, Long userId) {
		List<RoomStageOneCandidate> candidates = roomStageOneCandidateRepository.findByRoomIdOrderBySortOrderAsc(
			roomId
		);
		if (candidates.isEmpty()) {
			return List.of();
		}
		Set<Long> votedRestaurantIds = roomStageOneVoteRepository.findByRoomIdAndUserId(roomId, userId).stream()
			.map(RoomStageOneVote::getRestaurantId)
			.collect(Collectors.toCollection(HashSet::new));

		List<Long> pending = new ArrayList<>();
		for (RoomStageOneCandidate c : candidates) {
			if (!votedRestaurantIds.contains(c.getRestaurantId())) {
				pending.add(c.getRestaurantId());
			}
		}
		return pending;
	}

}
