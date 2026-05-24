package com.mitrian.diploma.voting.stageone.promotion;

import static com.mitrian.diploma.voting.stageone.promotion.StageOnePromotionUnitTestData.PARTICIPANT_COUNT;
import static com.mitrian.diploma.voting.stageone.promotion.StageOnePromotionUnitTestData.ROOM_ID;
import static com.mitrian.diploma.voting.stageone.promotion.StageOnePromotionUnitTestData.T0;
import static com.mitrian.diploma.voting.stageone.promotion.StageOnePromotionUnitTestData.T1;
import static com.mitrian.diploma.voting.stageone.promotion.StageOnePromotionUnitTestData.T2;
import static com.mitrian.diploma.voting.stageone.promotion.StageOnePromotionUnitTestData.T3;
import static com.mitrian.diploma.voting.stageone.promotion.StageOnePromotionUnitTestData.T4;
import static com.mitrian.diploma.voting.stageone.promotion.StageOnePromotionUnitTestData.T5;
import static com.mitrian.diploma.voting.stageone.promotion.StageOnePromotionUnitTestData.candidate;
import static com.mitrian.diploma.voting.stageone.promotion.StageOnePromotionUnitTestData.roomStageOne;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mitrian.diploma.voting.room.entity.Room;
import com.mitrian.diploma.voting.room.entity.RoomState;
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
class StageOnePromotionFinalistCapUnitTest {

	@Mock
	private RoomRepository roomRepository;
	@Mock
	private RoomStageOneCandidateRepository roomStageOneCandidateRepository;
	@Mock
	private RoomStageOneVoteRepository roomStageOneVoteRepository;
	@Mock
	private RoomStageOneFinalistRepository roomStageOneFinalistRepository;

	private StageOnePromotionServiceImpl promotionService;

	@BeforeEach
	void setUp() {
		VotingTimeoutProperties props = new VotingTimeoutProperties();
		props.setStageTwoTimeoutSeconds(60);
		promotionService = new StageOnePromotionServiceImpl(
			roomRepository,
			roomStageOneCandidateRepository,
			roomStageOneVoteRepository,
			roomStageOneFinalistRepository,
			props
		);
	}

	@Test
	@DisplayName("n=4: шесть кандидатов проходят строгий кворум — сохраняются только пять финалистов (порядок по времени достижения порога)")
	void sixBaseQuorumCandidates_onlyFiveFinalistsPersisted() {
		int candidatesCount = 6;
		Room room = roomStageOne(PARTICIPANT_COUNT * candidatesCount, PARTICIPANT_COUNT);
		when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

		List<RoomStageOneCandidate> candidates = List.of(
			candidate(701L, 0, 3, 3, T0, null, null, null),
			candidate(702L, 1, 3, 3, T1, null, null, null),
			candidate(703L, 2, 3, 3, T2, null, null, null),
			candidate(704L, 3, 3, 3, T3, null, null, null),
			candidate(705L, 4, 3, 3, T4, null, null, null),
			candidate(706L, 5, 3, 3, T5, null, null, null)
		);
		when(roomStageOneCandidateRepository.findByRoomIdOrderBySortOrderAsc(ROOM_ID)).thenReturn(candidates);

		promotionService.onVoteRecorded(ROOM_ID);

		ArgumentCaptor<RoomStageOneFinalist> cap = ArgumentCaptor.forClass(RoomStageOneFinalist.class);
		verify(roomStageOneFinalistRepository).deleteByRoomId(ROOM_ID);
		verify(roomStageOneFinalistRepository, times(5)).save(cap.capture());

		assertThat(cap.getAllValues())
			.hasSize(5)
			.allMatch(f -> RoomStageOneFinalist.INCLUDED_BASE_QUORUM.equals(f.getIncludedBy()))
			.extracting(RoomStageOneFinalist::getRestaurantId)
			.containsExactly(701L, 702L, 703L, 704L, 705L);

		assertThat(room.getState()).isEqualTo(RoomState.STAGE_TWO);
		verify(roomRepository).save(room);
	}
}
