package com.mitrian.diploma.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mitrian.diploma.HistoryIntegrationTestApplication;
import com.mitrian.diploma.auth.entity.User;
import com.mitrian.diploma.auth.repository.UserRepository;
import com.mitrian.diploma.history.dto.HistoryRoomState;
import com.mitrian.diploma.history.dto.RoomHistoryFiltersDTO;
import com.mitrian.diploma.history.dto.RoomHistoryOverviewDTO;
import com.mitrian.diploma.history.dto.RoomHistoryParticipantDTO;
import com.mitrian.diploma.history.dto.RoomHistoryStageOneSectionDTO;
import com.mitrian.diploma.history.dto.RoomHistoryStageOneVoteDTO;
import com.mitrian.diploma.history.dto.RoomHistoryStageTwoRowDTO;
import com.mitrian.diploma.history.dto.RoomHistorySummaryDTO;
import com.mitrian.diploma.history.dto.WinnerSelectionPrinciple;
import com.mitrian.diploma.history.exception.RoomHistoryAccessDeniedException;
import com.mitrian.diploma.history.exception.RoomHistoryNotAvailableException;
import com.mitrian.diploma.history.service.HistoryService;
import com.mitrian.diploma.voting.catalog.entity.Restaurant;
import com.mitrian.diploma.voting.catalog.entity.RestaurantKitchenTag;
import com.mitrian.diploma.voting.catalog.repository.RestaurantKitchenTagRepository;
import com.mitrian.diploma.voting.catalog.repository.RestaurantRepository;
import com.mitrian.diploma.voting.room.entity.Room;
import com.mitrian.diploma.voting.room.entity.RoomParticipant;
import com.mitrian.diploma.voting.room.entity.RoomState;
import com.mitrian.diploma.voting.room.filter.entity.KitchenTag;
import com.mitrian.diploma.voting.room.filter.entity.RoomKitchenTagSelection;
import com.mitrian.diploma.voting.room.filter.repository.KitchenTagRepository;
import com.mitrian.diploma.voting.room.filter.repository.RoomKitchenTagSelectionRepository;
import com.mitrian.diploma.voting.room.repository.RoomParticipantRepository;
import com.mitrian.diploma.voting.room.repository.RoomRepository;
import com.mitrian.diploma.voting.stageone.entity.RoomStageOneCandidate;
import com.mitrian.diploma.voting.stageone.entity.RoomStageOneFinalist;
import com.mitrian.diploma.voting.stageone.entity.RoomStageOneVote;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneCandidateRepository;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneFinalistRepository;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneVoteRepository;
import com.mitrian.diploma.voting.stagetwo.entity.RoomStageTwoRank;
import com.mitrian.diploma.voting.stagetwo.repository.RoomStageTwoRankRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = HistoryIntegrationTestApplication.class)
@Transactional
class HistoryIntegrationTest {

	private static final String ROOM_CODE = "HIST0001";
	private static final String ROOM_CODE_DURATION = "HISTDUR01";

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RoomRepository roomRepository;
	@Autowired
	private RoomParticipantRepository roomParticipantRepository;
	@Autowired
	private RestaurantRepository restaurantRepository;
	@Autowired
	private RestaurantKitchenTagRepository restaurantKitchenTagRepository;
	@Autowired
	private KitchenTagRepository kitchenTagRepository;
	@Autowired
	private RoomKitchenTagSelectionRepository roomKitchenTagSelectionRepository;
	@Autowired
	private RoomStageOneCandidateRepository roomStageOneCandidateRepository;
	@Autowired
	private RoomStageOneVoteRepository roomStageOneVoteRepository;
	@Autowired
	private RoomStageOneFinalistRepository roomStageOneFinalistRepository;
	@Autowired
	private RoomStageTwoRankRepository roomStageTwoRankRepository;
	@Autowired
	private HistoryService historyService;

	private List<Long> userIds;
	private Long roomId;
	private Long restaurantAId;
	private Long restaurantBId;
	private Long restaurantCId;
	private Long kitchenTagItalianId;
	private LocalDateTime votingStartedAt;
	private LocalDateTime votingFinishedAt;

