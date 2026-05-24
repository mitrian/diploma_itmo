package com.mitrian.diploma.voting.room.exception;

public class OnlyRoomOwnerException extends RuntimeException {
	public OnlyRoomOwnerException(String message) {
		super(message);
	}
}
