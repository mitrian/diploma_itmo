package com.mitrian.diploma.auth.exception;

public class LoginAlreadyExistsException extends RuntimeException {
	public LoginAlreadyExistsException(String message) {
		super(message);
	}
}
