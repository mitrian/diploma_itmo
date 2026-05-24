package com.mitrian.diploma.voting.room.exception;

public class OwnerCannotLeaveRoomException extends RuntimeException {
	public OwnerCannotLeaveRoomException(String message) {
		super(message);
	}
}
