package com.mitrian.diploma.voting.stagetwo.service;

import com.mitrian.diploma.voting.room.entity.Room;
import com.mitrian.diploma.voting.room.entity.WinnerSelectionPrinciple;
import com.mitrian.diploma.voting.stageone.entity.RoomStageOneFinalist;
import com.mitrian.diploma.voting.stagetwo.entity.RoomStageTwoRank;
import java.util.List;

public interface StageTwoWinnerResolver {

	StageTwoWinnerOutcome resolve(
		Room room,
		List<RoomStageOneFinalist> finalistRows,
		List<RoomStageTwoRank> allRanks
	);

	record StageTwoWinnerOutcome(long restaurantId, WinnerSelectionPrinciple principle) {
	}
}