	@BeforeEach
	void setUp() {
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

		KitchenTag italian = kitchenTagRepository.findBySlug("italian").orElseGet(() -> {
			KitchenTag kt = new KitchenTag();
			kt.setSlug("italian");
			kt.setLabelRu("итальянская");
			return kitchenTagRepository.save(kt);
		});
		kitchenTagItalianId = italian.getId();

		restaurantAId = restaurantRepository.save(sampleRestaurant("Ресторан A")).getId();
		restaurantBId = restaurantRepository.save(sampleRestaurant("Ресторан B")).getId();
		restaurantCId = restaurantRepository.save(sampleRestaurant("Ресторан C")).getId();

		RestaurantKitchenTag rkt = new RestaurantKitchenTag();
		rkt.setRestaurantId(restaurantAId);
		rkt.setKitchenTagId(kitchenTagItalianId);
		restaurantKitchenTagRepository.save(rkt);

		userIds = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			User u = new User();
			u.setLogin("histuser" + i);
			u.setPasswordHash("{noop}unused");
			u.setDisplayName("Участник " + i);
			userIds.add(userRepository.save(u).getId());
		}

		Room room = new Room();
		room.setCode(ROOM_CODE);
		room.setPasswordHash("{noop}room");
		room.setOwnerId(userIds.get(0));
		room.setState(RoomState.FINISHED);
		room.setParticipantCount(3);
		room.setStageOneParticipantCountSnapshot(3);
		room.setStageOneVoteRowsCount(0);
		room.setCenterLat(59.93);
		room.setCenterLon(30.31);
		room.setMaxDistanceMeters(2000);
		room.setOrganizerDoubleWeightApplied(false);
		room.setChosenRestaurantId(restaurantBId);
		room.setWinnerSelectionPrinciple(WinnerSelectionPrinciple.STAGE_TWO_RANK_SUM_UNIQUE_LEADER.name());
		votingStartedAt = LocalDateTime.now().minusMinutes(4).minusSeconds(35);
		votingFinishedAt = LocalDateTime.now().minusMinutes(1);
		room.setStageOneStartedAt(votingStartedAt);
		room.setFinishedAt(votingFinishedAt);
		roomId = roomRepository.save(room).getId();

		for (Long uid : userIds) {
			RoomParticipant rp = new RoomParticipant();
			rp.setRoomId(roomId);
			rp.setUserId(uid);
			rp.setOwner(uid.equals(userIds.get(0)));
			rp.setReady(true);
			rp.setFiltersConfirmed(true);
			roomParticipantRepository.save(rp);
		}

		RoomKitchenTagSelection sel = new RoomKitchenTagSelection();
		sel.setRoomId(roomId);
		sel.setUserId(userIds.get(0));
		sel.setKitchenTagId(kitchenTagItalianId);
		roomKitchenTagSelectionRepository.save(sel);

		saveCandidate(0, restaurantAId);
		saveCandidate(1, restaurantBId);
		saveCandidate(2, restaurantCId);

		saveVote(userIds.get(0), restaurantAId, true);
		saveVote(userIds.get(1), restaurantAId, true);
		saveVote(userIds.get(2), restaurantAId, false);

		saveVote(userIds.get(0), restaurantBId, true);
		saveVote(userIds.get(1), restaurantBId, true);
		saveVote(userIds.get(2), restaurantBId, true);

		saveVote(userIds.get(0), restaurantCId, false);

		saveFinalist(restaurantAId, 2, 1, RoomStageOneFinalist.INCLUDED_BASE_QUORUM);
		saveFinalist(restaurantBId, 3, 2, RoomStageOneFinalist.INCLUDED_BASE_QUORUM);

