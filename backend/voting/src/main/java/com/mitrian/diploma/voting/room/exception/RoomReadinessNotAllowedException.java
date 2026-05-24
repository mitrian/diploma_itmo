package com.mitrian.diploma.voting.room.exception;

public class RoomReadinessNotAllowedException extends RuntimeException {
	public RoomReadinessNotAllowedException(String message) {
		super(message);
	}
}
