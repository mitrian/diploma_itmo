package com.mitrian.diploma.voting.room.exception;

public class RoomLeaveNotAllowedDuringVotingException extends RuntimeException {
	public RoomLeaveNotAllowedDuringVotingException(String message) {
		super(message);
	}
}
