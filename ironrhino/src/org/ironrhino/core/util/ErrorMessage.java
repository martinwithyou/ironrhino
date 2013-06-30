package org.ironrhino.core.util;

public class ErrorMessage extends RuntimeException {

	private static final long serialVersionUID = 6808322631499170777L;

	private String message;

	private Object[] args;

	private String submessage;

	public ErrorMessage(String message) {
		super(message);
		this.message = message;
	}

	public ErrorMessage(String message, Object[] args) {
		super(message);
		this.message = message;
		this.args = args;
	}

	public ErrorMessage(String message, Object[] args, String submessage) {
		super(message);
		this.message = message;
		this.args = args;
		this.submessage = submessage;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	public String getSubmessage() {
		return submessage;
	}

	public void setSubmessage(String submessage) {
		this.submessage = submessage;
	}

	@Override
	public Throwable fillInStackTrace() {
		return this;
	}

}
