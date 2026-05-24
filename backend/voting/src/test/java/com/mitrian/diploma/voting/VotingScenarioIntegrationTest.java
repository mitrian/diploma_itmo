package com.mitrian.diploma.voting;

import static org.assertj.core.api.Assertions.assertThat;

import com.mitrian.diploma.VotingIntegrationTestApplication;
import com.mitrian.diploma.auth.repository.UserRepository;
import com.mitrian.diploma.voting.catalog.repository.RestaurantKitchenTagRepository;
import com.mitrian.diploma.voting.catalog.repository.RestaurantRepository;
import com.mitrian.diploma.voting.room.entity.Room;
import com.mitrian.diploma.voting.room.entity.RoomState;
import com.mitrian.diploma.voting.room.entity.WinnerSelectionPrinciple;
import com.mitrian.diploma.voting.room.filter.repository.KitchenTagRepository;
import com.mitrian.diploma.voting.room.filter.repository.RoomKitchenTagSelectionRepository;
import com.mitrian.diploma.voting.room.repository.RoomParticipantRepository;
import com.mitrian.diploma.voting.room.repository.RoomRepository;
import com.mitrian.diploma.voting.stageone.dto.StageOneVoteSuitableRequestDTO;
import com.mitrian.diploma.voting.stageone.entity.RoomStageOneFinalist;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneCandidateRepository;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneFinalistRepository;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneVoteRepository;
import com.mitrian.diploma.voting.stageone.service.StageOneService;
import com.mitrian.diploma.voting.stagetwo.dto.StageTwoRankEntryDTO;
import com.mitrian.diploma.voting.stagetwo.dto.StageTwoSubmitRanksRequestDTO;
import com.mitrian.diploma.voting.stagetwo.repository.RoomStageTwoRankRepository;
import com.mitrian.diploma.voting.stagetwo.service.StageTwoService;
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

@SpringBootTest(classes = VotingIntegrationTestApplication.class)
@Transactional
class VotingScenarioIntegrationTest {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private KitchenTagRepository kitchenTagRepository;
	@Autowired
	private RestaurantRepository restaurantRepository;
	@Autowired
	private RestaurantKitchenTagRepository restaurantKitchenTagRepository;
	@Autowired
	private RoomKitchenTagSelectionRepository roomKitchenTagSelectionRepository;
	@Autowired
	private RoomParticipantRepository roomParticipantRepository;
	@Autowired
	private RoomRepository roomRepository;
	@Autowired
	private RoomStageOneCandidateRepository roomStageOneCandidateRepository;
	@Autowired
	private RoomStageOneVoteRepository roomStageOneVoteRepository;
	@Autowired
	private RoomStageOneFinalistRepository roomStageOneFinalistRepository;
	@Autowired
	private RoomStageTwoRankRepository roomStageTwoRankRepository;
	@Autowired
	private StageTimeoutService stageTimeoutService;
	@Autowired
	private StageOneService stageOneService;
	@Autowired
	private StageTwoService stageTwoService;
	@Autowired
	private VotingIntegrationTestSupport support;

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
	@DisplayName("Запуск первого этапа: фиксируются начало и дедлайн завершения")
	void startStageOne_recordsStartAndDeadline() {
		StartedRoomContext ctx = support.startStageOneWithParticipants(2, 2);
		Room room = support.reloadRoom(ctx.roomCode());

		assertThat(room.getState()).isEqualTo(RoomState.STAGE_ONE);
		assertThat(room.getStageOneStartedAt()).isNotNull();
		assertThat(room.getStageOneTimeoutAt()).isNotNull();
		assertThat(room.getFinishedAt()).isNull();

		long configuredSeconds = support.stageOneTimeoutSeconds();
		long actualSeconds = Duration.between(room.getStageOneStartedAt(), room.getStageOneTimeoutAt()).getSeconds();
		assertThat(actualSeconds).isEqualTo(configuredSeconds);
	}

