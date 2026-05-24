package com.mitrian.diploma.voting.room.exception;

public class RoomLeaveNotAllowedWhileAwaitingStartException extends RuntimeException {
	public RoomLeaveNotAllowedWhileAwaitingStartException(String message) {
		super(message);
	}
}
