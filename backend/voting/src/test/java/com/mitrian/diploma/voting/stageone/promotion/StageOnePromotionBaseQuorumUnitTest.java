package com.mitrian.diploma.voting.stageone.promotion;

import static com.mitrian.diploma.voting.stageone.promotion.StageOnePromotionUnitTestData.PARTICIPANT_COUNT;
import static com.mitrian.diploma.voting.stageone.promotion.StageOnePromotionUnitTestData.ROOM_ID;
import static com.mitrian.diploma.voting.stageone.promotion.StageOnePromotionUnitTestData.T0;
import static com.mitrian.diploma.voting.stageone.promotion.StageOnePromotionUnitTestData.T1;
import static com.mitrian.diploma.voting.stageone.promotion.StageOnePromotionUnitTestData.T2;
import static com.mitrian.diploma.voting.stageone.promotion.StageOnePromotionUnitTestData.candidate;
import static com.mitrian.diploma.voting.stageone.promotion.StageOnePromotionUnitTestData.roomStageOne;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.mitrian.diploma.voting.room.entity.Room;
import com.mitrian.diploma.voting.room.entity.RoomState;
import com.mitrian.diploma.voting.room.entity.WinnerSelectionPrinciple;
import com.mitrian.diploma.voting.room.repository.RoomRepository;
import com.mitrian.diploma.voting.stageone.entity.RoomStageOneCandidate;
import com.mitrian.diploma.voting.stageone.entity.RoomStageOneFinalist;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneCandidateRepository;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneFinalistRepository;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneVoteRepository;
import com.mitrian.diploma.voting.stageone.service.impl.StageOnePromotionServiceImpl;
import com.mitrian.diploma.voting.timeout.VotingTimeoutProperties;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StageOnePromotionBaseQuorumUnitTest {

	@Mock
	private RoomRepository roomRepository;
	@Mock
	private RoomStageOneCandidateRepository roomStageOneCandidateRepository;
	@Mock
	private RoomStageOneVoteRepository roomStageOneVoteRepository;
	@Mock
	private RoomStageOneFinalistRepository roomStageOneFinalistRepository;

	private VotingTimeoutProperties votingTimeoutProperties;
	private StageOnePromotionServiceImpl promotionService;

	@BeforeEach
	void setUp() {
		votingTimeoutProperties = new VotingTimeoutProperties();
		votingTimeoutProperties.setStageTwoTimeoutSeconds(60);
		promotionService = new StageOnePromotionServiceImpl(
			roomRepository,
			roomStageOneCandidateRepository,
			roomStageOneVoteRepository,
			roomStageOneFinalistRepository,
			votingTimeoutProperties
		);
	}

	@Test
	@DisplayName("n=4: три одобрения на кандидата (строгий кворум) — три финалиста BASE_QUORUM, переход в STAGE_TWO")
	void threeCandidatesReachBaseQuorum_allFinalistsFromStrictVotes() {
		int candidatesCount = 3;
		Room room = roomStageOne(PARTICIPANT_COUNT * candidatesCount, PARTICIPANT_COUNT);
		when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

		List<RoomStageOneCandidate> candidates = List.of(
			candidate(501L, 0, 3, 4, T0, null, null, null),
			candidate(502L, 1, 3, 3, T1, null, null, null),
			candidate(503L, 2, 3, 3, T2, null, null, null)
		);
		when(roomStageOneCandidateRepository.findByRoomIdOrderBySortOrderAsc(ROOM_ID)).thenReturn(candidates);

		promotionService.onVoteRecorded(ROOM_ID);

		ArgumentCaptor<RoomStageOneFinalist> finalistCaptor = ArgumentCaptor.forClass(RoomStageOneFinalist.class);
		verify(roomStageOneFinalistRepository).deleteByRoomId(ROOM_ID);
		verify(roomStageOneFinalistRepository, times(3)).save(finalistCaptor.capture());

		assertThat(finalistCaptor.getAllValues())
			.hasSize(3)
			.allMatch(f -> RoomStageOneFinalist.INCLUDED_BASE_QUORUM.equals(f.getIncludedBy()))
			.extracting(RoomStageOneFinalist::getRestaurantId)
			.containsExactly(501L, 502L, 503L);

		assertThat(room.getState()).isEqualTo(RoomState.STAGE_TWO);
		assertThat(room.isOrganizerDoubleWeightApplied()).isFalse();
		verify(roomRepository).save(room);
		verify(roomStageOneCandidateRepository).deleteUnvotedByRoomId(ROOM_ID);
		verifyNoInteractions(roomStageOneVoteRepository);
	}

	@Test
	@DisplayName("n=4: ровно один кандидат проходит строгий кворум при полной матрице — FINISHED, победитель BASE_QUORUM, без STAGE_TWO")
	void oneCandidateReachesBaseQuorum_fullMatrix_finishesWithoutStageTwo() {
		int candidatesCount = 2;
		Room room = roomStageOne(PARTICIPANT_COUNT * candidatesCount, PARTICIPANT_COUNT);
		when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

		List<RoomStageOneCandidate> candidates = List.of(
			candidate(701L, 0, 3, 4, T0, null, null, null),
			candidate(702L, 1, 2, 2, null, null, null, null)
		);
		when(roomStageOneCandidateRepository.findByRoomIdOrderBySortOrderAsc(ROOM_ID)).thenReturn(candidates);

		promotionService.onVoteRecorded(ROOM_ID);

		ArgumentCaptor<RoomStageOneFinalist> finalistCaptor = ArgumentCaptor.forClass(RoomStageOneFinalist.class);
		verify(roomStageOneFinalistRepository).deleteByRoomId(ROOM_ID);
		verify(roomStageOneFinalistRepository, times(1)).save(finalistCaptor.capture());

		assertThat(finalistCaptor.getAllValues())
			.singleElement()
			.satisfies(f -> {
				assertThat(f.getRestaurantId()).isEqualTo(701L);
				assertThat(f.getIncludedBy()).isEqualTo(RoomStageOneFinalist.INCLUDED_BASE_QUORUM);
				assertThat(f.getApprovalCount()).isEqualTo(3);
			});

		assertThat(room.getState()).isEqualTo(RoomState.FINISHED);
		assertThat(room.getChosenRestaurantId()).isEqualTo(701L);
		assertThat(room.getWinnerSelectionPrinciple())
			.isEqualTo(WinnerSelectionPrinciple.STAGE_ONE_BASE_QUORUM_SINGLE.name());
		assertThat(room.isOrganizerDoubleWeightApplied()).isFalse();
		assertThat(room.getStageTwoTimeoutAt()).isNull();
		verify(roomRepository).save(room);
		verify(roomStageOneCandidateRepository).deleteUnvotedByRoomId(ROOM_ID);
		verifyNoInteractions(roomStageOneVoteRepository);
	}
}
