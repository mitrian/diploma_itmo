package com.mitrian.diploma.history.dto.row;

public record ParticipantRow(
	Long userId,
	String login,
	String displayName,
	boolean owner
) {
}
