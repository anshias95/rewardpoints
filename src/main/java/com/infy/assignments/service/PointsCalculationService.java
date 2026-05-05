package com.infy.assignments.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

@Service
public class PointsCalculationService {

	private static final BigDecimal HUNDRED = new BigDecimal("100");
	private static final BigDecimal FIFTY = new BigDecimal("50");
	private static final BigDecimal TWO = new BigDecimal("2");

	/**
	 * Points rule: 2 pts per dollar over $100, 1 pt per dollar between $50–$100.
	 * Fractional dollars are rounded HALF_UP before summing so $120.75 earns
	 * round(20.75 * 2) + 50 = 92 points rather than silently truncating to 90.
	 */
	public long calculatePoints(BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			return 0;
		}
		long points = 0;
		if (amount.compareTo(HUNDRED) > 0) {
			points += amount.subtract(HUNDRED).multiply(TWO)
					.setScale(0, RoundingMode.HALF_UP).longValue();
		}
		BigDecimal cappedAmount = amount.min(HUNDRED);
		if (cappedAmount.compareTo(FIFTY) > 0) {
			points += cappedAmount.subtract(FIFTY)
					.setScale(0, RoundingMode.HALF_UP).longValue();
		}
		return points;
	}
}
