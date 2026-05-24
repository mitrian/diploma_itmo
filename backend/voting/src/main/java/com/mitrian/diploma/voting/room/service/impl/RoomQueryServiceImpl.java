package com.mitrian.diploma.voting.room.service.impl;

import com.mitrian.diploma.auth.entity.User;
import com.mitrian.diploma.auth.exception.UserNotFoundException;
import com.mitrian.diploma.auth.repository.UserRepository;
import com.mitrian.diploma.voting.room.filter.entity.KitchenTag;
import com.mitrian.diploma.voting.room.filter.entity.RoomKitchenTagSelection;
import com.mitrian.diploma.voting.room.filter.repository.KitchenTagRepository;
import com.mitrian.diploma.voting.room.filter.repository.RoomKitchenTagSelectionRepository;
import com.mitrian.diploma.voting.room.mapper.RoomDetailsMapper;
import com.mitrian.diploma.voting.catalog.mapper.RestaurantCardMapper;
import com.mitrian.diploma.voting.room.dto.ActiveRoomResponseDTO;
import com.mitrian.diploma.voting.room.dto.RoomDetailsResponseDTO;
import com.mitrian.diploma.voting.room.dto.RoomWinnerResponseDTO;
import com.mitrian.diploma.voting.room.entity.Room;
import com.mitrian.diploma.voting.room.entity.RoomParticipant;
import com.mitrian.diploma.voting.room.entity.RoomState;
import com.mitrian.diploma.voting.room.exception.NotRoomParticipantException;
import com.mitrian.diploma.voting.room.exception.RoomNotFoundException;
import com.mitrian.diploma.voting.room.repository.RoomParticipantRepository;
import com.mitrian.diploma.voting.room.repository.RoomRepository;
import com.mitrian.diploma.voting.room.service.RoomQueryService;
import com.mitrian.diploma.voting.room.util.RoomCodeHelper;
import com.mitrian.diploma.voting.stageone.dto.RestaurantCardDTO;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomQueryServiceImpl implements RoomQueryService {

	private final RoomRepository roomRepository;
	private final RoomParticipantRepository roomParticipantRepository;
	private final UserRepository userRepository;
	private final KitchenTagRepository kitchenTagRepository;
	private final RoomKitchenTagSelectionRepository roomKitchenTagSelectionRepository;
	private final RoomDetailsMapper roomDetailsMapper;
	private final RestaurantCardMapper restaurantCardMapper;

	public RoomQueryServiceImpl(
		RoomRepository roomRepository,
		RoomParticipantRepository roomParticipantRepository,
		UserRepository userRepository,
		KitchenTagRepository kitchenTagRepository,
		RoomKitchenTagSelectionRepository roomKitchenTagSelectionRepository,
		RoomDetailsMapper roomDetailsMapper,
		RestaurantCardMapper restaurantCardMapper
	) {
		this.roomRepository = roomRepository;
		this.roomParticipantRepository = roomParticipantRepository;
		this.userRepository = userRepository;
		this.kitchenTagRepository = kitchenTagRepository;
		this.roomKitchenTagSelectionRepository = roomKitchenTagSelectionRepository;
		this.roomDetailsMapper = roomDetailsMapper;
		this.restaurantCardMapper = restaurantCardMapper;
	}

	@Override
	@Transactional(readOnly = true)
	public RoomDetailsResponseDTO getRoomDetails(String roomCode, String userLogin) {
		User user = userRepository.findByLogin(userLogin)
			.orElseThrow(() -> new UserNotFoundException("User not found"));

		String normalizedCode = RoomCodeHelper.normalize(roomCode);
		Room room = roomRepository.findByCode(normalizedCode)
			.orElseThrow(() -> new RoomNotFoundException("Room not found"));

		return buildRoomDetailsForViewer(room, user);
	}

	@Override
	@Transactional(readOnly = true)
	public RoomWinnerResponseDTO getRoomWinner(String roomCode, String userLogin) {
		User user = userRepository.findByLogin(userLogin)
			.orElseThrow(() -> new UserNotFoundException("User not found"));

		String normalizedCode = RoomCodeHelper.normalize(roomCode);
		Room room = roomRepository.findByCode(normalizedCode)
			.orElseThrow(() -> new RoomNotFoundException("Room not found"));

		requireParticipant(room.getId(), user.getId());
		RestaurantCardDTO winnerRestaurant = restaurantCardMapper.toCardByRestaurantId(room.getChosenRestaurantId());
		return new RoomWinnerResponseDTO(room.getState(), room.getChosenRestaurantId(), winnerRestaurant);
	}

	@Override
	@Transactional(readOnly = true)
	public ActiveRoomResponseDTO getMyActiveRoom(String userLogin) {
		User user = userRepository.findByLogin(userLogin)
			.orElseThrow(() -> new UserNotFoundException("User not found"));
		List<String> codes = roomParticipantRepository.findActiveRoomCodesForUser(user.getId(), RoomState.FINISHED);
		String code = codes.isEmpty() ? null : codes.get(0);
		return new ActiveRoomResponseDTO(code);
	}

	private RoomDetailsResponseDTO buildRoomDetailsForViewer(Room room, User viewer) {
		requireParticipant(room.getId(), viewer.getId());

		List<RoomParticipant> rows = roomParticipantRepository.findByRoomIdOrderByIdAsc(room.getId());
		List<Long> userIds = rows.stream().map(RoomParticipant::getUserId).distinct().toList();
		Map<Long, User> usersById = userRepository.findAllById(userIds).stream()
			.collect(Collectors.toMap(User::getId, Function.identity()));

		List<RoomKitchenTagSelection> roomSelections = roomKitchenTagSelectionRepository.findByRoomId(room.getId());
		Set<Long> unionTagIds = roomSelections.stream()
			.map(RoomKitchenTagSelection::getKitchenTagId)
			.collect(Collectors.toSet());
		Map<Long, KitchenTag> tagsById = kitchenTagRepository.findAllById(unionTagIds).stream()
			.collect(Collectors.toMap(KitchenTag::getId, Function.identity()));
		return roomDetailsMapper.toRoomDetails(room, viewer, rows, usersById, roomSelections, tagsById);
	}

	private RoomParticipant requireParticipant(Long roomId, Long userId) {
		return roomParticipantRepository.findByRoomIdAndUserId(roomId, userId)
			.orElseThrow(() -> new NotRoomParticipantException("User is not a participant of this room"));
	}

}
