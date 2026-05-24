package com.mitrian.diploma.voting.timeout;

import com.mitrian.diploma.voting.room.entity.Room;
import com.mitrian.diploma.voting.room.entity.RoomParticipant;
import com.mitrian.diploma.voting.room.entity.RoomState;
import com.mitrian.diploma.voting.room.repository.RoomParticipantRepository;
import com.mitrian.diploma.voting.room.repository.RoomRepository;
import com.mitrian.diploma.voting.stageone.entity.RoomStageOneCandidate;
import com.mitrian.diploma.voting.stageone.entity.RoomStageOneVote;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneCandidateRepository;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneVoteRepository;
import com.mitrian.diploma.voting.stageone.service.StageOnePromotionService;
import com.mitrian.diploma.voting.stagetwo.service.StageTwoService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StageTimeoutService {

	private final RoomRepository roomRepository;
	private final RoomParticipantRepository roomParticipantRepository;
	private final RoomStageOneVoteRepository roomStageOneVoteRepository;
	private final RoomStageOneCandidateRepository roomStageOneCandidateRepository;
	private final StageOnePromotionService stageOnePromotionService;
	private final StageTwoService stageTwoService;

	public StageTimeoutService(
		RoomRepository roomRepository,
		RoomParticipantRepository roomParticipantRepository,
		RoomStageOneVoteRepository roomStageOneVoteRepository,
		RoomStageOneCandidateRepository roomStageOneCandidateRepository,
		StageOnePromotionService stageOnePromotionService,
		StageTwoService stageTwoService
	) {
		this.roomRepository = roomRepository;
		this.roomParticipantRepository = roomParticipantRepository;
		this.roomStageOneVoteRepository = roomStageOneVoteRepository;
		this.roomStageOneCandidateRepository = roomStageOneCandidateRepository;
		this.stageOnePromotionService = stageOnePromotionService;
		this.stageTwoService = stageTwoService;
	}

	@Transactional
	public void processStageOneTimeout(Long roomId, LocalDateTime now) {
		Room room = roomRepository.findById(roomId).orElse(null);
		if (room == null || room.getState() != RoomState.STAGE_ONE || room.isStageOneTimeoutProcessed()) {
			return;
		}

		List<RoomParticipant> participants = roomParticipantRepository.findByRoomIdOrderByIdAsc(roomId);
		List<RoomStageOneCandidate> candidates = roomStageOneCandidateRepository.findByRoomIdOrderBySortOrderAsc(roomId);
		if (participants.isEmpty() || candidates.isEmpty()) {
			room.setStageOneTimeoutProcessed(true);
			roomRepository.save(room);
			return;
		}

		Long ownerId = room.getOwnerId();
		List<RoomParticipant> toRemove = participants.stream()
			.filter(p -> !p.getUserId().equals(ownerId))
			.filter(p -> !roomStageOneVoteRepository.existsByRoomIdAndUserId(roomId, p.getUserId()))
			.toList();
		int excludedCount = toRemove.size();
		if (!toRemove.isEmpty()) {
			roomParticipantRepository.deleteAll(toRemove);
			roomParticipantRepository.flush();
		}

		int currentParticipants = (int) roomParticipantRepository.countByRoomId(roomId);
		room.setParticipantCount(currentParticipants);
		room.setStageOneParticipantCountSnapshot(currentParticipants);
		int baseQuorum = currentParticipants > 0 ? Math.floorDiv(currentParticipants, 2) + 1 : 0;
		int relaxedQuorum = currentParticipants > 0 ? Math.floorDiv(currentParticipants, 2) : 0;

		if (excludedCount > 0) {
			Set<Long> ownerSuitableRestaurants = roomStageOneVoteRepository
				.findByRoomIdAndUserId(roomId, ownerId).stream()
				.filter(v -> Boolean.TRUE.equals(v.getSuitable()))
				.map(RoomStageOneVote::getRestaurantId)
				.collect(Collectors.toSet());
			for (RoomStageOneCandidate c : candidates) {
				if (!ownerSuitableRestaurants.contains(c.getRestaurantId())) {
					continue;
				}
				int nextWeighted = (c.getSuitableWeightedCount() == null ? 0 : c.getSuitableWeightedCount()) + excludedCount;
				c.setSuitableWeightedCount(nextWeighted);
				if (baseQuorum > 0 && nextWeighted >= baseQuorum && c.getBaseWeightedReachedAt() == null) {
					c.setBaseWeightedReachedAt(now);
				}
				if (relaxedQuorum > 0 && nextWeighted >= relaxedQuorum && c.getRelaxedWeightedReachedAt() == null) {
					c.setRelaxedWeightedReachedAt(now);
				}
			}
		}
		for (RoomStageOneCandidate c : candidates) {
			int suitable = c.getSuitableCount() == null ? 0 : c.getSuitableCount();
			int weighted = c.getSuitableWeightedCount() == null ? 0 : c.getSuitableWeightedCount();
			if (baseQuorum > 0 && suitable >= baseQuorum && c.getBaseReachedAt() == null) {
				c.setBaseReachedAt(now);
			}
			if (relaxedQuorum > 0 && suitable >= relaxedQuorum && c.getRelaxedReachedAt() == null) {
				c.setRelaxedReachedAt(now);
			}
			if (baseQuorum > 0 && weighted >= baseQuorum && c.getBaseWeightedReachedAt() == null) {
				c.setBaseWeightedReachedAt(now);
			}
			if (relaxedQuorum > 0 && weighted >= relaxedQuorum && c.getRelaxedWeightedReachedAt() == null) {
				c.setRelaxedWeightedReachedAt(now);
			}
		}
		roomStageOneCandidateRepository.saveAll(candidates);

		long forcedVoteRows = (long) currentParticipants * candidates.size();
		room.setStageOneVoteRowsCount(forcedVoteRows > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) forcedVoteRows);
		room.setStageOneTimeoutProcessed(true);
		roomRepository.save(room);

		stageOnePromotionService.onVoteRecorded(roomId);
	}

	@Transactional
	public void processStageTwoTimeout(Long roomId) {
		Room room = roomRepository.findById(roomId).orElse(null);
		if (room == null || room.getState() != RoomState.STAGE_TWO || room.isStageTwoTimeoutProcessed()) {
			return;
		}
		stageTwoService.finalizeByTimeout(roomId);
		Room refreshed = roomRepository.findById(roomId).orElse(null);
		if (refreshed != null) {
			refreshed.setStageTwoTimeoutProcessed(true);
			roomRepository.save(refreshed);
		}
	}
}
