package com.infy.assignments.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PointsCalculationServiceTest {

	private PointsCalculationService service;

	@BeforeEach
	void setUp() {
		service = new PointsCalculationService();
	}

	// --- Boundary: below $50 ---

	@Test
	void calculatePoints_nullAmount_returnsZero() {
		assertEquals(0, service.calculatePoints(null));
	}

	@Test
	void calculatePoints_zeroAmount_returnsZero() {
		assertEquals(0, service.calculatePoints(new BigDecimal("0.00")));
	}

	@Test
	void calculatePoints_negativeAmount_returnsZero() {
		assertEquals(0, service.calculatePoints(new BigDecimal("-10.00")));
	}

	@Test
	void calculatePoints_amountBelow50_returnsZero() {
		assertEquals(0, service.calculatePoints(new BigDecimal("45.00")));
	}

	@Test
	void calculatePoints_amountExactly50_returnsZero() {
		assertEquals(0, service.calculatePoints(new BigDecimal("50.00")));
	}

	// --- Between $50 and $100 ---

	@Test
	void calculatePoints_amountOf60_returns10() {
		assertEquals(10, service.calculatePoints(new BigDecimal("60.00")));
	}

	@Test
	void calculatePoints_amountOf75_returns25() {
		assertEquals(25, service.calculatePoints(new BigDecimal("75.00")));
	}

	@Test
	void calculatePoints_amountExactly100_returns50() {
		assertEquals(50, service.calculatePoints(new BigDecimal("100.00")));
	}

	// --- Over $100 ---

	@Test
	void calculatePoints_amountOf110_returns70() {
		// $50–$100 = 50 pts; over $100 = $10 * 2 = 20 pts → total 70
		assertEquals(70, service.calculatePoints(new BigDecimal("110.00")));
	}

	@Test
	void calculatePoints_amountOf120_returns90() {
		// $50–$100 = 50 pts; over $100 = $20 * 2 = 40 pts → total 90
		assertEquals(90, service.calculatePoints(new BigDecimal("120.00")));
	}

	@Test
	void calculatePoints_amountOf200_returns250() {
		// $50–$100 = 50 pts; over $100 = $100 * 2 = 200 pts → total 250
		assertEquals(250, service.calculatePoints(new BigDecimal("200.00")));
	}

	// --- Decimal amounts (regression for prior longValue() truncation bug) ---

	@Test
	void calculatePoints_decimalOver100_roundsHalfUp() {
		// $120.75: over-$100 = 20.75 * 2 = 41.5 → rounds to 42; $50–$100 = 50 → total 92
		assertEquals(92, service.calculatePoints(new BigDecimal("120.75")));
	}

	@Test
	void calculatePoints_decimalBetween50And100_roundsHalfUp() {
		// $75.50: $50–$100 portion = 25.50 → rounds to 26
		assertEquals(26, service.calculatePoints(new BigDecimal("75.50")));
	}

	@Test
	void calculatePoints_decimalJustOver50_roundsDown() {
		// $50.25: portion = 0.25 → rounds to 0
		assertEquals(0, service.calculatePoints(new BigDecimal("50.25")));
	}

	@Test
	void calculatePoints_decimalJustOver50WithHalfCent_roundsUp() {
		// $50.50: portion = 0.50 → rounds to 1
		assertEquals(1, service.calculatePoints(new BigDecimal("50.50")));
	}

	@Test
	void calculatePoints_decimalAmount55_25() {
		// $55.25: portion = 5.25 → rounds to 5
		assertEquals(5, service.calculatePoints(new BigDecimal("55.25")));
	}
}
