package com.mitrian.diploma.voting.room.util;

public final class RoomCodeHelper {

	private RoomCodeHelper() {
	}

	public static String normalize(String code) {
		return code == null ? "" : code.trim().toUpperCase();
	}
}
