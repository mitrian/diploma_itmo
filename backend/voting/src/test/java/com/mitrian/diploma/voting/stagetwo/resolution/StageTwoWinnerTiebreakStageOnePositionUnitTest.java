package com.mitrian.diploma.voting.stagetwo.resolution;

import static com.mitrian.diploma.voting.stagetwo.resolution.StageTwoWinnerResolverUnitTestData.OWNER_ID;
import static com.mitrian.diploma.voting.stagetwo.resolution.StageTwoWinnerResolverUnitTestData.USER_2;
import static com.mitrian.diploma.voting.stagetwo.resolution.StageTwoWinnerResolverUnitTestData.USER_3;
import static com.mitrian.diploma.voting.stagetwo.resolution.StageTwoWinnerResolverUnitTestData.USER_4;
import static com.mitrian.diploma.voting.stagetwo.resolution.StageTwoWinnerResolverUnitTestData.USER_5;
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

class StageTwoWinnerTiebreakStageOnePositionUnitTest {

	private StageTwoWinnerResolver resolver;

	@BeforeEach
	void setUp() {
		resolver = new StageTwoWinnerResolverImpl();
	}

	@Test
	@DisplayName("Равенство суммы и approval, ранги организатора в данных отсутствуют — меньшая position финалиста этапа 1, STAGE_TWO_TIEBREAK_BY_STAGE_ONE_POSITION")
	void fullTieFallsBackToStageOneFinalistPosition() {
		long restaurantA = 41L;
		long restaurantB = 42L;
		List<RoomStageOneFinalist> finalists = List.of(
			finalist(restaurantA, 3, 1),
			finalist(restaurantB, 3, 2)
		);
		// Четыре участника (не организатор): суммы рангов 6=6, approval 3=3; у организатора нет строк — блок ранга владельца пропускается
		List<RoomStageTwoRank> ranks = List.of(
			rank(USER_2, restaurantA, 1),
			rank(USER_2, restaurantB, 2),
			rank(USER_3, restaurantA, 2),
			rank(USER_3, restaurantB, 1),
			rank(USER_4, restaurantA, 2),
			rank(USER_4, restaurantB, 1),
			rank(USER_5, restaurantA, 1),
			rank(USER_5, restaurantB, 2)
		);

		var outcome = resolver.resolve(room(OWNER_ID), finalists, ranks);

		assertThat(outcome.restaurantId()).isEqualTo(restaurantA);
		assertThat(outcome.principle()).isEqualTo(WinnerSelectionPrinciple.STAGE_TWO_TIEBREAK_BY_STAGE_ONE_POSITION);
	}
}