	@Test
	@DisplayName("Истечение таймера этапа 1 при наличии финалистов: переход в STAGE_TWO")
	void stageOneTimeout_withFinalists_movesToStageTwo() {
		StartedRoomContext ctx = support.startStageOneWithParticipants(4, 3);
		support.voteAllCandidates(ctx.roomCode(), ctx.ownerLogin(), true);
		support.voteAllCandidates(ctx.roomCode(), ctx.memberLogins().get(0), true);

		Room before = support.reloadRoom(ctx.roomCode());
		stageTimeoutService.processStageOneTimeout(before.getId(), LocalDateTime.now());

		Room after = support.reloadRoom(ctx.roomCode());
		assertThat(after.getState()).isEqualTo(RoomState.STAGE_TWO);
		assertThat(roomStageOneFinalistRepository.findByRoomIdOrderByPositionAsc(after.getId())).hasSizeGreaterThanOrEqualTo(2);
		assertThat(after.getStageTwoTimeoutAt()).isNotNull();
	}

	@Test
	@DisplayName("Истечение таймера этапа 1 при одном финалисте: FINISHED и победитель")
	void stageOneTimeout_withSingleFinalist_finishesRoom() {
		StartedRoomContext ctx = support.startStageOneWithParticipants(4, 2);
		List<Long> candidateIds = support.candidates(ctx.roomCode()).stream()
			.map(c -> c.getRestaurantId())
			.toList();

		stageOneService.vote(ctx.roomCode(), ctx.ownerLogin(), candidateIds.get(0), new StageOneVoteSuitableRequestDTO(true));
		stageOneService.vote(ctx.roomCode(), ctx.ownerLogin(), candidateIds.get(1), new StageOneVoteSuitableRequestDTO(false));

		Room before = support.reloadRoom(ctx.roomCode());
		stageTimeoutService.processStageOneTimeout(before.getId(), LocalDateTime.now());

		Room after = support.reloadRoom(ctx.roomCode());
		assertThat(after.getState()).isEqualTo(RoomState.FINISHED);
		assertThat(after.getChosenRestaurantId()).isEqualTo(candidateIds.get(0));
		assertThat(after.getWinnerSelectionPrinciple())
			.isEqualTo(WinnerSelectionPrinciple.STAGE_ONE_BASE_QUORUM_SINGLE.name());
		assertThat(after.getFinishedAt()).isNotNull();
		assertThat(roomStageOneFinalistRepository.findByRoomIdOrderByPositionAsc(after.getId())).hasSize(1);
	}

	@Test
	@DisplayName("Истечение таймера этапа 1 без финалистов по базовому порогу: резервный механизм (RELAXED_QUORUM)")
	void stageOneTimeout_withoutBaseFinalists_appliesReserveRelaxedQuorum() {
		StartedRoomContext ctx = support.startStageOneWithParticipants(4, 2);
		List<Long> candidateIds = support.candidates(ctx.roomCode()).stream()
			.map(c -> c.getRestaurantId())
			.toList();
		for (String login : ctx.allLogins()) {
			boolean approve = login.equals(ctx.ownerLogin()) || login.equals(ctx.memberLogins().get(0));
			stageOneService.vote(
				ctx.roomCode(), login, candidateIds.get(0), new StageOneVoteSuitableRequestDTO(approve)
			);
			stageOneService.vote(
				ctx.roomCode(), login, candidateIds.get(1), new StageOneVoteSuitableRequestDTO(approve)
			);
		}

		Room before = support.reloadRoom(ctx.roomCode());
		stageTimeoutService.processStageOneTimeout(before.getId(), LocalDateTime.now());

		Room after = support.reloadRoom(ctx.roomCode());
		assertThat(after.getState()).isEqualTo(RoomState.STAGE_TWO);
		List<RoomStageOneFinalist> finalists = roomStageOneFinalistRepository.findByRoomIdOrderByPositionAsc(after.getId());
		assertThat(finalists).hasSize(2);
		assertThat(finalists).allMatch(f -> RoomStageOneFinalist.INCLUDED_RELAXED_QUORUM.equals(f.getIncludedBy()));
		assertThat(after.isOrganizerDoubleWeightApplied()).isFalse();
	}

