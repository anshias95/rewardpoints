package com.infy.assignments.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.infy.assignments.exception.CustomerNotFoundException;
import com.infy.assignments.exception.InvalidInputException;
import com.infy.assignments.model.Customer;
import com.infy.assignments.model.RewardsResponse;
import com.infy.assignments.model.RewardsResponse.MonthlyRewards;
import com.infy.assignments.model.Transaction;

@Service
public class RewardCalculationService {

	private static final Logger logger = LoggerFactory.getLogger(RewardCalculationService.class);

	private final DataService dataService;
	private final CustomerService customerService;
	private final PointsCalculationService pointsCalculationService;

	public RewardCalculationService(DataService dataService, CustomerService customerService,
			PointsCalculationService pointsCalculationService) {
		this.dataService = dataService;
		this.customerService = customerService;
		this.pointsCalculationService = pointsCalculationService;
	}

	/**
	 * Calculates rewards for all customers.
	 * Rules: both dates absent → default last 3 months; both present → use as-is;
	 * exactly one present → InvalidInputException.
	 */
	public List<RewardsResponse> calculateRewardsForAllCustomers(String startDateStr, String endDateStr) {
		boolean hasStart = startDateStr != null && !startDateStr.trim().isEmpty();
		boolean hasEnd = endDateStr != null && !endDateStr.trim().isEmpty();

		final LocalDate startDate;
		final LocalDate endDate;

		if (!hasStart && !hasEnd) {
			endDate = LocalDate.now();
			startDate = endDate.minusMonths(3).withDayOfMonth(1);
		} else if (hasStart && hasEnd) {
			startDate = parseDate(startDateStr);
			endDate = parseDate(endDateStr);
			validateDateRange(startDate, endDate);
		} else {
			throw new InvalidInputException(
					"Both startDate and endDate must be provided together, or neither.");
		}

		logger.info("Calculating rewards for all customers from {} to {}", startDate, endDate);
		return customerService.getAllCustomers().stream()
				.map(customer -> calculateRewardsForCustomer(customer.getId(), startDate, endDate))
				.collect(Collectors.toList());
	}

	private RewardsResponse calculateRewardsForCustomer(Long customerId, LocalDate startDate, LocalDate endDate) {
		Customer customer = customerService.getCustomer(customerId)
				.orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + customerId));

		List<Transaction> filtered = dataService.getTransactionsByCustomerId(customerId).stream()
				.filter(txn -> !txn.getTransactionDate().isBefore(startDate)
						&& !txn.getTransactionDate().isAfter(endDate))
				.collect(Collectors.toList());

		List<MonthlyRewards> monthlyBreakdown = buildMonthlyRewards(filtered);
		long totalPoints = monthlyBreakdown.stream().mapToLong(MonthlyRewards::getRewardsEarned).sum();
		BigDecimal totalSpent = filtered.stream()
				.map(Transaction::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		logger.info("Customer {}: {} transactions, {} points", customerId, filtered.size(), totalPoints);

		return new RewardsResponse(customerId, customer.getName(), customer.getEmail(),
				startDate, endDate, filtered.size(), totalSpent, totalPoints, monthlyBreakdown, filtered);
	}

	private List<MonthlyRewards> buildMonthlyRewards(List<Transaction> transactions) {
		return transactions.stream()
				.collect(Collectors.groupingBy(txn -> YearMonth.from(txn.getTransactionDate())))
				.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.map(entry -> {
					List<Transaction> monthTxns = entry.getValue();
					BigDecimal spent = monthTxns.stream()
							.map(Transaction::getAmount)
							.reduce(BigDecimal.ZERO, BigDecimal::add);
					long points = monthTxns.stream()
							.mapToLong(txn -> pointsCalculationService.calculatePoints(txn.getAmount()))
							.sum();
					return new MonthlyRewards(entry.getKey(), monthTxns.size(), spent, points);
				})
				.collect(Collectors.toList());
	}

	private void validateDateRange(LocalDate startDate, LocalDate endDate) {
		if (startDate.isAfter(endDate)) {
			throw new InvalidInputException("Start date cannot be after end date");
		}
		if (endDate.isAfter(LocalDate.now())) {
			throw new InvalidInputException("End date cannot be in the future");
		}
	}

	private LocalDate parseDate(String dateStr) {
		try {
			return LocalDate.parse(dateStr.trim());
		} catch (DateTimeParseException e) {
			throw new InvalidInputException(
					"Invalid date format: '" + dateStr.trim() + "'. Expected YYYY-MM-DD");
		}
	}
}
