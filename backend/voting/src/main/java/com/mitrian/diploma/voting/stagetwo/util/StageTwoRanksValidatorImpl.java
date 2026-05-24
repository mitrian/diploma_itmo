package com.mitrian.diploma.voting.stagetwo.util;

import com.mitrian.diploma.voting.stagetwo.dto.StageTwoRankEntryDTO;
import com.mitrian.diploma.voting.stagetwo.entity.RoomStageTwoRank;
import com.mitrian.diploma.voting.stagetwo.exception.StageTwoRanksInvalidException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class StageTwoRanksValidatorImpl {

	public void validateRanksPayload(List<StageTwoRankEntryDTO> ranks, Set<Long> finalistIds) {
		int k = finalistIds.size();
		if (ranks.size() != k) {
			throw new StageTwoRanksInvalidException(
				"Expected exactly " + k + " rank entries, one per finalist restaurant"
			);
		}
		Set<Long> seenRestaurants = new HashSet<>();
		Set<Integer> seenRanks = new HashSet<>();
		for (StageTwoRankEntryDTO e : ranks) {
			if (e.restaurantId() == null || e.rank() == null) {
				throw new StageTwoRanksInvalidException("Each entry must have restaurantId and rank");
			}
			if (!finalistIds.contains(e.restaurantId())) {
				throw new StageTwoRanksInvalidException("Restaurant is not a finalist for this room");
			}
			if (!seenRestaurants.add(e.restaurantId())) {
				throw new StageTwoRanksInvalidException("Duplicate restaurant in ranks");
			}
			if (e.rank() < 1 || e.rank() > k) {
				throw new StageTwoRanksInvalidException("Each rank must be between 1 and " + k);
			}
			if (!seenRanks.add(e.rank())) {
				throw new StageTwoRanksInvalidException("Duplicate rank value");
			}
		}
	}

	public void validateStoredRanksMatchFinalists(List<RoomStageTwoRank> userRanks, Set<Long> finalistIds, int finalistsCount) {
		Set<Long> rests = new HashSet<>();
		Set<Integer> rankValues = new HashSet<>();
		for (RoomStageTwoRank r : userRanks) {
			if (!finalistIds.contains(r.getRestaurantId())) {
				throw new StageTwoRanksInvalidException("Invalid restaurant in stored ranks");
			}
			if (!rests.add(r.getRestaurantId())) {
				throw new StageTwoRanksInvalidException("Duplicate restaurant in stored ranks");
			}
			if (r.getRankValue() < 1 || r.getRankValue() > finalistsCount) {
				throw new StageTwoRanksInvalidException("Invalid rank in stored ranks");
			}
			if (!rankValues.add(r.getRankValue())) {
				throw new StageTwoRanksInvalidException("Duplicate rank in stored ranks");
			}
		}
	}
}
