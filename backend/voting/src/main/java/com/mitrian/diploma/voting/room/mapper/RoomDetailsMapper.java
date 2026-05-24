package com.mitrian.diploma.voting.room.mapper;

import com.mitrian.diploma.auth.entity.User;
import com.mitrian.diploma.voting.room.filter.dto.KitchenTagDTO;
import com.mitrian.diploma.voting.room.filter.entity.KitchenTag;
import com.mitrian.diploma.voting.room.filter.entity.RoomKitchenTagSelection;
import com.mitrian.diploma.voting.room.dto.RoomDetailsResponseDTO;
import com.mitrian.diploma.voting.room.dto.RoomParticipantViewDTO;
import com.mitrian.diploma.voting.room.entity.Room;
import com.mitrian.diploma.voting.room.entity.RoomParticipant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RoomDetailsMapper {

	public RoomDetailsResponseDTO toRoomDetails(
		Room room,
		User viewer,
		List<RoomParticipant> participantsRows,
		Map<Long, User> usersById,
		List<RoomKitchenTagSelection> roomSelections,
		Map<Long, KitchenTag> tagsById
	) {
		boolean currentUserIsOwner = room.getOwnerId().equals(viewer.getId());

		List<RoomParticipantViewDTO> participants = participantsRows.stream()
			.map(rp -> {
				User u = usersById.get(rp.getUserId());
				if (u == null) {
					return null;
				}
				return new RoomParticipantViewDTO(
					u.getDisplayName(),
					rp.isOwner(),
					rp.isReady(),
					rp.isFiltersConfirmed()
				);
			})
			.filter(Objects::nonNull)
			.toList();

		Set<Long> unionTagIds = roomSelections.stream()
			.map(RoomKitchenTagSelection::getKitchenTagId)
			.collect(Collectors.toSet());

		List<KitchenTagDTO> roomKitchenTags = unionTagIds.stream()
			.map(tagsById::get)
			.filter(Objects::nonNull)
			.sorted(Comparator.comparing(KitchenTag::getId))
			.map(this::toKitchenTagDto)
			.toList();

		List<String> myKitchenTagSlugs = roomSelections.stream()
			.filter(s -> s.getUserId().equals(viewer.getId()))
			.map(RoomKitchenTagSelection::getKitchenTagId)
			.map(tagsById::get)
			.filter(Objects::nonNull)
			.sorted(Comparator.comparing(KitchenTag::getId))
			.map(KitchenTag::getSlug)
			.toList();

		return new RoomDetailsResponseDTO(
			room.getCode(),
			room.getState(),
			room.getCenterLat(),
			room.getCenterLon(),
			room.getMaxDistanceMeters(),
			currentUserIsOwner,
			participants,
			roomKitchenTags,
			myKitchenTagSlugs
		);
	}

	public KitchenTagDTO toKitchenTagDto(KitchenTag kt) {
		return new KitchenTagDTO(kt.getId(), kt.getSlug(), kt.getLabelRu());
	}
}
