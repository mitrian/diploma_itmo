package com.mitrian.diploma.voting.stageone.service.impl;

import com.mitrian.diploma.voting.room.entity.Room;
import com.mitrian.diploma.voting.room.entity.RoomState;
import com.mitrian.diploma.voting.room.exception.RoomNotFoundException;
import com.mitrian.diploma.voting.room.repository.RoomRepository;
import com.mitrian.diploma.voting.stageone.exception.StageOneDataInvariantException;
import com.mitrian.diploma.voting.stageone.exception.StageOneNotActiveException;
import com.mitrian.diploma.voting.stageone.entity.RoomStageOneCandidate;
import com.mitrian.diploma.voting.stageone.entity.RoomStageOneFinalist;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneCandidateRepository;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneFinalistRepository;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneVoteRepository;
import com.mitrian.diploma.voting.stageone.service.StageOnePromotionService;
import com.mitrian.diploma.voting.timeout.VotingTimeoutProperties;
import com.mitrian.diploma.voting.room.entity.WinnerSelectionPrinciple;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StageOnePromotionServiceImpl implements StageOnePromotionService {

	private static final int MAX_FINALISTS = 5;

	private final RoomRepository roomRepository;
	private final RoomStageOneCandidateRepository roomStageOneCandidateRepository;
	private final RoomStageOneVoteRepository roomStageOneVoteRepository;
	private final RoomStageOneFinalistRepository roomStageOneFinalistRepository;
	private final VotingTimeoutProperties votingTimeoutProperties;

	public StageOnePromotionServiceImpl(
		RoomRepository roomRepository,
		RoomStageOneCandidateRepository roomStageOneCandidateRepository,
		RoomStageOneVoteRepository roomStageOneVoteRepository,
		RoomStageOneFinalistRepository roomStageOneFinalistRepository,
		VotingTimeoutProperties votingTimeoutProperties
	) {
		this.roomRepository = roomRepository;
		this.roomStageOneCandidateRepository = roomStageOneCandidateRepository;
		this.roomStageOneVoteRepository = roomStageOneVoteRepository;
		this.roomStageOneFinalistRepository = roomStageOneFinalistRepository;
		this.votingTimeoutProperties = votingTimeoutProperties;
	}

	@Override
	@Transactional
	public void onVoteRecorded(Long roomId) {
		Room room = roomRepository.findById(roomId)
			.orElseThrow(() -> new RoomNotFoundException("Room not found"));
		if (room.getState() != RoomState.STAGE_ONE) {
			throw new StageOneNotActiveException("Stage one is not active for this room");
		}

		List<RoomStageOneCandidate> candidates = roomStageOneCandidateRepository.findByRoomIdOrderBySortOrderAsc(roomId);
		if (candidates.isEmpty()) {
			throw new StageOneDataInvariantException("Stage one has no candidates for this room");
		}

		int participantCount = room.getStageOneParticipantCountSnapshot() != null
			? room.getStageOneParticipantCountSnapshot()
			: room.getParticipantCount();
		if (participantCount <= 0) {
			throw new StageOneDataInvariantException("Room has no participants; cannot compute quorum");
		}

		int baseQuorum = Math.floorDiv(participantCount, 2) + 1;
		int relaxedQuorum = Math.floorDiv(participantCount, 2);
		long expectedVoteRows = participantCount * candidates.size();
		long actualVoteRows = room.getStageOneVoteRowsCount();
		boolean allVotesIn = actualVoteRows >= expectedVoteRows;

		// 1) Только строгий кворум.
		List<FinalistPick> strictPicks = buildPicksBaseQuorum(candidates, baseQuorum);
		// 2) Ослабленный — только если по строгому 0 финалистов и матрица полная
		List<FinalistPick> normalPicks;
		if (!strictPicks.isEmpty()) {
			normalPicks = strictPicks;
		} else if (allVotesIn) {
			normalPicks = buildPicksRelaxedQuorum(candidates, relaxedQuorum);
		} else {
			normalPicks = List.of();
		}

		boolean hitCap = normalPicks.size() >= MAX_FINALISTS;
		boolean shouldFinish = hitCap || allVotesIn;

		if (!shouldFinish) {
			persistFinalists(roomId, normalPicks);
			return;
		}

		List<FinalistPick> finalPicks = normalPicks;
		boolean reservePointless = candidates.size() == 1 && normalPicks.size() == 1;
		// 3) Удвоение голоса организатора — только если строгий не дал ни одного финалиста и после ослабленного всё ещё < 2
		boolean organizerDoubleAllowed = strictPicks.isEmpty() && normalPicks.size() < 2;
		if (organizerDoubleAllowed && !reservePointless) {
			List<FinalistPick> reservePicks = computeReservePicksOrganizerDoubleWeight(
				candidates,
				baseQuorum,
				relaxedQuorum,
				allVotesIn
			);
			finalPicks = mergeNormalWithReservePicks(normalPicks, reservePicks);
			room.setOrganizerDoubleWeightApplied(true);
		}

		boolean outcomeFromRelaxedOrReserveOnly = strictPicks.isEmpty();
		if (outcomeFromRelaxedOrReserveOnly && finalPicks.size() == 1) {
			finalPicks = List.of();
			room.setOrganizerDoubleWeightApplied(false);
		}

		persistFinalists(roomId, finalPicks);

		if (finalPicks.size() >= 2) {
			room.setChosenRestaurantId(null);
			room.setWinnerSelectionPrinciple(null);
			room.setState(RoomState.STAGE_TWO);
			room.setStageOneTimeoutAt(null);
			room.setStageOneTimeoutProcessed(false);
			room.setStageTwoTimeoutAt(LocalDateTime.now().plusSeconds(votingTimeoutProperties.getStageTwoTimeoutSeconds()));
			room.setStageTwoTimeoutProcessed(false);
		} else {
			Long winnerRestaurantId = finalPicks.size() == 1 ? finalPicks.get(0).restaurantId() : null;
			room.setChosenRestaurantId(winnerRestaurantId);
			if (winnerRestaurantId != null
				&& finalPicks.size() == 1
				&& RoomStageOneFinalist.INCLUDED_BASE_QUORUM.equals(finalPicks.get(0).includedBy())) {
				room.setWinnerSelectionPrinciple(WinnerSelectionPrinciple.STAGE_ONE_BASE_QUORUM_SINGLE.name());
			} else {
				room.setWinnerSelectionPrinciple(WinnerSelectionPrinciple.NONE.name());
			}
			room.setState(RoomState.FINISHED);
			room.setFinishedAt(LocalDateTime.now());
			room.setStageOneTimeoutAt(null);
			room.setStageOneTimeoutProcessed(false);
			room.setStageTwoTimeoutAt(null);
			room.setStageTwoTimeoutProcessed(false);
		}
		roomRepository.save(room);

		roomStageOneCandidateRepository.deleteUnvotedByRoomId(roomId);
	}

	private static List<FinalistPick> mergeNormalWithReservePicks(
		List<FinalistPick> normalPicks,
		List<FinalistPick> reservePicks
	) {
		Map<Long, FinalistPick> byRestaurant = new HashMap<>();
		for (FinalistPick n : normalPicks) {
			byRestaurant.put(n.restaurantId(), n);
		}
		for (FinalistPick r : reservePicks) {
			byRestaurant.putIfAbsent(r.restaurantId(), r);
		}
		List<FinalistPick> merged = new ArrayList<>(byRestaurant.values());
		merged.sort(FINALIST_PICK_ORDER);
		return capPicks(merged);
	}

	private List<FinalistPick> computeReservePicksOrganizerDoubleWeight(
		List<RoomStageOneCandidate> candidatesInOrder,
		int baseQuorum,
		int relaxedQuorum,
		boolean allVotesIn
	) {
		boolean anyBaseWeighted = candidatesInOrder.stream()
			.anyMatch(c -> safeCount(c.getSuitableWeightedCount()) >= baseQuorum);
		if (anyBaseWeighted) {
			return buildPicksWeightedQuorum(
				candidatesInOrder,
				baseQuorum,
				RoomStageOneFinalist.INCLUDED_ORGANIZER_DOUBLE,
				true
			);
		}
		if (allVotesIn && relaxedQuorum >= 1) {
			boolean anyRelaxedWeighted = candidatesInOrder.stream()
				.anyMatch(c -> safeCount(c.getSuitableWeightedCount()) >= relaxedQuorum);
			if (anyRelaxedWeighted) {
				return buildPicksWeightedQuorum(
					candidatesInOrder,
					relaxedQuorum,
					RoomStageOneFinalist.INCLUDED_ORGANIZER_DOUBLE,
					false
				);
			}
		}
		return List.of();
	}

	private static int safeCount(Integer value) {
		return value == null ? 0 : value;
	}

	private List<FinalistPick> buildPicksWeightedQuorum(
		List<RoomStageOneCandidate> candidatesInOrder,
		int threshold,
		String includedBy,
		boolean useBaseReachedAt
	) {
		List<FinalistPick> qualifying = new ArrayList<>();
		for (RoomStageOneCandidate c : candidatesInOrder) {
			if (safeCount(c.getSuitableWeightedCount()) < threshold) {
				continue;
			}
			LocalDateTime reachedAt = useBaseReachedAt
				? c.getBaseWeightedReachedAt()
				: c.getRelaxedWeightedReachedAt();
			if (reachedAt == null) {
				reachedAt = roomStageOneVoteRepository
					.findCreatedAtWhenWeightedSuitableCumulativeFirstReaches(
						c.getRoomId(),
						c.getRestaurantId(),
						threshold
					)
					.orElse(null);
			}
			if (reachedAt == null) {
				continue;
			}
			qualifying.add(new FinalistPick(
				c.getRestaurantId(),
				safeCount(c.getSuitableCount()),
				includedBy,
				reachedAt,
				c.getSortOrder()
			));
		}
		qualifying.sort(FINALIST_PICK_ORDER);
		return capPicks(qualifying);
	}

	private List<FinalistPick> buildPicksBaseQuorum(
		List<RoomStageOneCandidate> candidatesInOrder,
		int baseQuorum
	) {
		List<FinalistPick> qualifying = new ArrayList<>();
		for (RoomStageOneCandidate c : candidatesInOrder) {
			if (safeCount(c.getSuitableCount()) < baseQuorum) {
				continue;
			}
			LocalDateTime reachedAt = c.getBaseReachedAt();
			if (reachedAt == null && baseQuorum > 0) {
				reachedAt = roomStageOneVoteRepository
					.findCreatedAtOfSuitableVoteAtZeroBasedIndex(
						c.getRoomId(),
						c.getRestaurantId(),
						baseQuorum - 1
					)
					.orElse(null);
			}
			if (reachedAt == null) {
				continue;
			}
			qualifying.add(new FinalistPick(
				c.getRestaurantId(),
				safeCount(c.getSuitableCount()),
				RoomStageOneFinalist.INCLUDED_BASE_QUORUM,
				reachedAt,
				c.getSortOrder()
			));
		}
		qualifying.sort(FINALIST_PICK_ORDER);
		return capPicks(qualifying);
	}

	private List<FinalistPick> buildPicksRelaxedQuorum(
		List<RoomStageOneCandidate> candidatesInOrder,
		int relaxedQuorum
	) {
		if (relaxedQuorum < 1) {
			return List.of();
		}
		List<FinalistPick> qualifying = new ArrayList<>();
		for (RoomStageOneCandidate c : candidatesInOrder) {
			if (safeCount(c.getSuitableCount()) < relaxedQuorum) {
				continue;
			}
			LocalDateTime reachedAt = c.getRelaxedReachedAt();
			if (reachedAt == null) {
				reachedAt = roomStageOneVoteRepository
					.findCreatedAtOfSuitableVoteAtZeroBasedIndex(
						c.getRoomId(),
						c.getRestaurantId(),
						relaxedQuorum - 1
					)
					.orElse(null);
			}
			if (reachedAt == null) {
				continue;
			}
			qualifying.add(new FinalistPick(
				c.getRestaurantId(),
				safeCount(c.getSuitableCount()),
				RoomStageOneFinalist.INCLUDED_RELAXED_QUORUM,
				reachedAt,
				c.getSortOrder()
			));
		}
		qualifying.sort(FINALIST_PICK_ORDER);
		return capPicks(qualifying);
	}

	private static List<FinalistPick> capPicks(List<FinalistPick> qualifying) {
		int take = Math.min(MAX_FINALISTS, qualifying.size());
		return new ArrayList<>(qualifying.subList(0, take));
	}

	private static final Comparator<FinalistPick> FINALIST_PICK_ORDER = Comparator
		.comparing(FinalistPick::reachedThresholdAt)
		.thenComparingInt(FinalistPick::sortOrder);

	private void persistFinalists(Long roomId, List<FinalistPick> picks) {
		roomStageOneFinalistRepository.deleteByRoomId(roomId);
		int position = 1;
		for (FinalistPick p : picks) {
			RoomStageOneFinalist row = new RoomStageOneFinalist();
			row.setRoomId(roomId);
			row.setRestaurantId(p.restaurantId());
			row.setApprovalCount(p.approvalCount());
			row.setIncludedBy(p.includedBy());
			row.setPosition(position++);
			roomStageOneFinalistRepository.save(row);
		}
	}

	private record FinalistPick(
		Long restaurantId,
		int approvalCount,
		String includedBy,
		LocalDateTime reachedThresholdAt,
		int sortOrder
	) {
	}
}
