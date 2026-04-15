package com.infy.assignments.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.infy.assignments.exception.CustomerNotFoundException;
import com.infy.assignments.exception.InvalidInputException;
import com.infy.assignments.model.RewardsResponse;

@SpringBootTest
class RewardCalculationServiceTest {

	@Autowired
	private RewardCalculationService rewardCalculationService;

	@BeforeEach
	void setUp() {
		assertNotNull(rewardCalculationService);
	}

	@Test
	void testCalculatePointsForAmountLessThanFifty() {
		long points = rewardCalculationService.calculatePointsForTransaction(new BigDecimal("45.00"));
		assertEquals(0, points, "Amount less than $50 should earn 0 points");
	}

	@Test
	void testCalculatePointsForAmountBetweenFiftyAndSixty() {
		long points = rewardCalculationService.calculatePointsForTransaction(new BigDecimal("60.00"));
		assertEquals(10, points, "$60 should earn 1 point per dollar between $50-$100 = 10 points");
	}

	@Test
	void testCalculatePointsForAmountEqualToFifty() {
		long points = rewardCalculationService.calculatePointsForTransaction(new BigDecimal("50.00"));
		assertEquals(0, points, "Amount exactly $50 should earn 0 points");
	}

	@Test
	void testCalculatePointsForAmountBetweenFiftyAndOneHundred() {
		long points = rewardCalculationService.calculatePointsForTransaction(new BigDecimal("75.00"));
		assertEquals(25, points, "$75 should earn 1 point per dollar between $50-$100 = 25 points");
	}

	@Test
	void testCalculatePointsForAmountEqualToOneHundred() {
		long points = rewardCalculationService.calculatePointsForTransaction(new BigDecimal("100.00"));
		assertEquals(50, points, "$100 should earn 1 point per dollar between $50-$100 = 50 points");
	}

	@Test
	void testCalculatePointsForAmountOneHundredTwenty() {
		long points = rewardCalculationService.calculatePointsForTransaction(new BigDecimal("120.00"));
		// Between $50-$100: 50 points
		// Over $100: $20 * 2 = 40 points
		// Total: 90 points
		assertEquals(90, points, "$120 should earn 50 + 40 = 90 points");
	}

	@Test
	void testCalculatePointsForAmountOneHundredTen() {
		long points = rewardCalculationService.calculatePointsForTransaction(new BigDecimal("110.00"));
		// Between $50-$100: 50 points
		// Over $100: $10 * 2 = 20 points
		// Total: 70 points
		assertEquals(70, points, "$110 should earn 50 + 20 = 70 points");
	}

	@Test
	void testCalculatePointsForAmountTwoHundred() {
		long points = rewardCalculationService.calculatePointsForTransaction(new BigDecimal("200.00"));
		// Between $50-$100: 50 points
		// Over $100: $100 * 2 = 200 points
		// Total: 250 points
		assertEquals(250, points, "$200 should earn 50 + 200 = 250 points");
	}

	@Test
	void testCalculatePointsForZeroAmount() {
		long points = rewardCalculationService.calculatePointsForTransaction(new BigDecimal("0.00"));
		assertEquals(0, points, "Zero amount should earn 0 points");
	}

	@Test
	void testCalculatePointsForNullAmount() {
		long points = rewardCalculationService.calculatePointsForTransaction(null);
		assertEquals(0, points, "Null amount should earn 0 points");
	}

	@Test
	void testCalculateRewardsForExistingCustomer() {
		LocalDate startDate = LocalDate.of(2026, 1, 1);
		LocalDate endDate = LocalDate.of(2026, 3, 31);

		RewardsResponse response = rewardCalculationService.calculateRewardsForCustomer(
				1L, startDate, endDate);

		assertNotNull(response);
		assertEquals(1L, response.getCustomerId());
		assertEquals("John Doe", response.getCustomerName());
		assertEquals("john.doe@example.com", response.getEmail());
		assertEquals(startDate, response.getQueryStartDate());
		assertEquals(endDate, response.getQueryEndDate());
		assertNotNull(response.getMonthlyRewards());
		assertNotNull(response.getTransactions());
	}

	@Test
	void testCalculateRewardsForNonExistentCustomer() {
		LocalDate startDate = LocalDate.of(2026, 1, 1);
		LocalDate endDate = LocalDate.of(2026, 3, 31);

		assertThrows(CustomerNotFoundException.class, () ->
				rewardCalculationService.calculateRewardsForCustomer(999L, startDate, endDate),
				"Non-existent customer should throw CustomerNotFoundException");
	}

	@Test
	void testCalculateRewardsWithInvalidDateRange() {
		LocalDate startDate = LocalDate.of(2026, 3, 31);
		LocalDate endDate = LocalDate.of(2026, 1, 1);

		assertThrows(InvalidInputException.class, () ->
				rewardCalculationService.calculateRewardsForCustomer(1L, startDate, endDate),
				"Start date after end date should throw InvalidInputException");
	}

	@Test
	void testCalculateRewardsWithNullDates() {
		RewardsResponse response = rewardCalculationService.calculateRewardsForCustomer(
				1L, null, null);

		assertNotNull(response);
		assertEquals(1L, response.getCustomerId());
	}

	@Test
	void testCalculateRewardsTotalPoints() {
		LocalDate startDate = LocalDate.of(2026, 1, 1);
		LocalDate endDate = LocalDate.of(2026, 3, 31);

		RewardsResponse response = rewardCalculationService.calculateRewardsForCustomer(
				1L, startDate, endDate);

		assertNotNull(response);
		assertNotNull(response.getMonthlyRewards());

		// Verify monthly rewards sum equals total rewards
		long totalFromMonthly = response.getMonthlyRewards().stream()
				.mapToLong(m -> m.getRewardsEarned())
				.sum();
		assertEquals(response.getTotalRewardsPoints(), totalFromMonthly,
				"Total rewards should equal sum of monthly rewards");
	}

	@Test
	void testCalculateRewardsForCustomerWithNoTransactions() {
		// Query for future dates when no transactions exist
		LocalDate startDate = LocalDate.of(2026, 5, 1);
		LocalDate endDate = LocalDate.of(2026, 5, 31);

		RewardsResponse response = rewardCalculationService.calculateRewardsForCustomer(
				1L, startDate, endDate);

		assertEquals(0, response.getTransactionCount(),
				"Customer with no transactions in date range should have 0 transaction count");
		assertEquals(0, response.getTotalRewardsPoints(),
				"Customer with no transactions should have 0 total rewards points");
	}
}