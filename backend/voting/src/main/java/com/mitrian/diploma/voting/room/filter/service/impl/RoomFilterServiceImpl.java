package com.mitrian.diploma.voting.room.filter.service.impl;

import com.mitrian.diploma.auth.entity.User;
import com.mitrian.diploma.auth.exception.UserNotFoundException;
import com.mitrian.diploma.auth.repository.UserRepository;
import com.mitrian.diploma.voting.room.dto.AddRoomKitchenTagsRequestDTO;
import com.mitrian.diploma.voting.room.dto.RoomDetailsResponseDTO;
import com.mitrian.diploma.voting.room.dto.SetRoomGeoFilterRequestDTO;
import com.mitrian.diploma.voting.room.entity.Room;
import com.mitrian.diploma.voting.room.entity.RoomParticipant;
import com.mitrian.diploma.voting.room.entity.RoomState;
import com.mitrian.diploma.voting.room.exception.NotRoomParticipantException;
import com.mitrian.diploma.voting.room.exception.OnlyRoomOwnerException;
import com.mitrian.diploma.voting.room.exception.RoomGeoFilterNotAllowedException;
import com.mitrian.diploma.voting.room.exception.RoomKitchenTagsNotAllowedException;
import com.mitrian.diploma.voting.room.exception.RoomNotFoundException;
import com.mitrian.diploma.voting.room.exception.RoomStartNotAllowedException;
import com.mitrian.diploma.voting.room.filter.dto.KitchenTagDTO;
import com.mitrian.diploma.voting.room.filter.entity.KitchenTag;
import com.mitrian.diploma.voting.room.filter.entity.RoomKitchenTagSelection;
import com.mitrian.diploma.voting.room.filter.exception.InvalidKitchenTagSlugException;
import com.mitrian.diploma.voting.room.filter.repository.KitchenTagRepository;
import com.mitrian.diploma.voting.room.filter.repository.RoomKitchenTagSelectionRepository;
import com.mitrian.diploma.voting.room.filter.service.RoomFilterService;
import com.mitrian.diploma.voting.room.repository.RoomParticipantRepository;
import com.mitrian.diploma.voting.room.repository.RoomRepository;
import com.mitrian.diploma.voting.room.service.RoomService;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomFilterServiceImpl implements RoomFilterService {

	private final UserRepository userRepository;
	private final RoomRepository roomRepository;
	private final RoomParticipantRepository roomParticipantRepository;
	private final KitchenTagRepository kitchenTagRepository;
	private final RoomKitchenTagSelectionRepository roomKitchenTagSelectionRepository;
	private final RoomService roomService;

	public RoomFilterServiceImpl(
		UserRepository userRepository,
		RoomRepository roomRepository,
		RoomParticipantRepository roomParticipantRepository,
		KitchenTagRepository kitchenTagRepository,
		RoomKitchenTagSelectionRepository roomKitchenTagSelectionRepository,
		RoomService roomService
	) {
		this.userRepository = userRepository;
		this.roomRepository = roomRepository;
		this.roomParticipantRepository = roomParticipantRepository;
		this.kitchenTagRepository = kitchenTagRepository;
		this.roomKitchenTagSelectionRepository = roomKitchenTagSelectionRepository;
		this.roomService = roomService;
	}

	@Override
	@Transactional
	public RoomDetailsResponseDTO setRoomGeoFilter(String roomCode, String userLogin, SetRoomGeoFilterRequestDTO request) {
		User user = userRepository.findByLogin(userLogin)
			.orElseThrow(() -> new UserNotFoundException("User not found"));
		Room room = loadRoomForCode(roomCode);
		if (!room.getOwnerId().equals(user.getId())) {
			throw new OnlyRoomOwnerException("Only the room owner can set geo filter");
		}
		if (room.getState() != RoomState.GEO_FILTERS) {
			throw new RoomGeoFilterNotAllowedException(
				"Geo filter can only be set during GEO_FILTERS stage"
			);
		}

		room.setCenterLat(request.centerLat());
		room.setCenterLon(request.centerLon());
		room.setMaxDistanceMeters(request.maxDistanceMeters());
		roomRepository.save(room);
		return roomService.getRoomDetails(roomCode, userLogin);
	}

	@Override
	@Transactional
	public RoomDetailsResponseDTO confirmRoomGeoFilter(String roomCode, String userLogin) {
		User user = userRepository.findByLogin(userLogin)
			.orElseThrow(() -> new UserNotFoundException("User not found"));
		Room room = loadRoomForCode(roomCode);
		if (!room.getOwnerId().equals(user.getId())) {
			throw new OnlyRoomOwnerException("Only the room owner can confirm geo filter");
		}
		if (room.getState() != RoomState.GEO_FILTERS) {
			throw new RoomGeoFilterNotAllowedException(
				"Geo filter can only be confirmed during GEO_FILTERS stage"
			);
		}
		if (room.getCenterLat() == null || room.getCenterLon() == null || room.getMaxDistanceMeters() == null) {
			throw new RoomStartNotAllowedException("Geo filter is not configured");
		}

		room.setState(RoomState.AWAITING_START);
		roomRepository.save(room);
		return roomService.getRoomDetails(roomCode, userLogin);
	}

	@Override
	@Transactional(readOnly = true)
	public List<KitchenTagDTO> listKitchenTagsCatalog() {
		return kitchenTagRepository.findAllByOrderByIdAsc().stream()
			.map(this::toKitchenTagDto)
			.toList();
	}

	@Override
	@Transactional
	public RoomDetailsResponseDTO addRoomKitchenTags(
		String roomCode,
		String userLogin,
		AddRoomKitchenTagsRequestDTO request
	) {
		User user = userRepository.findByLogin(userLogin)
			.orElseThrow(() -> new UserNotFoundException("User not found"));
		Room room = loadRoomForCode(roomCode);
		RoomParticipant membership = roomParticipantRepository
			.findByRoomIdAndUserId(room.getId(), user.getId())
			.orElseThrow(() -> new NotRoomParticipantException("User is not a participant of this room"));

		assertKitchenFiltersEditable(room);
		LinkedHashSet<String> wanted = normalizeSlugSet(request.slugs());
		if (wanted.isEmpty()) {
			return roomService.getRoomDetails(roomCode, userLogin);
		}

		List<KitchenTag> tags = resolveKitchenTagsBySlugs(wanted);
		boolean anyInserted = false;
		for (KitchenTag tag : tags) {
			if (roomKitchenTagSelectionRepository.findByRoomIdAndKitchenTagId(room.getId(), tag.getId()).isPresent()) {
				continue;
			}
			RoomKitchenTagSelection row = new RoomKitchenTagSelection();
			row.setRoomId(room.getId());
			row.setUserId(user.getId());
			row.setKitchenTagId(tag.getId());
			roomKitchenTagSelectionRepository.save(row);
			anyInserted = true;
		}

		if (anyInserted) {
			roomParticipantRepository.clearFiltersConfirmedForRoom(room.getId());
			membership.setFiltersConfirmed(false);
			roomParticipantRepository.save(membership);
		}

		return roomService.getRoomDetails(roomCode, userLogin);
	}

	@Override
	@Transactional
	public RoomDetailsResponseDTO removeRoomKitchenTag(String roomCode, String userLogin, String tagSlug) {
		User user = userRepository.findByLogin(userLogin)
			.orElseThrow(() -> new UserNotFoundException("User not found"));
		Room room = loadRoomForCode(roomCode);
		RoomParticipant membership = roomParticipantRepository
			.findByRoomIdAndUserId(room.getId(), user.getId())
			.orElseThrow(() -> new NotRoomParticipantException("User is not a participant of this room"));

		assertKitchenFiltersEditable(room);
		String normalizedSlug = normalizeSlug(tagSlug);
		KitchenTag tag = kitchenTagRepository.findBySlug(normalizedSlug)
			.orElseThrow(() -> new InvalidKitchenTagSlugException("Unknown kitchen tag slug: " + normalizedSlug));
		RoomKitchenTagSelection selection = roomKitchenTagSelectionRepository
			.findByRoomIdAndKitchenTagId(room.getId(), tag.getId())
			.orElseThrow(() -> new RoomKitchenTagsNotAllowedException("This kitchen tag is not claimed in the room"));
		if (!selection.getUserId().equals(user.getId())) {
			throw new RoomKitchenTagsNotAllowedException("Only the participant who claimed this tag can remove it");
		}

		roomKitchenTagSelectionRepository.delete(selection);
		roomParticipantRepository.clearFiltersConfirmedForRoom(room.getId());
		membership.setFiltersConfirmed(false);
		roomParticipantRepository.save(membership);
		return roomService.getRoomDetails(roomCode, userLogin);
	}

	@Override
	@Transactional
	public RoomDetailsResponseDTO confirmRoomKitchenFilters(String roomCode, String userLogin) {
		User user = userRepository.findByLogin(userLogin)
			.orElseThrow(() -> new UserNotFoundException("User not found"));
		Room room = loadRoomForCode(roomCode);
		RoomParticipant membership = roomParticipantRepository
			.findByRoomIdAndUserId(room.getId(), user.getId())
			.orElseThrow(() -> new NotRoomParticipantException("User is not a participant of this room"));

		if (room.getState() != RoomState.AWAITING_START) {
			throw new RoomKitchenTagsNotAllowedException(
				"Kitchen filters can only be confirmed while the room is awaiting start"
			);
		}
		if (!roomParticipantRepository.existsByRoomIdAndFiltersConfirmedFalse(room.getId())) {
			return roomService.getRoomDetails(roomCode, userLogin);
		}

		membership.setFiltersConfirmed(true);
		roomParticipantRepository.save(membership);
		return roomService.getRoomDetails(roomCode, userLogin);
	}

	private Room loadRoomForCode(String roomCode) {
		String normalizedCode = normalizeRoomCode(roomCode);
		return roomRepository.findByCode(normalizedCode)
			.orElseThrow(() -> new RoomNotFoundException("Room not found"));
	}

	private void assertKitchenFiltersEditable(Room room) {
		if (room.getState() != RoomState.AWAITING_START) {
			throw new RoomKitchenTagsNotAllowedException(
				"Kitchen tags can only be changed while the room is awaiting start"
			);
		}
		if (!roomParticipantRepository.existsByRoomIdAndFiltersConfirmedFalse(room.getId())) {
			throw new RoomKitchenTagsNotAllowedException(
				"Kitchen filters are locked: all participants confirmed"
			);
		}
	}

	private static LinkedHashSet<String> normalizeSlugSet(List<String> raw) {
		if (raw == null) {
			return new LinkedHashSet<>();
		}
		LinkedHashSet<String> out = new LinkedHashSet<>();
		for (String s : raw) {
			if (s == null) {
				continue;
			}
			String n = s.trim().toLowerCase();
			if (!n.isEmpty()) {
				out.add(n);
			}
		}
		return out;
	}

	private static String normalizeSlug(String raw) {
		if (raw == null) {
			return "";
		}
		return raw.trim().toLowerCase();
	}

	private List<KitchenTag> resolveKitchenTagsBySlugs(Set<String> wanted) {
		List<KitchenTag> found = kitchenTagRepository.findBySlugIn(wanted);
		if (found.size() != wanted.size()) {
			Set<String> have = found.stream().map(KitchenTag::getSlug).collect(Collectors.toSet());
			Set<String> missing = new HashSet<>(wanted);
			missing.removeAll(have);
			throw new InvalidKitchenTagSlugException("Unknown kitchen tag slug(s): " + String.join(", ", missing));
		}
		return found.stream()
			.sorted(Comparator.comparing(KitchenTag::getId))
			.toList();
	}

	private KitchenTagDTO toKitchenTagDto(KitchenTag kt) {
		return new KitchenTagDTO(kt.getId(), kt.getSlug(), kt.getLabelRu());
	}

	private static String normalizeRoomCode(String code) {
		return code == null ? "" : code.trim().toUpperCase();
	}
}
