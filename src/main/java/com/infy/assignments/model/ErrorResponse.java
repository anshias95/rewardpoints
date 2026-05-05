package com.infy.assignments.model;

public class ErrorResponse {

	private final String errorCode;
	private final String message;
	private final int statusCode;

	public ErrorResponse(String errorCode, String message, int statusCode) {
		this.errorCode = errorCode;
		this.message = message;
		this.statusCode = statusCode;
	}

	public String getErrorCode() { return errorCode; }
	public String getMessage() { return message; }
	public int getStatusCode() { return statusCode; }
}
