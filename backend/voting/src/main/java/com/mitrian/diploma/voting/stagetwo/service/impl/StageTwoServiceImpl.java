package com.mitrian.diploma.voting.stagetwo.service.impl;

import com.mitrian.diploma.auth.entity.User;
import com.mitrian.diploma.auth.exception.UserNotFoundException;
import com.mitrian.diploma.auth.repository.UserRepository;
import com.mitrian.diploma.voting.room.entity.Room;
import com.mitrian.diploma.voting.room.entity.RoomParticipant;
import com.mitrian.diploma.voting.room.entity.RoomState;
import com.mitrian.diploma.voting.room.exception.NotRoomParticipantException;
import com.mitrian.diploma.voting.room.exception.RoomNotFoundException;
import com.mitrian.diploma.voting.room.repository.RoomParticipantRepository;
import com.mitrian.diploma.voting.room.repository.RoomRepository;
import com.mitrian.diploma.voting.stageone.entity.RoomStageOneFinalist;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneFinalistRepository;
import com.mitrian.diploma.voting.stagetwo.dto.StageTwoFinalistRowDTO;
import com.mitrian.diploma.voting.stagetwo.dto.StageTwoMyRankRowDTO;
import com.mitrian.diploma.voting.stagetwo.dto.StageTwoRankEntryDTO;
import com.mitrian.diploma.voting.stagetwo.dto.StageTwoStatusResponseDTO;
import com.mitrian.diploma.voting.stagetwo.dto.StageTwoSubmitRanksRequestDTO;
import com.mitrian.diploma.voting.stagetwo.entity.RoomStageTwoRank;
import com.mitrian.diploma.voting.stagetwo.exception.StageTwoAlreadyVotedException;
import com.mitrian.diploma.voting.stagetwo.exception.StageTwoNotActiveException;
import com.mitrian.diploma.voting.stagetwo.exception.StageTwoRanksInvalidException;
import com.mitrian.diploma.voting.stagetwo.repository.RoomStageTwoRankRepository;
import com.mitrian.diploma.voting.stagetwo.service.StageTwoQueryService;
import com.mitrian.diploma.voting.stagetwo.service.StageTwoService;
import com.mitrian.diploma.voting.stagetwo.service.StageTwoWinnerResolver;
import com.mitrian.diploma.voting.stagetwo.util.StageTwoRanksValidatorImpl;
import com.mitrian.diploma.voting.room.entity.WinnerSelectionPrinciple;
import com.mitrian.diploma.voting.room.util.RoomCodeHelper;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StageTwoServiceImpl implements StageTwoService {

	private final UserRepository userRepository;
	private final RoomRepository roomRepository;
	private final RoomParticipantRepository roomParticipantRepository;
	private final RoomStageOneFinalistRepository roomStageOneFinalistRepository;
	private final RoomStageTwoRankRepository roomStageTwoRankRepository;
	private final StageTwoQueryService stageTwoQueryService;
	private final StageTwoWinnerResolver stageTwoWinnerResolver;
	private final StageTwoRanksValidatorImpl stageTwoRanksValidator;

	public StageTwoServiceImpl(
		UserRepository userRepository,
		RoomRepository roomRepository,
		RoomParticipantRepository roomParticipantRepository,
		RoomStageOneFinalistRepository roomStageOneFinalistRepository,
		RoomStageTwoRankRepository roomStageTwoRankRepository,
		StageTwoQueryService stageTwoQueryService,
		StageTwoWinnerResolver stageTwoWinnerResolver,
		StageTwoRanksValidatorImpl stageTwoRanksValidator
	) {
		this.userRepository = userRepository;
		this.roomRepository = roomRepository;
		this.roomParticipantRepository = roomParticipantRepository;
		this.roomStageOneFinalistRepository = roomStageOneFinalistRepository;
		this.roomStageTwoRankRepository = roomStageTwoRankRepository;
		this.stageTwoQueryService = stageTwoQueryService;
		this.stageTwoWinnerResolver = stageTwoWinnerResolver;
		this.stageTwoRanksValidator = stageTwoRanksValidator;
	}

	@Override
	@Transactional(readOnly = true)
	public StageTwoStatusResponseDTO getStatus(String roomCode, String userLogin) {
		return stageTwoQueryService.getStatus(roomCode, userLogin);
	}

	@Override
	@Transactional
	public void submitRanks(String roomCode, String userLogin, StageTwoSubmitRanksRequestDTO request) {
		User user = userRepository.findByLogin(userLogin)
			.orElseThrow(() -> new UserNotFoundException("User not found"));
		Room room = loadRoomForSubmit(roomCode);
		assertParticipant(room.getId(), user.getId());

		if (room.getState() != RoomState.STAGE_TWO) {
			throw new StageTwoNotActiveException("Stage two is not active for this room");
		}
		if (roomStageTwoRankRepository.existsByRoomIdAndUserId(room.getId(), user.getId())) {
			throw new StageTwoAlreadyVotedException("You have already submitted your stage two vote");
		}

		List<RoomStageOneFinalist> finalistRows =
			roomStageOneFinalistRepository.findByRoomIdOrderByPositionAsc(room.getId());
		if (finalistRows.size() < 2) {
			throw new StageTwoNotActiveException("Stage two requires at least two finalists");
		}

		Set<Long> finalistIds = finalistRows.stream()
			.map(RoomStageOneFinalist::getRestaurantId)
			.collect(Collectors.toSet());

		stageTwoRanksValidator.validateRanksPayload(request.ranks(), finalistIds);

		for (StageTwoRankEntryDTO e : request.ranks()) {
			RoomStageTwoRank row = new RoomStageTwoRank();
			row.setRoomId(room.getId());
			row.setUserId(user.getId());
			row.setRestaurantId(e.restaurantId());
			row.setRankValue(e.rank());
			roomStageTwoRankRepository.save(row);
		}

		maybeFinalizeStageTwo(room.getId());
	}

	private Room loadRoomForSubmit(String roomCode) {
		String normalized = RoomCodeHelper.normalize(roomCode);
		return roomRepository.findByCodeForUpdate(normalized)
			.orElseThrow(() -> new RoomNotFoundException("Room not found"));
	}

	@Override
	@Transactional
	public void finalizeByTimeout(Long roomId) {
		Room room = roomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException("Room not found"));
		if (room.getState() != RoomState.STAGE_TWO) {
			return;
		}

		List<RoomStageOneFinalist> finalistRows =
			roomStageOneFinalistRepository.findByRoomIdOrderByPositionAsc(roomId);
		Set<Long> finalistIds = finalistRows.stream()
			.map(RoomStageOneFinalist::getRestaurantId)
			.collect(Collectors.toSet());
		int k = finalistIds.size();
		if (k < 2) {
			room.setChosenRestaurantId(null);
			room.setWinnerSelectionPrinciple(WinnerSelectionPrinciple.NONE.name());
			room.setState(RoomState.FINISHED);
			room.setFinishedAt(LocalDateTime.now());
			roomRepository.save(room);
			return;
		}

		List<RoomStageTwoRank> allRanks = roomStageTwoRankRepository.findByRoomId(roomId);
		Map<Long, List<RoomStageTwoRank>> byUser = allRanks.stream()
			.collect(Collectors.groupingBy(RoomStageTwoRank::getUserId));
		List<RoomStageTwoRank> completeRanks = byUser.values().stream()
			.filter(userRanks -> userRanks.size() == k)
			.filter(userRanks -> {
				try {
					stageTwoRanksValidator.validateStoredRanksMatchFinalists(userRanks, finalistIds, k);
					return true;
				} catch (StageTwoRanksInvalidException ex) {
					return false;
				}
			})
			.flatMap(List::stream)
			.toList();

		if (completeRanks.isEmpty()) {
			room.setChosenRestaurantId(null);
			room.setWinnerSelectionPrinciple(WinnerSelectionPrinciple.NONE.name());
			room.setState(RoomState.FINISHED);
			room.setFinishedAt(LocalDateTime.now());
			room.setStageTwoTimeoutAt(null);
			room.setStageTwoTimeoutProcessed(false);
			roomRepository.save(room);
			return;
		}

		StageTwoWinnerResolver.StageTwoWinnerOutcome outcome = stageTwoWinnerResolver.resolve(room, finalistRows, completeRanks);
		room.setChosenRestaurantId(outcome.restaurantId());
		room.setWinnerSelectionPrinciple(outcome.principle().name());
		room.setState(RoomState.FINISHED);
		room.setFinishedAt(LocalDateTime.now());
		room.setStageTwoTimeoutAt(null);
		room.setStageTwoTimeoutProcessed(false);
		roomRepository.save(room);
	}


	private void maybeFinalizeStageTwo(Long roomId) {
		Room room = roomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException("Room not found"));
		if (room.getState() != RoomState.STAGE_TWO) {
			return;
		}

		List<RoomStageOneFinalist> finalistRows =
			roomStageOneFinalistRepository.findByRoomIdOrderByPositionAsc(roomId);
		Set<Long> finalistIds = finalistRows.stream()
			.map(RoomStageOneFinalist::getRestaurantId)
			.collect(Collectors.toSet());
		int k = finalistIds.size();
		if (k < 2) {
			return;
		}

		List<RoomParticipant> participants = roomParticipantRepository.findByRoomIdOrderByIdAsc(roomId);
		Set<Long> participantUserIds = participants.stream()
			.map(RoomParticipant::getUserId)
			.collect(Collectors.toSet());

		List<RoomStageTwoRank> allRanks = roomStageTwoRankRepository.findByRoomId(roomId);
		Map<Long, List<RoomStageTwoRank>> byUser = allRanks.stream()
			.collect(Collectors.groupingBy(RoomStageTwoRank::getUserId));

		for (Long uid : participantUserIds) {
			List<RoomStageTwoRank> userRanks = byUser.get(uid);
			if (userRanks == null || userRanks.size() != k) {
				return;
			}
			try {
				stageTwoRanksValidator.validateStoredRanksMatchFinalists(userRanks, finalistIds, k);
			} catch (StageTwoRanksInvalidException ex) {
				return;
			}
		}

		StageTwoWinnerResolver.StageTwoWinnerOutcome outcome = stageTwoWinnerResolver.resolve(room, finalistRows, allRanks);
		room.setChosenRestaurantId(outcome.restaurantId());
		room.setWinnerSelectionPrinciple(outcome.principle().name());
		room.setState(RoomState.FINISHED);
		room.setFinishedAt(LocalDateTime.now());
		room.setStageTwoTimeoutAt(null);
		room.setStageTwoTimeoutProcessed(false);
		roomRepository.save(room);
	}

	private Room loadRoom(String roomCode) {
		String normalized = RoomCodeHelper.normalize(roomCode);
		return roomRepository.findByCode(normalized)
			.orElseThrow(() -> new RoomNotFoundException("Room not found"));
	}

	private void assertParticipant(Long roomId, Long userId) {
		if (!roomParticipantRepository.existsByRoomIdAndUserId(roomId, userId)) {
			throw new NotRoomParticipantException("User is not a participant of this room");
		}
	}
}