		saveRank(userIds.get(0), restaurantAId, 2);
		saveRank(userIds.get(0), restaurantBId, 1);
		saveRank(userIds.get(1), restaurantAId, 2);
		saveRank(userIds.get(1), restaurantBId, 1);
		saveRank(userIds.get(2), restaurantAId, 2);
		saveRank(userIds.get(2), restaurantBId, 1);
	}

	@Test
	@DisplayName("GET /history/overview содержит победителя и принцип выбора")
	void overview_returnsWinnerAndPrinciple() {
		RoomHistoryOverviewDTO overview = historyService.getOverview(ROOM_CODE, "histuser1");
		assertThat(overview.roomState()).isEqualTo(HistoryRoomState.FINISHED);
		assertThat(overview.ownerDisplayName()).isEqualTo("Участник 1");
		assertThat(overview.participantCount()).isEqualTo(3);
		assertThat(overview.chosenRestaurantId()).isEqualTo(restaurantBId);
		assertThat(overview.winnerRestaurantName()).isEqualTo("Ресторан B");
		assertThat(overview.winnerPrinciple())
			.isEqualTo(WinnerSelectionPrinciple.STAGE_TWO_RANK_SUM_UNIQUE_LEADER);
	}

	@Test
	@DisplayName("Длительность голосования: 12 мин между stage_one_started_at и finished_at (overview и список)")
	void votingDuration_returnsElapsedSecondsFromStoredTimestamps() {
		LocalDateTime started = LocalDateTime.of(2026, 5, 10, 14, 0, 0);
		LocalDateTime finished = started.plusMinutes(12);
		long expectedSeconds = Duration.between(started, finished).getSeconds();

		Room durationRoom = finishedRoomWithVotingTimestamps(ROOM_CODE_DURATION, started, finished);
		Long durationRoomId = durationRoom.getId();
		RoomParticipant rp = new RoomParticipant();
		rp.setRoomId(durationRoomId);
		rp.setUserId(userIds.get(0));
		rp.setOwner(true);
		rp.setReady(true);
		rp.setFiltersConfirmed(true);
		roomParticipantRepository.save(rp);

		RoomHistoryOverviewDTO overview =
			historyService.getOverview(ROOM_CODE_DURATION, "histuser1");
		assertThat(overview.stageOneStartedAt()).isEqualTo(started);
		assertThat(overview.finishedAt()).isEqualTo(finished);
		assertThat(overview.votingDurationSeconds()).isEqualTo(expectedSeconds);
		assertThat(overview.votingDurationSeconds()).isEqualTo(720L);

		RoomHistorySummaryDTO summary = historyService.listMyFinishedRoomSummaries("histuser1").stream()
			.filter(s -> ROOM_CODE_DURATION.equals(s.roomCode()))
			.findFirst()
			.orElseThrow();
		assertThat(summary.finishedAt()).isEqualTo(finished);
		assertThat(summary.votingDurationSeconds()).isEqualTo(expectedSeconds);
	}

	@Test
	@DisplayName("Длительность голосования: null, если stage_one_started_at не записан")
	void votingDuration_nullWhenStageOneStartMissing() {
		LocalDateTime finishedOnly = LocalDateTime.of(2026, 4, 1, 18, 30, 0);
		Room legacyRoom = finishedRoomWithVotingTimestamps("HISTLEG01", null, finishedOnly);
		RoomParticipant rp = new RoomParticipant();
		rp.setRoomId(legacyRoom.getId());
		rp.setUserId(userIds.get(1));
		rp.setOwner(false);
		rp.setReady(true);
		rp.setFiltersConfirmed(true);
		roomParticipantRepository.save(rp);

		RoomHistoryOverviewDTO overview = historyService.getOverview("HISTLEG01", "histuser2");
		assertThat(overview.stageOneStartedAt()).isNull();
		assertThat(overview.finishedAt()).isEqualTo(finishedOnly);
		assertThat(overview.votingDurationSeconds()).isNull();

		RoomHistorySummaryDTO summary = historyService.listMyFinishedRoomSummaries("histuser2").stream()
			.filter(s -> "HISTLEG01".equals(s.roomCode()))
			.findFirst()
			.orElseThrow();
		assertThat(summary.votingDurationSeconds()).isNull();
	}

	@Test
	@DisplayName("GET /history/participants — все участники с признаком владельца")
	void participants_returnsAllWithOwnerFlag() {
		List<RoomHistoryParticipantDTO> participants =
			historyService.getParticipants(ROOM_CODE, "histuser2");
		assertThat(participants).hasSize(3);
		assertThat(participants).extracting("displayName").containsExactlyInAnyOrder(
			"Участник 1", "Участник 2", "Участник 3"
		);
		assertThat(participants).filteredOn("owner", true).extracting("displayName")
			.containsExactly("Участник 1");
	}

	@Test
	@DisplayName("GET /history/filters — гео + кухонные теги c автором")
	void filters_returnsGeoAndKitchenTags() {
		RoomHistoryFiltersDTO filters = historyService.getFilters(ROOM_CODE, "histuser1");
		assertThat(filters.centerLat()).isEqualTo(59.93);
		assertThat(filters.centerLon()).isEqualTo(30.31);
		assertThat(filters.maxDistanceMeters()).isEqualTo(2000);
		assertThat(filters.kitchenTags()).extracting("slug").containsExactly("italian");
		assertThat(filters.kitchenTags()).extracting("pickedByLogin").containsExactly("histuser1");
	}

	@Test
	@DisplayName("GET /history/stage-one — список ресторанов с агрегатами без поимённых голосов")
	void stageOne_returnsAggregatedRowsWithoutPerUserVotes() {
		RoomHistoryStageOneSectionDTO section = historyService.getStageOne(ROOM_CODE, "histuser1");
		assertThat(section.outcome().participantCount()).isEqualTo(3);
		assertThat(section.outcome().baseQuorum()).isEqualTo(2);
		assertThat(section.outcome().relaxedQuorum()).isEqualTo(1);

		assertThat(section.restaurants()).hasSize(3);
		assertThat(section.restaurants())
			.extracting("restaurantId", "totalSuitable", "totalUnsuitable")
			.containsExactly(
				org.assertj.core.api.Assertions.tuple(restaurantAId, 2, 1),
				org.assertj.core.api.Assertions.tuple(restaurantBId, 3, 0),
				org.assertj.core.api.Assertions.tuple(restaurantCId, 0, 1)
			);
		assertThat(section.restaurants())
			.filteredOn("restaurantId", restaurantBId)
			.extracting("includedBy")
			.containsExactly(RoomStageOneFinalist.INCLUDED_BASE_QUORUM);
	}

	@Test
	@DisplayName("GET /history/stage-one/restaurants/{id}/votes — индивидуальные голоса")
	void stageOneRestaurantVotes_returnsPerUserVotes() {
		List<RoomHistoryStageOneVoteDTO> votesForA =
			historyService.getStageOneRestaurantVotes(ROOM_CODE, restaurantAId, "histuser2");
		assertThat(votesForA).hasSize(3);
		assertThat(votesForA).extracting("userDisplayName")
			.containsExactlyInAnyOrder("Участник 1", "Участник 2", "Участник 3");
		assertThat(votesForA).filteredOn("suitable", false).extracting("userDisplayName")
			.containsExactly("Участник 3");
	}

	@Test
	@DisplayName("GET /history/stage-two — финалисты с рангами и суммами")
	void stageTwo_returnsRanksAndSums() {
		List<RoomHistoryStageTwoRowDTO> rows = historyService.getStageTwo(ROOM_CODE, "histuser1");
		assertThat(rows).hasSize(2);
		assertThat(rows).extracting("restaurantId", "rankSum")
			.containsExactly(
				org.assertj.core.api.Assertions.tuple(restaurantAId, 6),
				org.assertj.core.api.Assertions.tuple(restaurantBId, 3)
			);
		assertThat(rows.get(0).ranks()).hasSize(3);
	}

	@Test
	@DisplayName("GET /rooms/me/history — краткий список FINISHED-комнат пользователя")
	void listMyFinishedRooms_returnsLightweightSummaries() {
		List<RoomHistorySummaryDTO> forOwner = historyService.listMyFinishedRoomSummaries("histuser1");
		assertThat(forOwner).hasSize(1);
		assertThat(forOwner.get(0).roomCode()).isEqualTo(ROOM_CODE);
		assertThat(forOwner.get(0).chosenRestaurantId()).isEqualTo(restaurantBId);
		assertThat(forOwner.get(0).winnerRestaurantName()).isEqualTo("Ресторан B");
		assertThat(forOwner.get(0).participantCount()).isEqualTo(3);
		assertThat(forOwner.get(0).viewerWasOwner()).isTrue();

		List<RoomHistorySummaryDTO> forMember = historyService.listMyFinishedRoomSummaries("histuser2");
		assertThat(forMember).hasSize(1);
		assertThat(forMember.get(0).viewerWasOwner()).isFalse();
	}

	@Test
	@DisplayName("Код комнаты нормализуется (trim + upper) при запросе истории")
	void overview_normalizesRoomCode() {
		RoomHistoryOverviewDTO overview = historyService.getOverview("  hist0001  ", "histuser1");
		assertThat(overview.roomCode()).isEqualTo(ROOM_CODE);
		assertThat(overview.winnerRestaurantName()).isEqualTo("Ресторан B");
	}

	@Test
	@DisplayName("В списке истории room без победителя возвращает null chosenRestaurantId")
	void listMyFinishedRooms_keepsNullWinnerFields() {
		Room roomWithoutWinner = new Room();
		roomWithoutWinner.setCode("HIST0002");
		roomWithoutWinner.setPasswordHash("{noop}room");
		roomWithoutWinner.setOwnerId(userIds.get(0));
		roomWithoutWinner.setState(RoomState.FINISHED);
		roomWithoutWinner.setParticipantCount(3);
		roomWithoutWinner.setStageOneParticipantCountSnapshot(3);
		roomWithoutWinner.setStageOneVoteRowsCount(0);
		roomWithoutWinner.setCenterLat(59.93);
		roomWithoutWinner.setCenterLon(30.31);
		roomWithoutWinner.setMaxDistanceMeters(2000);
		roomWithoutWinner.setOrganizerDoubleWeightApplied(false);
		roomWithoutWinner.setChosenRestaurantId(null);
		roomWithoutWinner.setWinnerSelectionPrinciple(WinnerSelectionPrinciple.NONE.name());
		Long roomWithoutWinnerId = roomRepository.save(roomWithoutWinner).getId();

		for (Long uid : userIds) {
			RoomParticipant rp = new RoomParticipant();
			rp.setRoomId(roomWithoutWinnerId);
			rp.setUserId(uid);
			rp.setOwner(uid.equals(userIds.get(0)));
			rp.setReady(true);
			rp.setFiltersConfirmed(true);
			roomParticipantRepository.save(rp);
		}

		List<RoomHistorySummaryDTO> summaries = historyService.listMyFinishedRoomSummaries("histuser2");
		RoomHistorySummaryDTO summary = summaries.stream()
			.filter(s -> s.roomCode().equals("HIST0002"))
			.findFirst()
			.orElseThrow();

		assertThat(summary.chosenRestaurantId()).isNull();
		assertThat(summary.winnerRestaurantName()).isNull();
	}

	@Test
	@DisplayName("Список истории для пользователя без FINISHED-комнат — пустой")
	void listMyFinishedRooms_emptyWhenNoFinishedParticipations() {
		User outsider = new User();
		outsider.setLogin("outsider_hist");
		outsider.setPasswordHash("{noop}unused");
		outsider.setDisplayName("Без комнат");
		userRepository.save(outsider);

		assertThat(historyService.listMyFinishedRoomSummaries("outsider_hist")).isEmpty();
	}

	@Test
	@DisplayName("Победитель ровно один на этапе 1 по строгому кворуму — STAGE_ONE_BASE_QUORUM_SINGLE")
	void detectsStageOneSingleWinner() {
		roomStageTwoRankRepository.deleteAll();
		roomStageOneFinalistRepository.deleteByRoomId(roomId);
		saveFinalist(restaurantBId, 3, 1, RoomStageOneFinalist.INCLUDED_BASE_QUORUM);
		Room room = roomRepository.findById(roomId).orElseThrow();
		room.setChosenRestaurantId(restaurantBId);
		room.setWinnerSelectionPrinciple(WinnerSelectionPrinciple.STAGE_ONE_BASE_QUORUM_SINGLE.name());
		roomRepository.save(room);

		RoomHistoryOverviewDTO overview = historyService.getOverview(ROOM_CODE, "histuser1");
		List<RoomHistoryStageTwoRowDTO> stageTwo = historyService.getStageTwo(ROOM_CODE, "histuser1");

		assertThat(stageTwo).isEmpty();
		assertThat(overview.winnerPrinciple())
			.isEqualTo(WinnerSelectionPrinciple.STAGE_ONE_BASE_QUORUM_SINGLE);
	}

	@Test
	@DisplayName("История недоступна, если комната ещё не FINISHED")
	void rejectsNonFinishedRoom() {
		Room room = roomRepository.findById(roomId).orElseThrow();
		room.setState(RoomState.STAGE_TWO);
		roomRepository.save(room);

		assertThatThrownBy(() -> historyService.getOverview(ROOM_CODE, "histuser1"))
			.isInstanceOf(RoomHistoryNotAvailableException.class);
		assertThatThrownBy(() -> historyService.getStageOne(ROOM_CODE, "histuser1"))
			.isInstanceOf(RoomHistoryNotAvailableException.class);
		assertThatThrownBy(() -> historyService.getStageTwo(ROOM_CODE, "histuser1"))
			.isInstanceOf(RoomHistoryNotAvailableException.class);
	}

	@Test
	@DisplayName("Не-участник не может смотреть историю комнаты")
	void rejectsNonParticipantViewer() {
		User outsider = new User();
		outsider.setLogin("outsider1");
		outsider.setPasswordHash("{noop}unused");
		outsider.setDisplayName("Чужак");
		userRepository.save(outsider);

		assertThatThrownBy(() -> historyService.getOverview(ROOM_CODE, "outsider1"))
			.isInstanceOf(RoomHistoryAccessDeniedException.class);
		assertThatThrownBy(() -> historyService.getParticipants(ROOM_CODE, "outsider1"))
			.isInstanceOf(RoomHistoryAccessDeniedException.class);
	}

	private void saveCandidate(int sortOrder, Long restaurantId) {
		RoomStageOneCandidate c = new RoomStageOneCandidate();
		c.setRoomId(roomId);
		c.setSortOrder(sortOrder);
		c.setRestaurantId(restaurantId);
		c.setSuitableCount(0);
		c.setSuitableWeightedCount(0);
		roomStageOneCandidateRepository.save(c);
	}

	private void saveVote(Long userId, Long restaurantId, boolean suitable) {
		RoomStageOneVote v = new RoomStageOneVote();
		v.setRoomId(roomId);
		v.setUserId(userId);
		v.setRestaurantId(restaurantId);
		v.setSuitable(suitable);
		roomStageOneVoteRepository.save(v);
	}

	private void saveFinalist(Long restaurantId, int approvalCount, int position, String includedBy) {
		RoomStageOneFinalist f = new RoomStageOneFinalist();
		f.setRoomId(roomId);
		f.setRestaurantId(restaurantId);
		f.setApprovalCount(approvalCount);
		f.setPosition(position);
		f.setIncludedBy(includedBy);
		roomStageOneFinalistRepository.save(f);
	}

	private void saveRank(Long userId, Long restaurantId, int rank) {
		RoomStageTwoRank r = new RoomStageTwoRank();
		r.setRoomId(roomId);
		r.setUserId(userId);
		r.setRestaurantId(restaurantId);
		r.setRankValue(rank);
		roomStageTwoRankRepository.save(r);
	}

	private static Restaurant sampleRestaurant(String name) {
		Restaurant r = new Restaurant();
		r.setName(name);
		r.setAddress("Адрес");
		r.setLatitude(59.93);
		r.setLongitude(30.31);
		r.setPhone("+7 900 000-00-00");
		r.setOpeningHours("10:00–22:00");
		r.setWebsiteUrl(null);
		return r;
	}

	private Room finishedRoomWithVotingTimestamps(
		String code,
		LocalDateTime stageOneStartedAt,
		LocalDateTime finishedAt
	) {
		Room room = new Room();
		room.setCode(code);
		room.setPasswordHash("{noop}room");
		room.setOwnerId(userIds.get(0));
		room.setState(RoomState.FINISHED);
		room.setParticipantCount(1);
		room.setStageOneParticipantCountSnapshot(1);
		room.setStageOneVoteRowsCount(0);
		room.setOrganizerDoubleWeightApplied(false);
		room.setChosenRestaurantId(restaurantAId);
		room.setWinnerSelectionPrinciple(WinnerSelectionPrinciple.NONE.name());
		room.setStageOneStartedAt(stageOneStartedAt);
		room.setFinishedAt(finishedAt);
		return roomRepository.save(room);
	}
}
