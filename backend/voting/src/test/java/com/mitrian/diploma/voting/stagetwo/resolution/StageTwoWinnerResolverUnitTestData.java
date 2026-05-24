package com.mitrian.diploma.voting.stagetwo.resolution;

import com.mitrian.diploma.voting.room.entity.Room;
import com.mitrian.diploma.voting.stageone.entity.RoomStageOneFinalist;
import com.mitrian.diploma.voting.stagetwo.entity.RoomStageTwoRank;

final class StageTwoWinnerResolverUnitTestData {

	static final long OWNER_ID = 100L;
	static final long USER_2 = 201L;
	static final long USER_3 = 202L;
	static final long USER_4 = 203L;
	static final long USER_5 = 204L;

	private StageTwoWinnerResolverUnitTestData() {
	}

	static Room room(long ownerId) {
		Room room = new Room();
		room.setOwnerId(ownerId);
		return room;
	}

	static RoomStageOneFinalist finalist(long restaurantId, int approvalCount, int position) {
		RoomStageOneFinalist f = new RoomStageOneFinalist();
		f.setRestaurantId(restaurantId);
		f.setApprovalCount(approvalCount);
		f.setPosition(position);
		return f;
	}

	static RoomStageTwoRank rank(long userId, long restaurantId, int rankValue) {
		RoomStageTwoRank r = new RoomStageTwoRank();
		r.setUserId(userId);
		r.setRestaurantId(restaurantId);
		r.setRankValue(rankValue);
		return r;
	}
}
