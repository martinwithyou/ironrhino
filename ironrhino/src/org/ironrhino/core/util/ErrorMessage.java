package org.ironrhino.core.util;

public class ErrorMessage extends RuntimeException {

	private static final long serialVersionUID = 6808322631499170777L;

	public ErrorMessage(String message) {
		super(message);
	}

	public Throwable fillInStackTrace() {
		return this;
	}

}
