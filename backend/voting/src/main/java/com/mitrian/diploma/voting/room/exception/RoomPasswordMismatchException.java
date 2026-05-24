package com.mitrian.diploma.voting.room.exception;

public class RoomPasswordMismatchException extends RuntimeException {
	public RoomPasswordMismatchException(String message) {
		super(message);
	}
}