	@Test
	@DisplayName("Пять финалистов до таймера: досрочное завершение этапа 1 и переход в STAGE_TWO")
	void fiveFinalistsBeforeTimeout_finishesStageOneEarly() {
		StartedRoomContext ctx = support.startStageOneWithParticipants(4, 6);
		for (String login : ctx.allLogins()) {
			if (support.reloadRoom(ctx.roomCode()).getState() != RoomState.STAGE_ONE) {
				break;
			}
			support.voteAllCandidates(ctx.roomCode(), login, true);
		}

		Room room = support.reloadRoom(ctx.roomCode());
		assertThat(room.getState()).isEqualTo(RoomState.STAGE_TWO);
		assertThat(room.isStageOneTimeoutProcessed()).isFalse();
		assertThat(roomStageOneFinalistRepository.findByRoomIdOrderByPositionAsc(room.getId())).hasSize(5);
	}

	@Test
	@DisplayName("Истечение таймера этапа 2: победитель по полным корректным ранжированиям")
	void stageTwoTimeout_resolvesWinnerFromCompleteRankings() {
		StartedRoomContext ctx = support.startStageOneWithParticipants(4, 2);
		support.voteAllCandidatesForAllUsers(ctx, true);

		Room stageTwoRoom = support.reloadRoom(ctx.roomCode());
		assertThat(stageTwoRoom.getState()).isEqualTo(RoomState.STAGE_TWO);

		List<RoomStageOneFinalist> finalists =
			roomStageOneFinalistRepository.findByRoomIdOrderByPositionAsc(stageTwoRoom.getId());
		assertThat(finalists).hasSizeGreaterThanOrEqualTo(2);

		Long winnerId = finalists.get(0).getRestaurantId();
		Long otherId = finalists.get(1).getRestaurantId();

		stageTwoService.submitRanks(
			ctx.roomCode(),
			ctx.ownerLogin(),
			new StageTwoSubmitRanksRequestDTO(List.of(
				new StageTwoRankEntryDTO(winnerId, 1),
				new StageTwoRankEntryDTO(otherId, 2)
			))
		);

		stageTimeoutService.processStageTwoTimeout(stageTwoRoom.getId());

		Room finished = support.reloadRoom(ctx.roomCode());
		assertThat(finished.getState()).isEqualTo(RoomState.FINISHED);
		assertThat(finished.getChosenRestaurantId()).isEqualTo(winnerId);
		assertThat(finished.getWinnerSelectionPrinciple())
			.isEqualTo(WinnerSelectionPrinciple.STAGE_TWO_RANK_SUM_UNIQUE_LEADER.name());
		assertThat(finished.getFinishedAt()).isNotNull();
	}

	@Test
	@DisplayName("Завершение комнаты: фиксируется момент окончания голосования")
	void roomCompletion_recordsFinishedAt() {
		StartedRoomContext ctx = support.startStageOneWithParticipants(4, 2);
		List<Long> candidateIds = support.candidates(ctx.roomCode()).stream()
			.map(c -> c.getRestaurantId())
			.toList();

		stageOneService.vote(ctx.roomCode(), ctx.ownerLogin(), candidateIds.get(0), new StageOneVoteSuitableRequestDTO(true));
		stageOneService.vote(ctx.roomCode(), ctx.ownerLogin(), candidateIds.get(1), new StageOneVoteSuitableRequestDTO(false));

		Room before = support.reloadRoom(ctx.roomCode());
		assertThat(before.getFinishedAt()).isNull();

		stageTimeoutService.processStageOneTimeout(before.getId(), LocalDateTime.now());

		Room after = support.reloadRoom(ctx.roomCode());
		assertThat(after.getState()).isEqualTo(RoomState.FINISHED);
		assertThat(after.getFinishedAt()).isNotNull();
		assertThat(after.getStageOneStartedAt()).isNotNull();
		assertThat(after.getFinishedAt()).isAfterOrEqualTo(after.getStageOneStartedAt());
	}
}
