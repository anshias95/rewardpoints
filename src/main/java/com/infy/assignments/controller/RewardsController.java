package com.infy.assignments.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infy.assignments.exception.CustomerNotFoundException;
import com.infy.assignments.exception.InvalidInputException;
import com.infy.assignments.model.ErrorResponse;
import com.infy.assignments.model.RewardsResponse;
import com.infy.assignments.service.RewardCalculationService;

@RestController
public class RewardsController {

	private static final Logger logger = LoggerFactory.getLogger(RewardsController.class);

	private final RewardCalculationService rewardCalculationService;

	public RewardsController(RewardCalculationService rewardCalculationService) {
		this.rewardCalculationService = rewardCalculationService;
	}

	@GetMapping("/api/rewards/{customerId}")
	public ResponseEntity<RewardsResponse> calculateRewards(
			@PathVariable Long customerId,
			@RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate) {

		logger.info("Received request to calculate rewards for customer: {} from {} to {}",
				customerId, startDate, endDate);
		RewardsResponse response = rewardCalculationService.calculateRewards(customerId, startDate, endDate);
		logger.info("Successfully calculated rewards for customer {}. Total points: {}",
				customerId, response.getTotalRewardsPoints());
		return ResponseEntity.ok(response);
	}

	@GetMapping("/api/v1/rewards/calculate")
	public ResponseEntity<List<RewardsResponse>> calculateAllCustomerRewards() {
		logger.info("Received request to calculate rewards for all customers (last 3 months)");
		List<RewardsResponse> responses = rewardCalculationService.calculateRewardsForAllCustomers();
		logger.info("Successfully calculated rewards for {} customers", responses.size());
		return ResponseEntity.ok(responses);
	}

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
