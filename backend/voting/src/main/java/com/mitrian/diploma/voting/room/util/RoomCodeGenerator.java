package com.mitrian.diploma.voting.room.util;

import com.mitrian.diploma.voting.room.repository.RoomRepository;
import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class RoomCodeGenerator {

	private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
	private static final int CODE_LENGTH = 8;
	private static final int MAX_CODE_GENERATION_ATTEMPTS = 32;

	private final RoomRepository roomRepository;
	private final SecureRandom secureRandom = new SecureRandom();

	public RoomCodeGenerator(RoomRepository roomRepository) {
		this.roomRepository = roomRepository;
	}

	public String generateUniqueCode() {
		for (int attempt = 0; attempt < MAX_CODE_GENERATION_ATTEMPTS; attempt++) {
			String candidate = randomCode();
			if (!roomRepository.existsByCode(candidate)) {
				return candidate;
			}
		}
		throw new IllegalStateException("Could not generate unique room code");
	}

	private String randomCode() {
		StringBuilder sb = new StringBuilder(CODE_LENGTH);
		for (int i = 0; i < CODE_LENGTH; i++) {
			sb.append(CODE_ALPHABET.charAt(secureRandom.nextInt(CODE_ALPHABET.length())));
		}
		return sb.toString();
	}
}
