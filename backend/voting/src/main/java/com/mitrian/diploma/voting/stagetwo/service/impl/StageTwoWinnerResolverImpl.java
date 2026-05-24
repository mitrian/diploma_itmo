package com.mitrian.diploma.voting.stagetwo.service.impl;

import com.mitrian.diploma.voting.room.entity.Room;
import com.mitrian.diploma.voting.room.entity.WinnerSelectionPrinciple;
import com.mitrian.diploma.voting.stageone.entity.RoomStageOneFinalist;
import com.mitrian.diploma.voting.stagetwo.entity.RoomStageTwoRank;
import com.mitrian.diploma.voting.stagetwo.service.StageTwoWinnerResolver;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class StageTwoWinnerResolverImpl implements StageTwoWinnerResolver {

	@Override
	public StageTwoWinnerOutcome resolve(
		Room room,
		List<RoomStageOneFinalist> finalistRows,
		List<RoomStageTwoRank> allRanks
	) {
		Set<Long> finalistIds = finalistRows.stream()
			.map(RoomStageOneFinalist::getRestaurantId)
			.collect(Collectors.toSet());

		Map<Long, Integer> sumRanks = new HashMap<>();
		for (Long id : finalistIds) {
			sumRanks.put(id, 0);
		}
		for (RoomStageTwoRank r : allRanks) {
			sumRanks.merge(r.getRestaurantId(), r.getRankValue(), Integer::sum);
		}

		int minSum = finalistIds.stream().mapToInt(sumRanks::get).min().orElseThrow();
		List<Long> leaders = finalistIds.stream()
			.filter(id -> sumRanks.get(id) == minSum)
			.toList();
		if (leaders.size() == 1) {
			return new StageTwoWinnerOutcome(
				leaders.get(0),
				WinnerSelectionPrinciple.STAGE_TWO_RANK_SUM_UNIQUE_LEADER
			);
		}

		Map<Long, Integer> approvalByRestaurant = finalistRows.stream()
			.collect(Collectors.toMap(RoomStageOneFinalist::getRestaurantId, RoomStageOneFinalist::getApprovalCount));

		int maxApproval = leaders.stream().mapToInt(approvalByRestaurant::get).max().orElse(0);
		List<Long> afterApproval = leaders.stream()
			.filter(id -> Objects.equals(approvalByRestaurant.get(id), maxApproval))
			.toList();
		if (afterApproval.size() == 1) {
			return new StageTwoWinnerOutcome(
				afterApproval.get(0),
				WinnerSelectionPrinciple.STAGE_TWO_TIEBREAK_BY_APPROVAL_COUNT
			);
		}

		Long ownerId = room.getOwnerId();
		Map<Long, Integer> organizerRankByRestaurant = allRanks.stream()
			.filter(r -> r.getUserId().equals(ownerId))
			.collect(Collectors.toMap(RoomStageTwoRank::getRestaurantId, RoomStageTwoRank::getRankValue));

		List<Long> withOrgRank = afterApproval.stream()
			.filter(organizerRankByRestaurant::containsKey)
			.toList();
		if (!withOrgRank.isEmpty()) {
			int bestOrgRank = withOrgRank.stream().mapToInt(organizerRankByRestaurant::get).min().orElseThrow();
			List<Long> afterOrg = withOrgRank.stream()
				.filter(id -> organizerRankByRestaurant.get(id) == bestOrgRank)
				.toList();
			if (afterOrg.size() == 1) {
				return new StageTwoWinnerOutcome(
					afterOrg.get(0),
					WinnerSelectionPrinciple.STAGE_TWO_TIEBREAK_BY_ORGANIZER_RANK
				);
			}
		}

		Map<Long, Integer> positionByRestaurant = finalistRows.stream()
			.collect(Collectors.toMap(RoomStageOneFinalist::getRestaurantId, RoomStageOneFinalist::getPosition));
		long winnerId = afterApproval.stream()
			.min(Comparator.comparingInt(id -> positionByRestaurant.getOrDefault(id, Integer.MAX_VALUE)))
			.orElseThrow();
		return new StageTwoWinnerOutcome(
			winnerId,
			WinnerSelectionPrinciple.STAGE_TWO_TIEBREAK_BY_STAGE_ONE_POSITION
		);
	}
}
