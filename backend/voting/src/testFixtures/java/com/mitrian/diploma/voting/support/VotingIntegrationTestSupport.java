package com.mitrian.diploma.voting.support;

import com.mitrian.diploma.auth.entity.User;
import com.mitrian.diploma.auth.repository.UserRepository;
import com.mitrian.diploma.voting.catalog.entity.Restaurant;
import com.mitrian.diploma.voting.catalog.entity.RestaurantKitchenTag;
import com.mitrian.diploma.voting.catalog.repository.RestaurantKitchenTagRepository;
import com.mitrian.diploma.voting.catalog.repository.RestaurantRepository;
import com.mitrian.diploma.voting.room.dto.CreateRoomRequestDTO;
import com.mitrian.diploma.voting.room.dto.JoinRoomRequestDTO;
import com.mitrian.diploma.voting.room.dto.SetRoomReadyRequestDTO;
import com.mitrian.diploma.voting.room.entity.Room;
import com.mitrian.diploma.voting.room.entity.RoomState;
import com.mitrian.diploma.voting.room.dto.AddRoomKitchenTagsRequestDTO;
import com.mitrian.diploma.voting.room.filter.entity.KitchenTag;
import com.mitrian.diploma.voting.room.filter.repository.KitchenTagRepository;
import com.mitrian.diploma.voting.room.filter.service.RoomFilterService;
import com.mitrian.diploma.voting.room.dto.SetRoomGeoFilterRequestDTO;
import com.mitrian.diploma.voting.room.repository.RoomRepository;
import com.mitrian.diploma.voting.room.service.RoomCommandService;
import com.mitrian.diploma.voting.stageone.dto.StageOneVoteSuitableRequestDTO;
import com.mitrian.diploma.voting.stageone.entity.RoomStageOneCandidate;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneCandidateRepository;
import com.mitrian.diploma.voting.stageone.service.StageOneService;
import com.mitrian.diploma.voting.timeout.VotingTimeoutProperties;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class VotingIntegrationTestSupport {

	public static final String ROOM_PASSWORD = "room-pass";
	public static final String KITCHEN_SLUG = "italian";
	public static final double CENTER_LAT = 59.93;
	public static final double CENTER_LON = 30.31;

	private final UserRepository userRepository;
	private final KitchenTagRepository kitchenTagRepository;
	private final RestaurantRepository restaurantRepository;
	private final RestaurantKitchenTagRepository restaurantKitchenTagRepository;
	private final RoomRepository roomRepository;
	private final RoomCommandService roomCommandService;
	private final RoomFilterService roomFilterService;
	private final StageOneService stageOneService;
	private final RoomStageOneCandidateRepository roomStageOneCandidateRepository;
	private final PasswordEncoder passwordEncoder;
	private final VotingTimeoutProperties votingTimeoutProperties;

	public VotingIntegrationTestSupport(
		UserRepository userRepository,
		KitchenTagRepository kitchenTagRepository,
		RestaurantRepository restaurantRepository,
		RestaurantKitchenTagRepository restaurantKitchenTagRepository,
		RoomRepository roomRepository,
		RoomCommandService roomCommandService,
		RoomFilterService roomFilterService,
		StageOneService stageOneService,
		RoomStageOneCandidateRepository roomStageOneCandidateRepository,
		PasswordEncoder passwordEncoder,
		VotingTimeoutProperties votingTimeoutProperties
	) {
		this.userRepository = userRepository;
		this.kitchenTagRepository = kitchenTagRepository;
		this.restaurantRepository = restaurantRepository;
		this.restaurantKitchenTagRepository = restaurantKitchenTagRepository;
		this.roomRepository = roomRepository;
		this.roomCommandService = roomCommandService;
		this.roomFilterService = roomFilterService;
		this.stageOneService = stageOneService;
		this.roomStageOneCandidateRepository = roomStageOneCandidateRepository;
		this.passwordEncoder = passwordEncoder;
		this.votingTimeoutProperties = votingTimeoutProperties;
	}

	public record StartedRoomContext(
		String roomCode,
		String ownerLogin,
		List<String> memberLogins,
		List<Long> restaurantIds
	) {
		public List<String> allLogins() {
			List<String> all = new ArrayList<>();
			all.add(ownerLogin);
			all.addAll(memberLogins);
			return all;
		}
	}

	public User createUser(String login) {
		User user = new User();
		user.setLogin(login);
		user.setDisplayName(login);
		user.setPasswordHash(passwordEncoder.encode("user-pass"));
		return userRepository.save(user);
	}

	public KitchenTag ensureKitchenTag() {
		return kitchenTagRepository.findBySlug(KITCHEN_SLUG).orElseGet(() -> {
			KitchenTag tag = new KitchenTag();
			tag.setSlug(KITCHEN_SLUG);
			tag.setLabelRu("итальянская");
			return kitchenTagRepository.save(tag);
		});
	}

	public Restaurant createRestaurantNearCenter(String name) {
		Restaurant restaurant = new Restaurant();
		restaurant.setName(name);
		restaurant.setAddress("Санкт-Петербург, тест");
		restaurant.setLatitude(CENTER_LAT);
		restaurant.setLongitude(CENTER_LON);
		restaurant.setPhone("+7 900 000-00-00");
		restaurant.setOpeningHours("10:00–22:00");
		restaurant.setWebsiteUrl(null);
		restaurant = restaurantRepository.save(restaurant);

		KitchenTag tag = ensureKitchenTag();
		RestaurantKitchenTag link = new RestaurantKitchenTag();
		link.setRestaurantId(restaurant.getId());
		link.setKitchenTagId(tag.getId());
		restaurantKitchenTagRepository.save(link);
		return restaurant;
	}

	public StartedRoomContext startStageOneWithParticipants(int participantCount, int restaurantCount) {
		if (participantCount < 1 || participantCount > 6) {
			throw new IllegalArgumentException("participantCount must be 1..6");
		}
		String ownerLogin = "owner-it";
		createUser(ownerLogin);
		List<String> memberLogins = new ArrayList<>();
		for (int i = 1; i < participantCount; i++) {
			String login = "member-it-" + i;
			createUser(login);
			memberLogins.add(login);
		}

		List<Long> restaurantIds = new ArrayList<>();
		for (int i = 0; i < restaurantCount; i++) {
			restaurantIds.add(createRestaurantNearCenter("Ресторан " + (i + 1)).getId());
		}

		String roomCode = roomCommandService
			.createRoom(new CreateRoomRequestDTO(ROOM_PASSWORD), ownerLogin)
			.code();
		for (String member : memberLogins) {
			roomCommandService.joinRoom(
				new JoinRoomRequestDTO(roomCode, ROOM_PASSWORD),
				member
			);
		}

		for (String login : allLogins(ownerLogin, memberLogins)) {
			roomCommandService.setParticipantReady(roomCode, login, new SetRoomReadyRequestDTO(true));
		}

		roomFilterService.setRoomGeoFilter(
			roomCode,
			ownerLogin,
			new SetRoomGeoFilterRequestDTO(CENTER_LAT, CENTER_LON, 5_000)
		);
		roomFilterService.confirmRoomGeoFilter(roomCode, ownerLogin);

		for (String login : allLogins(ownerLogin, memberLogins)) {
			roomFilterService.addRoomKitchenTags(
				roomCode,
				login,
				new AddRoomKitchenTagsRequestDTO(List.of(KITCHEN_SLUG))
			);
			roomFilterService.confirmRoomKitchenFilters(roomCode, login);
		}

		roomCommandService.startSession(roomCode, ownerLogin);
		return new StartedRoomContext(roomCode, ownerLogin, memberLogins, restaurantIds);
	}

	public List<RoomStageOneCandidate> candidates(String roomCode) {
		Room room = roomRepository.findByCode(roomCode).orElseThrow();
		return roomStageOneCandidateRepository.findByRoomIdOrderBySortOrderAsc(room.getId());
	}

	public void voteAllCandidates(String roomCode, String userLogin, boolean suitable) {
		for (RoomStageOneCandidate candidate : candidates(roomCode)) {
			if (reloadRoom(roomCode).getState() != RoomState.STAGE_ONE) {
				return;
			}
			stageOneService.vote(
				roomCode,
				userLogin,
				candidate.getRestaurantId(),
				new StageOneVoteSuitableRequestDTO(suitable)
			);
		}
	}

	public void voteAllCandidatesForAllUsers(StartedRoomContext ctx, boolean suitable) {
		for (String login : ctx.allLogins()) {
			if (reloadRoom(ctx.roomCode()).getState() != RoomState.STAGE_ONE) {
				break;
			}
			voteAllCandidates(ctx.roomCode(), login, suitable);
		}
	}

	public Room reloadRoom(String roomCode) {
		return roomRepository.findByCode(roomCode).orElseThrow();
	}

	public long stageOneTimeoutSeconds() {
		return votingTimeoutProperties.getStageOneTimeoutSeconds();
	}

	private static List<String> allLogins(String ownerLogin, List<String> memberLogins) {
		List<String> all = new ArrayList<>();
		all.add(ownerLogin);
		all.addAll(memberLogins);
		return all;
	}
}
