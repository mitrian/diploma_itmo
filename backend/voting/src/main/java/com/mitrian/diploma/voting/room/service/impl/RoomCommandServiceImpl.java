package com.mitrian.diploma.voting.room.service.impl;

import com.mitrian.diploma.auth.entity.User;
import com.mitrian.diploma.auth.exception.UserNotFoundException;
import com.mitrian.diploma.auth.repository.UserRepository;
import com.mitrian.diploma.voting.room.filter.repository.RoomKitchenTagSelectionRepository;
import com.mitrian.diploma.voting.room.dto.CreateRoomRequestDTO;
import com.mitrian.diploma.voting.room.dto.CreateRoomResponseDTO;
import com.mitrian.diploma.voting.room.dto.JoinRoomRequestDTO;
import com.mitrian.diploma.voting.room.dto.JoinRoomResponseDTO;
import com.mitrian.diploma.voting.room.dto.RoomDetailsResponseDTO;
import com.mitrian.diploma.voting.room.dto.SetRoomReadyRequestDTO;
import com.mitrian.diploma.voting.room.entity.Room;
import com.mitrian.diploma.voting.room.entity.RoomParticipant;
import com.mitrian.diploma.voting.room.entity.RoomState;
import com.mitrian.diploma.voting.room.exception.ActiveRoomMembershipException;
import com.mitrian.diploma.voting.room.exception.AlreadyRoomParticipantException;
import com.mitrian.diploma.voting.room.exception.NotRoomParticipantException;
import com.mitrian.diploma.voting.room.exception.OnlyRoomOwnerException;
import com.mitrian.diploma.voting.room.exception.RoomFinishedException;
import com.mitrian.diploma.voting.room.exception.RoomLeaveNotAllowedDuringVotingException;
import com.mitrian.diploma.voting.room.exception.RoomLeaveNotAllowedWhileAwaitingStartException;
import com.mitrian.diploma.voting.room.exception.RoomNotFoundException;
import com.mitrian.diploma.voting.room.exception.RoomNotJoinableException;
import com.mitrian.diploma.voting.room.exception.RoomPasswordMismatchException;
import com.mitrian.diploma.voting.room.exception.RoomReadinessNotAllowedException;
import com.mitrian.diploma.voting.room.exception.RoomStartNotAllowedException;
import com.mitrian.diploma.voting.catalog.entity.Restaurant;
import com.mitrian.diploma.voting.catalog.service.CandidateSelectionService;
import com.mitrian.diploma.voting.room.repository.RoomParticipantRepository;
import com.mitrian.diploma.voting.room.repository.RoomRepository;
import com.mitrian.diploma.voting.room.service.RoomCommandService;
import com.mitrian.diploma.voting.room.service.RoomQueryService;
import com.mitrian.diploma.voting.room.util.RoomCodeGenerator;
import com.mitrian.diploma.voting.room.util.RoomCodeHelper;
import com.mitrian.diploma.voting.stageone.entity.RoomStageOneCandidate;
import com.mitrian.diploma.voting.stageone.repository.RoomStageOneCandidateRepository;
import com.mitrian.diploma.voting.timeout.VotingTimeoutProperties;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomCommandServiceImpl implements RoomCommandService {

	private static final int MAX_PARTICIPANTS = 6;

	private final RoomRepository roomRepository;
	private final RoomParticipantRepository roomParticipantRepository;
	private final RoomKitchenTagSelectionRepository roomKitchenTagSelectionRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final RoomCodeGenerator roomCodeGenerator;
	private final CandidateSelectionService candidateSelectionService;
	private final RoomStageOneCandidateRepository roomStageOneCandidateRepository;
	private final VotingTimeoutProperties votingTimeoutProperties;
	private final RoomQueryService roomQueryService;

	public RoomCommandServiceImpl(
		RoomRepository roomRepository,
		RoomParticipantRepository roomParticipantRepository,
		RoomKitchenTagSelectionRepository roomKitchenTagSelectionRepository,
		UserRepository userRepository,
		PasswordEncoder passwordEncoder,
		RoomCodeGenerator roomCodeGenerator,
		CandidateSelectionService candidateSelectionService,
		RoomStageOneCandidateRepository roomStageOneCandidateRepository,
		VotingTimeoutProperties votingTimeoutProperties,
		RoomQueryService roomQueryService
	) {
		this.roomRepository = roomRepository;
		this.roomParticipantRepository = roomParticipantRepository;
		this.roomKitchenTagSelectionRepository = roomKitchenTagSelectionRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.roomCodeGenerator = roomCodeGenerator;
		this.candidateSelectionService = candidateSelectionService;
		this.roomStageOneCandidateRepository = roomStageOneCandidateRepository;
		this.votingTimeoutProperties = votingTimeoutProperties;
		this.roomQueryService = roomQueryService;
	}

	@Override
	@Transactional
	public CreateRoomResponseDTO createRoom(CreateRoomRequestDTO request, String ownerLogin) {
		User owner = userRepository.findByLogin(ownerLogin)
			.orElseThrow(() -> new UserNotFoundException("User not found"));

		if (roomParticipantRepository.countParticipationsInNonFinishedRooms(owner.getId(), RoomState.FINISHED) > 0) {
			throw new ActiveRoomMembershipException("User already participates in an active room session");
		}

		Room room = new Room();
		room.setCode(roomCodeGenerator.generateUniqueCode());
		room.setPasswordHash(passwordEncoder.encode(request.roomPassword()));
		room.setOwnerId(owner.getId());
		room.setState(RoomState.LOBBY);
		room.setParticipantCount(0);
		room.setStageOneParticipantCountSnapshot(null);
		room.setStageOneVoteRowsCount(0);

		Room saved = roomRepository.save(room);
		addParticipant(saved.getId(), owner.getId(), true);

		return new CreateRoomResponseDTO(
			saved.getId(),
			saved.getCode(),
			saved.getState(),
			saved.getCreatedAt()
		);
	}

	@Override
	@Transactional
	public JoinRoomResponseDTO joinRoom(JoinRoomRequestDTO request, String userLogin) {
		User user = userRepository.findByLogin(userLogin)
			.orElseThrow(() -> new UserNotFoundException("User not found"));

		String normalizedCode = RoomCodeHelper.normalize(request.code());
		Room room = roomRepository.findByCode(normalizedCode)
			.orElseThrow(() -> new RoomNotFoundException("Room not found"));

		if (room.getState() != RoomState.LOBBY) {
			throw new RoomNotJoinableException("Room is not accepting new participants");
		}
		if (!passwordEncoder.matches(request.roomPassword(), room.getPasswordHash())) {
			throw new RoomPasswordMismatchException("Invalid room password");
		}
		if (room.getParticipantCount() >= MAX_PARTICIPANTS) {
			throw new RoomNotJoinableException("Room participant limit reached");
		}
		if (roomParticipantRepository.existsByRoomIdAndUserId(room.getId(), user.getId())) {
			throw new AlreadyRoomParticipantException("Already a participant of this room");
		}
		if (roomParticipantRepository.countParticipationsInNonFinishedRoomsExcept(
			user.getId(), RoomState.FINISHED, room.getId()
		) > 0) {
			throw new ActiveRoomMembershipException("User already participates in another active room session");
		}
		addParticipant(room.getId(), user.getId(), false);
		return new JoinRoomResponseDTO(room.getId(), room.getCode(), room.getState());
	}

	@Override
	@Transactional
	public void leaveRoom(String roomCode, String userLogin) {
		User user = userRepository.findByLogin(userLogin)
			.orElseThrow(() -> new UserNotFoundException("User not found"));

		String normalizedCode = RoomCodeHelper.normalize(roomCode);
		Room room = roomRepository.findByCode(normalizedCode)
			.orElseThrow(() -> new RoomNotFoundException("Room not found"));
		Long roomId = room.getId();

		switch (room.getState()) {
			case FINISHED:
				throw new RoomFinishedException("Cannot leave: room session is already finished");
			case GEO_FILTERS:
			case AWAITING_START:
				throw new RoomLeaveNotAllowedWhileAwaitingStartException("Cannot leave: room is in pre-start phase");
			case STAGE_ONE:
			case STAGE_TWO:
				throw new RoomLeaveNotAllowedDuringVotingException(
					"Cannot leave: voting is in progress (stage one or two)"
				);
			case LOBBY:
				break;
		}

		RoomParticipant membership = requireParticipant(roomId, user.getId());
		if (room.getOwnerId().equals(user.getId())) {
			roomRepository.delete(room);
			return;
		}

		roomParticipantRepository.delete(membership);
		room.setParticipantCount(Math.max(0, room.getParticipantCount() - 1));
		roomRepository.save(room);
		Room reloaded = roomRepository.findById(roomId).orElseThrow();
		revertAwaitingStartIfIncomplete(reloaded);
	}

	@Override
	@Transactional
	public RoomDetailsResponseDTO setParticipantReady(String roomCode, String userLogin, SetRoomReadyRequestDTO request) {
		User user = userRepository.findByLogin(userLogin)
			.orElseThrow(() -> new UserNotFoundException("User not found"));

		String normalizedCode = RoomCodeHelper.normalize(roomCode);
		Room room = roomRepository.findByCode(normalizedCode)
			.orElseThrow(() -> new RoomNotFoundException("Room not found"));

		requireState(
			room,
			RoomState.LOBBY,
			new RoomReadinessNotAllowedException("Readiness can only be changed while the room is in lobby")
		);

		RoomParticipant membership = requireParticipant(room.getId(), user.getId());
		membership.setReady(request.ready());
		roomParticipantRepository.save(membership);
		roomParticipantRepository.flush();

		Room after = roomRepository.findById(room.getId()).orElseThrow();
		tryPromoteLobbyToGeoFilters(after);
		return roomQueryService.getRoomDetails(after.getCode(), userLogin);
	}

	@Override
	@Transactional
	public RoomDetailsResponseDTO startSession(String roomCode, String userLogin) {
		User user = userRepository.findByLogin(userLogin)
			.orElseThrow(() -> new UserNotFoundException("User not found"));
		String normalizedCode = RoomCodeHelper.normalize(roomCode);
		Room room = roomRepository.findByCode(normalizedCode)
			.orElseThrow(() -> new RoomNotFoundException("Room not found"));

		Room refreshed = startSessionInternal(room, user.getId());
		return roomQueryService.getRoomDetails(refreshed.getCode(), userLogin);
	}

	private Room startSessionInternal(Room room, Long userId) {
		requireOwner(room, userId);
		requireAwaitingStart(room);
		if (roomParticipantRepository.existsByRoomIdAndFiltersConfirmedFalse(room.getId())) {
			throw new RoomStartNotAllowedException("Not all participants confirmed kitchen filters");
		}

		List<Restaurant> candidates = candidateSelectionService.findCandidatesForRoom(room.getId());
		if (candidates.isEmpty()) {
			resetRoomToGeoFiltersClearingFilters(room);
			return roomRepository.findById(room.getId()).orElseThrow();
		}

		room.setState(RoomState.STAGE_ONE);
		room.setStageOneStartedAt(LocalDateTime.now());
		room.setFinishedAt(null);
		room.setWinnerSelectionPrinciple(null);
		room.setStageOneParticipantCountSnapshot(room.getParticipantCount());
		room.setStageOneVoteRowsCount(0);
		room.setStageOneTimeoutAt(LocalDateTime.now().plusSeconds(votingTimeoutProperties.getStageOneTimeoutSeconds()));
		room.setStageOneTimeoutProcessed(false);
		room.setStageTwoTimeoutAt(null);
		room.setStageTwoTimeoutProcessed(false);
		roomRepository.save(room);

		List<RoomStageOneCandidate> rows = new ArrayList<>();
		int sortOrder = 0;
		for (Restaurant r : candidates) {
			RoomStageOneCandidate row = new RoomStageOneCandidate();
			row.setRoomId(room.getId());
			row.setSortOrder(sortOrder++);
			row.setRestaurantId(r.getId());
			rows.add(row);
		}
		roomStageOneCandidateRepository.saveAll(rows);
		return roomRepository.findById(room.getId()).orElseThrow();
	}

	private void tryPromoteLobbyToGeoFilters(Room room) {
		if (room.getState() != RoomState.LOBBY) {
			return;
		}
		long participantCount = room.getParticipantCount();
		if (participantCount == 0) {
			return;
		}
		if (roomParticipantRepository.existsByRoomIdAndReadyIsFalse(room.getId())) {
			return;
		}
		room.setState(RoomState.GEO_FILTERS);
		roomRepository.save(room);
	}

	private void revertAwaitingStartIfIncomplete(Room room) {
		if (room.getState() != RoomState.AWAITING_START && room.getState() != RoomState.GEO_FILTERS) {
			return;
		}
		long participantCount = room.getParticipantCount();
		if (participantCount == 0 || roomParticipantRepository.existsByRoomIdAndReadyIsFalse(room.getId())) {
			roomKitchenTagSelectionRepository.deleteByRoomId(room.getId());
			roomParticipantRepository.clearFiltersConfirmedForRoom(room.getId());
			roomParticipantRepository.clearReadyFlagsForRoom(room.getId());
			room.setStageOneTimeoutAt(null);
			room.setStageOneTimeoutProcessed(false);
			room.setStageTwoTimeoutAt(null);
			room.setStageTwoTimeoutProcessed(false);
			room.setStageOneStartedAt(null);
			room.setFinishedAt(null);
			room.setState(RoomState.LOBBY);
			roomRepository.save(room);
		}
	}

	private void resetRoomToGeoFiltersClearingFilters(Room room) {
		roomKitchenTagSelectionRepository.deleteByRoomId(room.getId());
		room.setCenterLat(null);
		room.setCenterLon(null);
		room.setMaxDistanceMeters(null);
		room.setWinnerSelectionPrinciple(null);
		room.setStageOneTimeoutAt(null);
		room.setStageOneTimeoutProcessed(false);
		room.setStageTwoTimeoutAt(null);
		room.setStageTwoTimeoutProcessed(false);
		room.setStageOneStartedAt(null);
		room.setFinishedAt(null);
		room.setState(RoomState.GEO_FILTERS);
		roomRepository.save(room);
		roomParticipantRepository.clearFiltersConfirmedForRoom(room.getId());
	}

	private void addParticipant(Long roomId, Long userId, boolean owner) {
		RoomParticipant p = new RoomParticipant();
		p.setRoomId(roomId);
		p.setUserId(userId);
		p.setOwner(owner);
		p.setReady(false);
		p.setFiltersConfirmed(false);
		roomParticipantRepository.save(p);
		Room room = roomRepository.findById(roomId).orElseThrow();
		room.setParticipantCount(room.getParticipantCount() + 1);
		roomRepository.save(room);
	}

	private RoomParticipant requireParticipant(Long roomId, Long userId) {
		return roomParticipantRepository.findByRoomIdAndUserId(roomId, userId)
			.orElseThrow(() -> new NotRoomParticipantException("User is not a participant of this room"));
	}

	private static void requireState(Room room, RoomState expectedState, RuntimeException ex) {
		if (room.getState() != expectedState) {
			throw ex;
		}
	}

	private static void requireOwner(Room room, Long userId) {
		if (!room.getOwnerId().equals(userId)) {
			throw new OnlyRoomOwnerException("Only the room owner can start the session");
		}
	}

	private static void requireAwaitingStart(Room room) {
		if (room.getState() != RoomState.AWAITING_START) {
			throw new RoomStartNotAllowedException("Room is not awaiting start");
		}
	}
}
