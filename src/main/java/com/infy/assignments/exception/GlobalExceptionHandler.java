package com.infy.assignments.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.infy.assignments.model.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(CustomerNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleCustomerNotFoundException(CustomerNotFoundException ex) {
		logger.error("Customer not found: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ErrorResponse("CUSTOMER_NOT_FOUND", ex.getMessage(), 404));
	}

	@ExceptionHandler(InvalidInputException.class)
	public ResponseEntity<ErrorResponse> handleInvalidInputException(InvalidInputException ex) {
		logger.error("Invalid input: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponse("INVALID_INPUT", ex.getMessage(), 400));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
		logger.error("Unexpected error", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred", 500));
	}
}
