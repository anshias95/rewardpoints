package com.infy.assignments.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infy.assignments.model.RewardsResponse;
import com.infy.assignments.service.RewardCalculationService;

@RestController
@RequestMapping("/api/v1/rewards")
public class RewardsController {

	private static final Logger logger = LoggerFactory.getLogger(RewardsController.class);

	private final RewardCalculationService rewardCalculationService;

	public RewardsController(RewardCalculationService rewardCalculationService) {
		this.rewardCalculationService = rewardCalculationService;
	}

	@GetMapping("/calculate")
	public ResponseEntity<List<RewardsResponse>> calculateRewards(
			@RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate) {
		logger.info("Received rewards calculation request: startDate={}, endDate={}", startDate, endDate);
		List<RewardsResponse> responses = rewardCalculationService.calculateRewardsForAllCustomers(startDate, endDate);
		logger.info("Returning rewards for {} customers", responses.size());
		return ResponseEntity.ok(responses);
	}
}
