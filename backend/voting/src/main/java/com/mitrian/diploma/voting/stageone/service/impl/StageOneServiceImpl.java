package com.mitrian.diploma.voting.stageone.service.impl;

import com.mitrian.diploma.auth.entity.User;
import com.mitrian.diploma.auth.exception.UserNotFoundException;
import com.mitrian.diploma.auth.repository.UserRepository;
import com.mitrian.diploma.voting.room.entity.Room;
import com.mitrian.diploma.voting.room.entity.RoomState;
import com.mitrian.diploma.voting.room.exception.NotRoomParticipantException;
import com.mitrian.diploma.voting.room.exception.RoomNotFoundException;
import com.mitrian.diploma.voting.room.repository.RoomParticipantRepository;
import com.mitrian.diploma.voting.room.repository.RoomRepository;
import com.mitrian.diploma.voting.stageone.dto.StageOneVoteSuitableRequestDTO;
import com.mitrian.diploma.voting.stageone.entity.RoomStageOneCandidate;
import com.mitrian.diploma.voting.stageone.entity.RoomStageOneVote;
import com.mitrian.diploma.voting.stageone.exception.StageOneDataInvariantException;
import com.mitrian.diploma.voting.stageone.exception.StageOneNotActiveException;
import com.mitrian.diploma.voting.stageone.exception.StageOneVotingNotAllowedException;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneCandidateRepository;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneVoteRepository;
import com.mitrian.diploma.voting.stageone.service.StageOnePromotionService;
import com.mitrian.diploma.voting.stageone.service.StageOneService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StageOneServiceImpl implements StageOneService {

	private final UserRepository userRepository;
	private final RoomRepository roomRepository;
	private final RoomParticipantRepository roomParticipantRepository;
	private final RoomStageOneCandidateRepository roomStageOneCandidateRepository;
	private final RoomStageOneVoteRepository roomStageOneVoteRepository;
	private final StageOnePromotionService stageOnePromotionService;

	public StageOneServiceImpl(
		UserRepository userRepository,
		RoomRepository roomRepository,
		RoomParticipantRepository roomParticipantRepository,
		RoomStageOneCandidateRepository roomStageOneCandidateRepository,
		RoomStageOneVoteRepository roomStageOneVoteRepository,
		StageOnePromotionService stageOnePromotionService
	) {
		this.userRepository = userRepository;
		this.roomRepository = roomRepository;
		this.roomParticipantRepository = roomParticipantRepository;
		this.roomStageOneCandidateRepository = roomStageOneCandidateRepository;
		this.roomStageOneVoteRepository = roomStageOneVoteRepository;
		this.stageOnePromotionService = stageOnePromotionService;
	}

	@Override
	@Transactional
	public void vote(String roomCode, String userLogin, long restaurantId, StageOneVoteSuitableRequestDTO request) {
		User user = userRepository.findByLogin(userLogin)
			.orElseThrow(() -> new UserNotFoundException("User not found"));
		Room room = loadRoomForVote(roomCode);
		assertParticipant(room.getId(), user.getId());
		assertStageOne(room);

		Optional<RoomStageOneVote> existingVote = roomStageOneVoteRepository
			.findByRoomIdAndUserId(room.getId(), user.getId()).stream()
			.filter(v -> v.getRestaurantId().equals(restaurantId))
			.findFirst();
		if (existingVote.isPresent()) {
			if (!existingVote.get().getSuitable().equals(request.suitable())) {
				throw new StageOneVotingNotAllowedException("Vote has already been recorded for this restaurant");
			}
			return;
		}

		List<Long> pending = pendingRestaurantIdsInOrder(room.getId(), user.getId());
		Long expectedRestaurantId = pending.isEmpty() ? null : pending.get(0);

		if (expectedRestaurantId == null) {
			throw new StageOneVotingNotAllowedException("No pending restaurant to vote on");
		}
		if (!expectedRestaurantId.equals(restaurantId)) {
			throw new StageOneVotingNotAllowedException("Vote must be cast for the current restaurant in order");
		}

		RoomStageOneVote vote = new RoomStageOneVote();
		vote.setRoomId(room.getId());
		vote.setUserId(user.getId());
		vote.setRestaurantId(restaurantId);
		vote.setSuitable(request.suitable());
		roomStageOneVoteRepository.save(vote);
		room.setStageOneVoteRowsCount(room.getStageOneVoteRowsCount() + 1);
		roomRepository.save(room);
		if (request.suitable()) {
			RoomStageOneCandidate candidate = roomStageOneCandidateRepository
				.findByRoomIdAndRestaurantId(room.getId(), restaurantId)
				.orElseThrow(() -> new StageOneDataInvariantException(
					"Candidate not found for room=" + room.getId() + ", restaurant=" + restaurantId
				));
			int participantSnapshot = room.getStageOneParticipantCountSnapshot() != null
				? room.getStageOneParticipantCountSnapshot()
				: room.getParticipantCount();
			int baseQuorum = participantSnapshot > 0 ? Math.floorDiv(participantSnapshot, 2) + 1 : 0;
			int relaxedQuorum = participantSnapshot > 0 ? Math.floorDiv(participantSnapshot, 2) : 0;
			int nextSuitable = candidate.getSuitableCount() + 1;
			candidate.setSuitableCount(nextSuitable);
			int weightedInc = room.getOwnerId().equals(user.getId()) ? 2 : 1;
			int nextWeighted = candidate.getSuitableWeightedCount() + weightedInc;
			candidate.setSuitableWeightedCount(nextWeighted);
			LocalDateTime voteCreatedAt = vote.getCreatedAt() != null ? vote.getCreatedAt() : LocalDateTime.now();
			if (baseQuorum > 0 && nextSuitable >= baseQuorum && candidate.getBaseReachedAt() == null) {
				candidate.setBaseReachedAt(voteCreatedAt);
			}
			if (relaxedQuorum > 0 && nextSuitable >= relaxedQuorum && candidate.getRelaxedReachedAt() == null) {
				candidate.setRelaxedReachedAt(voteCreatedAt);
			}
			if (baseQuorum > 0 && nextWeighted >= baseQuorum && candidate.getBaseWeightedReachedAt() == null) {
				candidate.setBaseWeightedReachedAt(voteCreatedAt);
			}
			if (relaxedQuorum > 0 && nextWeighted >= relaxedQuorum && candidate.getRelaxedWeightedReachedAt() == null) {
				candidate.setRelaxedWeightedReachedAt(voteCreatedAt);
			}
			roomStageOneCandidateRepository.save(candidate);
			roomStageOneCandidateRepository.flush();
		}
		roomStageOneVoteRepository.flush();
		stageOnePromotionService.onVoteRecorded(room.getId());
	}

	private Room loadRoomForVote(String roomCode) {
		String normalizedCode = normalizeRoomCode(roomCode);
		return roomRepository.findByCodeForUpdate(normalizedCode)
			.orElseThrow(() -> new RoomNotFoundException("Room not found"));
	}

	private void assertParticipant(Long roomId, Long userId) {
		if (!roomParticipantRepository.existsByRoomIdAndUserId(roomId, userId)) {
			throw new NotRoomParticipantException("User is not a participant of this room");
		}
	}

	private void assertStageOne(Room room) {
		if (room.getState() != RoomState.STAGE_ONE) {
			throw new StageOneNotActiveException("Stage one is not active for this room");
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

	private static String normalizeRoomCode(String code) {
		return code == null ? "" : code.trim().toUpperCase();
	}
}
