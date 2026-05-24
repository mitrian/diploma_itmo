package com.mitrian.diploma.voting.stagetwo.resolution;

import static com.mitrian.diploma.voting.stagetwo.resolution.StageTwoWinnerResolverUnitTestData.OWNER_ID;
import static com.mitrian.diploma.voting.stagetwo.resolution.StageTwoWinnerResolverUnitTestData.USER_2;
import static com.mitrian.diploma.voting.stagetwo.resolution.StageTwoWinnerResolverUnitTestData.USER_3;
import static com.mitrian.diploma.voting.stagetwo.resolution.StageTwoWinnerResolverUnitTestData.USER_4;
import static com.mitrian.diploma.voting.stagetwo.resolution.StageTwoWinnerResolverUnitTestData.finalist;
import static com.mitrian.diploma.voting.stagetwo.resolution.StageTwoWinnerResolverUnitTestData.rank;
import static com.mitrian.diploma.voting.stagetwo.resolution.StageTwoWinnerResolverUnitTestData.room;
import static org.assertj.core.api.Assertions.assertThat;

import com.mitrian.diploma.voting.room.entity.WinnerSelectionPrinciple;
import com.mitrian.diploma.voting.stageone.entity.RoomStageOneFinalist;
import com.mitrian.diploma.voting.stagetwo.entity.RoomStageTwoRank;
import com.mitrian.diploma.voting.stagetwo.service.StageTwoWinnerResolver;
import com.mitrian.diploma.voting.stagetwo.service.impl.StageTwoWinnerResolverImpl;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StageTwoWinnerUniqueRankSumUnitTest {

	private StageTwoWinnerResolver resolver;

	@BeforeEach
	void setUp() {
		resolver = new StageTwoWinnerResolverImpl();
	}

	@Test
	@DisplayName("Два финалиста: у одного меньшая сумма рангов — победитель и принцип STAGE_TWO_RANK_SUM_UNIQUE_LEADER")
	void uniqueMinSumSelectsWinner() {
		long restaurantA = 11L;
		long restaurantB = 12L;
		List<RoomStageOneFinalist> finalists = List.of(
			finalist(restaurantA, 3, 1),
			finalist(restaurantB, 3, 2)
		);
		List<RoomStageTwoRank> ranks = List.of(
			rank(OWNER_ID, restaurantA, 1),
			rank(OWNER_ID, restaurantB, 2),
			rank(USER_2, restaurantA, 1),
			rank(USER_2, restaurantB, 2),
			rank(USER_3, restaurantA, 1),
			rank(USER_3, restaurantB, 2),
			rank(USER_4, restaurantA, 1),
			rank(USER_4, restaurantB, 2)
		);

		var outcome = resolver.resolve(room(OWNER_ID), finalists, ranks);

		assertThat(outcome.restaurantId()).isEqualTo(restaurantA);
		assertThat(outcome.principle()).isEqualTo(WinnerSelectionPrinciple.STAGE_TWO_RANK_SUM_UNIQUE_LEADER);
	}
}
