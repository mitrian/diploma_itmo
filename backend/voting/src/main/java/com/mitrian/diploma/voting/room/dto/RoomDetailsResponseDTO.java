package com.mitrian.diploma.voting.room.dto;

import com.mitrian.diploma.voting.room.filter.dto.KitchenTagDTO;
import com.mitrian.diploma.voting.room.entity.RoomState;
import java.util.List;

public record RoomDetailsResponseDTO(
	String code,
	RoomState state,
	Double centerLat,
	Double centerLon,
	Integer maxDistanceMeters,
	boolean currentUserIsOwner,
	List<RoomParticipantViewDTO> participants,
	List<KitchenTagDTO> roomKitchenTags,
	List<String> myKitchenTagSlugs
) {
}
