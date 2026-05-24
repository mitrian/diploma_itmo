package com.mitrian.diploma.history;

import static org.assertj.core.api.Assertions.assertThat;

import com.mitrian.diploma.HistoryIntegrationTestApplication;
import com.mitrian.diploma.auth.repository.UserRepository;
import com.mitrian.diploma.history.dto.RoomHistoryOverviewDTO;
import com.mitrian.diploma.history.service.HistoryService;
import com.mitrian.diploma.voting.catalog.repository.RestaurantKitchenTagRepository;
import com.mitrian.diploma.voting.catalog.repository.RestaurantRepository;
import com.mitrian.diploma.voting.room.entity.Room;
import com.mitrian.diploma.voting.room.entity.RoomState;
import com.mitrian.diploma.voting.room.filter.repository.KitchenTagRepository;
import com.mitrian.diploma.voting.room.filter.repository.RoomKitchenTagSelectionRepository;
import com.mitrian.diploma.voting.room.repository.RoomParticipantRepository;
import com.mitrian.diploma.voting.room.repository.RoomRepository;
import com.mitrian.diploma.voting.stageone.dto.StageOneVoteSuitableRequestDTO;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneCandidateRepository;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneFinalistRepository;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneVoteRepository;
import com.mitrian.diploma.voting.stageone.service.StageOneService;
import com.mitrian.diploma.voting.stagetwo.repository.RoomStageTwoRankRepository;
import com.mitrian.diploma.voting.support.VotingIntegrationTestSupport;
import com.mitrian.diploma.voting.support.VotingIntegrationTestSupport.StartedRoomContext;
import com.mitrian.diploma.voting.timeout.StageTimeoutService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = HistoryIntegrationTestApplication.class)
@Transactional
class HistoryVotingDurationE2eIntegrationTest {

	@Autowired
	private HistoryService historyService;
	@Autowired
	private StageTimeoutService stageTimeoutService;
	@Autowired
	private StageOneService stageOneService;
	@Autowired
	private RoomRepository roomRepository;
	@Autowired
	private VotingIntegrationTestSupport support;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RestaurantRepository restaurantRepository;
	@Autowired
	private RestaurantKitchenTagRepository restaurantKitchenTagRepository;
	@Autowired
	private KitchenTagRepository kitchenTagRepository;
	@Autowired
	private RoomKitchenTagSelectionRepository roomKitchenTagSelectionRepository;
	@Autowired
	private RoomParticipantRepository roomParticipantRepository;
	@Autowired
	private RoomStageOneCandidateRepository roomStageOneCandidateRepository;
	@Autowired
	private RoomStageOneVoteRepository roomStageOneVoteRepository;
	@Autowired
	private RoomStageOneFinalistRepository roomStageOneFinalistRepository;
	@Autowired
	private RoomStageTwoRankRepository roomStageTwoRankRepository;

	@BeforeEach
	void cleanDatabase() {
		roomStageTwoRankRepository.deleteAll();
		roomStageOneFinalistRepository.deleteAll();
		roomStageOneVoteRepository.deleteAll();
		roomStageOneCandidateRepository.deleteAll();
		roomKitchenTagSelectionRepository.deleteAll();
		restaurantKitchenTagRepository.deleteAll();
		roomParticipantRepository.deleteAll();
		roomRepository.deleteAll();
		restaurantRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	@DisplayName("Расчёт длительности: разница между finished_at и stage_one_started_at после реального завершения")
	void votingDuration_afterVotingFlow_matchesStoredTimestamps() {
		StartedRoomContext ctx = support.startStageOneWithParticipants(4, 2);
		List<Long> candidateIds = support.candidates(ctx.roomCode()).stream()
			.map(c -> c.getRestaurantId())
			.toList();

		stageOneService.vote(ctx.roomCode(), ctx.ownerLogin(), candidateIds.get(0), new StageOneVoteSuitableRequestDTO(true));
		stageOneService.vote(ctx.roomCode(), ctx.ownerLogin(), candidateIds.get(1), new StageOneVoteSuitableRequestDTO(false));

		Room room = support.reloadRoom(ctx.roomCode());
		stageTimeoutService.processStageOneTimeout(room.getId(), LocalDateTime.now());

		Room finished = support.reloadRoom(ctx.roomCode());
		assertThat(finished.getState()).isEqualTo(RoomState.FINISHED);
		assertThat(finished.getStageOneStartedAt()).isNotNull();
		assertThat(finished.getFinishedAt()).isNotNull();

		long expectedSeconds = Duration.between(finished.getStageOneStartedAt(), finished.getFinishedAt()).getSeconds();

		RoomHistoryOverviewDTO overview = historyService.getOverview(ctx.roomCode(), ctx.ownerLogin());
		assertThat(overview.stageOneStartedAt()).isEqualTo(finished.getStageOneStartedAt());
		assertThat(overview.finishedAt()).isEqualTo(finished.getFinishedAt());
		assertThat(overview.votingDurationSeconds()).isEqualTo(expectedSeconds);
		assertThat(overview.votingDurationSeconds()).isGreaterThanOrEqualTo(0L);
	}
}
