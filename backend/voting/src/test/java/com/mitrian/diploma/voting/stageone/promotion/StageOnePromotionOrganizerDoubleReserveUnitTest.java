package com.mitrian.diploma.voting.stageone.promotion;

import static com.mitrian.diploma.voting.stageone.promotion.StageOnePromotionUnitTestData.PARTICIPANT_COUNT;
import static com.mitrian.diploma.voting.stageone.promotion.StageOnePromotionUnitTestData.ROOM_ID;
import static com.mitrian.diploma.voting.stageone.promotion.StageOnePromotionUnitTestData.T0;
import static com.mitrian.diploma.voting.stageone.promotion.StageOnePromotionUnitTestData.T1;
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
class StageOnePromotionOrganizerDoubleReserveUnitTest {

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
	@DisplayName("n=4: один финалист по ослабленному порогу + второй по взвешенному строгому кворуму — ORGANIZER_DOUBLE, флаг удвоения")
	void oneRelaxedFinalistPlusWeightedReserve_secondFinalistMarkedOrganizerDouble() {
		int candidatesCount = 2;
		Room room = roomStageOne(PARTICIPANT_COUNT * candidatesCount, PARTICIPANT_COUNT);
		when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

		List<RoomStageOneCandidate> candidates = List.of(
			candidate(801L, 0, 2, 2, null, T0, null, null),
			candidate(802L, 1, 1, 4, null, null, T1, null)
		);
		when(roomStageOneCandidateRepository.findByRoomIdOrderBySortOrderAsc(ROOM_ID)).thenReturn(candidates);

		promotionService.onVoteRecorded(ROOM_ID);

		ArgumentCaptor<RoomStageOneFinalist> cap = ArgumentCaptor.forClass(RoomStageOneFinalist.class);
		verify(roomStageOneFinalistRepository).deleteByRoomId(ROOM_ID);
		verify(roomStageOneFinalistRepository, times(2)).save(cap.capture());

		assertThat(cap.getAllValues())
			.hasSize(2)
			.extracting(RoomStageOneFinalist::getRestaurantId)
			.containsExactly(801L, 802L);

		assertThat(cap.getAllValues().get(0).getIncludedBy()).isEqualTo(RoomStageOneFinalist.INCLUDED_RELAXED_QUORUM);
		assertThat(cap.getAllValues().get(1).getIncludedBy()).isEqualTo(RoomStageOneFinalist.INCLUDED_ORGANIZER_DOUBLE);

		assertThat(room.isOrganizerDoubleWeightApplied()).isTrue();
		assertThat(room.getState()).isEqualTo(RoomState.STAGE_TWO);
		verify(roomRepository).save(room);
	}
}
