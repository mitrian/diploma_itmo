package com.mitrian.diploma.voting.catalog.service.impl;

import com.mitrian.diploma.voting.room.filter.entity.RoomKitchenTagSelection;
import com.mitrian.diploma.voting.room.filter.repository.RoomKitchenTagSelectionRepository;
import com.mitrian.diploma.voting.catalog.entity.Restaurant;
import com.mitrian.diploma.voting.catalog.exception.CandidateSelectionPrerequisitesException;
import com.mitrian.diploma.voting.catalog.repository.RestaurantKitchenTagRepository;
import com.mitrian.diploma.voting.catalog.repository.RestaurantRepository;
import com.mitrian.diploma.voting.catalog.service.CandidateSelectionService;
import com.mitrian.diploma.voting.catalog.util.Haversine;
import com.mitrian.diploma.voting.room.entity.Room;
import com.mitrian.diploma.voting.room.exception.RoomNotFoundException;
import com.mitrian.diploma.voting.room.repository.RoomRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CandidateSelectionServiceImpl implements CandidateSelectionService {

	private static final int MAX_STAGE_ONE_CANDIDATES = 25;

	private final RoomRepository roomRepository;
	private final RoomKitchenTagSelectionRepository roomKitchenTagSelectionRepository;
	private final RestaurantKitchenTagRepository restaurantKitchenTagRepository;
	private final RestaurantRepository restaurantRepository;

	public CandidateSelectionServiceImpl(
		RoomRepository roomRepository,
		RoomKitchenTagSelectionRepository roomKitchenTagSelectionRepository,
		RestaurantKitchenTagRepository restaurantKitchenTagRepository,
		RestaurantRepository restaurantRepository
	) {
		this.roomRepository = roomRepository;
		this.roomKitchenTagSelectionRepository = roomKitchenTagSelectionRepository;
		this.restaurantKitchenTagRepository = restaurantKitchenTagRepository;
		this.restaurantRepository = restaurantRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Restaurant> findCandidatesForRoom(Long roomId) {
		Room room = roomRepository.findById(roomId)
			.orElseThrow(() -> new RoomNotFoundException("Room not found"));

		if (room.getCenterLat() == null || room.getCenterLon() == null || room.getMaxDistanceMeters() == null) {
			throw new CandidateSelectionPrerequisitesException("Geo filter is not configured for this room");
		}

		Set<Long> roomKitchenTagIds = roomKitchenTagSelectionRepository.findByRoomId(roomId).stream()
			.map(RoomKitchenTagSelection::getKitchenTagId)
			.collect(Collectors.toCollection(LinkedHashSet::new));
		List<Restaurant> byTags;
		if (roomKitchenTagIds.isEmpty()) {
			byTags = restaurantRepository.findAll().stream()
				.sorted(Comparator.comparing(Restaurant::getId))
				.toList();
		} else {
			List<Long> restaurantIds = restaurantKitchenTagRepository
				.findDistinctRestaurantIdsByKitchenTagIdIn(roomKitchenTagIds);
			if (restaurantIds.isEmpty()) {
				return List.of();
			}
			byTags = restaurantRepository.findAllByIdInOrderByIdAsc(restaurantIds);
		}
		double centerLat = room.getCenterLat();
		double centerLon = room.getCenterLon();
		int maxMeters = room.getMaxDistanceMeters();

		record Scored(Restaurant restaurant, double distanceMeters) {
		}

		List<Scored> withinRadius = new ArrayList<>();
		for (Restaurant r : byTags) {
			double m = Haversine.distanceMeters(centerLat, centerLon, r.getLatitude(), r.getLongitude());
			if (m <= maxMeters) {
				withinRadius.add(new Scored(r, m));
			}
		}

		withinRadius.sort(Comparator
			.comparingDouble(Scored::distanceMeters)
			.thenComparing(s -> s.restaurant().getId()));

		return withinRadius.stream()
			.limit(MAX_STAGE_ONE_CANDIDATES)
			.map(Scored::restaurant)
			.toList();
	}
}
