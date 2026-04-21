package com.infy.assignments.service;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class PointsCalculationService {

	private static final BigDecimal HUNDRED = new BigDecimal("100");
	private static final BigDecimal FIFTY = new BigDecimal("50");

	public long calculatePoints(BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			return 0;
		}
		long points = 0;
		if (amount.compareTo(HUNDRED) > 0) {
			points += amount.subtract(HUNDRED).longValue() * 2;
		}
		BigDecimal cappedAmount = amount.min(HUNDRED);
		if (cappedAmount.compareTo(FIFTY) > 0) {
			points += cappedAmount.subtract(FIFTY).longValue();
		}
		return points;
	}
}
