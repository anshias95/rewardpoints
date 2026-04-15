package com.infy.assignments.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.infy.assignments.exception.CustomerNotFoundException;
import com.infy.assignments.exception.InvalidInputException;
import com.infy.assignments.model.RewardsResponse;
import com.infy.assignments.service.RewardCalculationService;

@RestController
@RequestMapping("/api/rewards")
public class RewardsController {

	private static final Logger logger = LoggerFactory.getLogger(RewardsController.class);

	private final RewardCalculationService rewardCalculationService;

	public RewardsController(RewardCalculationService rewardCalculationService) {
		this.rewardCalculationService = rewardCalculationService;
	}

	/**
	 * Calculate rewards for a customer within a specified date range.
	 *
	 * @param customerId The customer ID
	 * @param startDate  Optional start date (YYYY-MM-DD format)
	 * @param endDate    Optional end date (YYYY-MM-DD format)
	 * @return RewardsResponse containing customer rewards details
	 */
	@GetMapping("/{customerId}")
	public ResponseEntity<RewardsResponse> calculateRewards(
			@PathVariable Long customerId,
			@RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate) {

		logger.info("Received request to calculate rewards for customer: {} from {} to {}",
				customerId, startDate, endDate);

		// Default to last 90 days if no dates provided
		LocalDate finalEndDate = parseDate(endDate, LocalDate.now());
		LocalDate finalStartDate = parseDate(startDate, LocalDate.now().minusDays(90));

		// Validate that start date is not after end date
		if (finalStartDate.isAfter(finalEndDate)) {
			logger.warn("Invalid date range: startDate {} is after endDate {}", finalStartDate, finalEndDate);
			throw new InvalidInputException("Start date cannot be after end date");
		}

		// Validate that dates are not in the future
		if (finalEndDate.isAfter(LocalDate.now())) {
			logger.warn("End date {} is in the future", finalEndDate);
			throw new InvalidInputException("End date cannot be in the future");
		}

		RewardsResponse response = rewardCalculationService.calculateRewardsForCustomer(
				customerId, finalStartDate, finalEndDate);

		logger.info("Successfully calculated rewards for customer {}. Total points: {}",
				customerId, response.getTotalRewardsPoints());

		return ResponseEntity.ok(response);
	}

	private LocalDate parseDate(String dateStr, LocalDate defaultValue) {
		if (dateStr == null || dateStr.trim().isEmpty()) {
			return defaultValue;
		}
		try {
			return LocalDate.parse(dateStr.trim());
		} catch (DateTimeParseException e) {
			throw new InvalidInputException("Invalid date format: '" + dateStr.trim() + "'. Expected YYYY-MM-DD");
		}
	}

	@ExceptionHandler(CustomerNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleCustomerNotFoundException(CustomerNotFoundException ex) {
		logger.error("Customer not found: {}", ex.getMessage());
		ErrorResponse errorResponse = new ErrorResponse(
				"CUSTOMER_NOT_FOUND",
				ex.getMessage(),
				404);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler(InvalidInputException.class)
	public ResponseEntity<ErrorResponse> handleInvalidInputException(InvalidInputException ex) {
		logger.error("Invalid input: {}", ex.getMessage());
		ErrorResponse errorResponse = new ErrorResponse(
				"INVALID_INPUT",
				ex.getMessage(),
				400);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
		logger.error("Unexpected error", ex);
		ErrorResponse errorResponse = new ErrorResponse(
				"INTERNAL_ERROR",
				"An unexpected error occurred",
				500);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}

	public static class ErrorResponse {
		public String errorCode;
		public String message;
		public int statusCode;

		public ErrorResponse(String errorCode, String message, int statusCode) {
			this.errorCode = errorCode;
			this.message = message;
			this.statusCode = statusCode;
		}

		public String getErrorCode() {
			return errorCode;
		}

		public String getMessage() {
			return message;
		}

		public int getStatusCode() {
			return statusCode;
		}
	}
}