package com.mitrian.diploma.voting.stageone.promotion;

import com.mitrian.diploma.voting.room.entity.Room;
import com.mitrian.diploma.voting.room.entity.RoomState;
import com.mitrian.diploma.voting.stageone.entity.RoomStageOneCandidate;
import java.time.LocalDateTime;

final class StageOnePromotionUnitTestData {

	static final Long ROOM_ID = 42L;
	static final Long OWNER_ID = 100L;
	static final int PARTICIPANT_COUNT = 4;

	static final LocalDateTime T0 = LocalDateTime.of(2025, 1, 1, 12, 0);
	static final LocalDateTime T1 = LocalDateTime.of(2025, 1, 1, 12, 1);
	static final LocalDateTime T2 = LocalDateTime.of(2025, 1, 1, 12, 2);
	static final LocalDateTime T3 = LocalDateTime.of(2025, 1, 1, 12, 3);
	static final LocalDateTime T4 = LocalDateTime.of(2025, 1, 1, 12, 4);
	static final LocalDateTime T5 = LocalDateTime.of(2025, 1, 1, 12, 5);

	private StageOnePromotionUnitTestData() {
	}

	static Room roomStageOne(int stageOneVoteRows, Integer stageOneParticipantSnapshot) {
		Room room = new Room();
		room.setId(ROOM_ID);
		room.setOwnerId(OWNER_ID);
		room.setState(RoomState.STAGE_ONE);
		room.setParticipantCount(PARTICIPANT_COUNT);
		room.setStageOneParticipantCountSnapshot(stageOneParticipantSnapshot);
		room.setStageOneVoteRowsCount(stageOneVoteRows);
		room.setOrganizerDoubleWeightApplied(false);
		return room;
	}

	static RoomStageOneCandidate candidate(
		long restaurantId,
		int sortOrder,
		int suitableCount,
		int suitableWeightedCount,
		LocalDateTime baseReachedAt,
		LocalDateTime relaxedReachedAt,
		LocalDateTime baseWeightedReachedAt,
		LocalDateTime relaxedWeightedReachedAt
	) {
		RoomStageOneCandidate c = new RoomStageOneCandidate();
		c.setRoomId(ROOM_ID);
		c.setRestaurantId(restaurantId);
		c.setSortOrder(sortOrder);
		c.setSuitableCount(suitableCount);
		c.setSuitableWeightedCount(suitableWeightedCount);
		c.setBaseReachedAt(baseReachedAt);
		c.setRelaxedReachedAt(relaxedReachedAt);
		c.setBaseWeightedReachedAt(baseWeightedReachedAt);
		c.setRelaxedWeightedReachedAt(relaxedWeightedReachedAt);
		return c;
	}
}
