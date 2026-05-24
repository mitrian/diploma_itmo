package com.mitrian.diploma.voting.room.dto;

public record RoomParticipantViewDTO(
	String displayName,
	boolean owner,
	boolean ready,
	boolean filtersConfirmed
) {
}
