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

	/** Entry point for the single-customer endpoint — accepts raw string params from the controller. */
	public RewardsResponse calculateRewards(Long customerId, String startDateStr, String endDateStr) {
		LocalDate startDate = parseDateOrDefault(startDateStr, LocalDate.now().minusDays(90));
		LocalDate endDate = parseDateOrDefault(endDateStr, LocalDate.now());
		validateDateRange(startDate, endDate);
		return calculateRewardsForCustomer(customerId, startDate, endDate);
	}

	/** Entry point for the all-customers endpoint — date range is fixed to the last 3 months. */
	public List<RewardsResponse> calculateRewardsForAllCustomers() {
		LocalDate endDate = LocalDate.now();
		LocalDate startDate = endDate.minusMonths(3).withDayOfMonth(1);
		logger.info("Calculating rewards for all customers from {} to {}", startDate, endDate);
		return customerService.getAllCustomers().stream()
				.map(customer -> calculateRewardsForCustomer(customer.getId(), startDate, endDate))
				.collect(Collectors.toList());
	}

	/** Core calculation — accepts resolved LocalDate params (nullable; null means apply defaults). */
	public RewardsResponse calculateRewardsForCustomer(Long customerId, LocalDate startDate, LocalDate endDate) {
		if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
			throw new InvalidInputException("Start date cannot be after end date");
		}

		LocalDate resolvedStart = startDate != null ? startDate : LocalDate.now().minusDays(90);
		LocalDate resolvedEnd = endDate != null ? endDate : LocalDate.now();

		Customer customer = customerService.getCustomer(customerId)
				.orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + customerId));

		List<Transaction> filtered = dataService.getTransactionsByCustomerId(customerId).stream()
				.filter(txn -> !txn.getTransactionDate().isBefore(resolvedStart)
						&& !txn.getTransactionDate().isAfter(resolvedEnd))
				.collect(Collectors.toList());

		List<MonthlyRewards> monthlyRewards = buildMonthlyRewards(filtered);
		long totalPoints = monthlyRewards.stream().mapToLong(MonthlyRewards::getRewardsEarned).sum();
		BigDecimal totalSpent = monthlyRewards.stream()
				.map(MonthlyRewards::getTotalSpent)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		logger.info("Customer {}: {} transactions, {} points", customerId, filtered.size(), totalPoints);

		RewardsResponse response = new RewardsResponse();
		response.setCustomerId(customerId);
		response.setCustomerName(customer.getName());
		response.setEmail(customer.getEmail());
		response.setQueryStartDate(resolvedStart);
		response.setQueryEndDate(resolvedEnd);
		response.setTransactionCount(filtered.size());
		response.setTransactions(filtered);
		response.setMonthlyRewards(monthlyRewards);
		response.setTotalRewardsPoints(totalPoints);
		response.setTotalPurchaseAmount(totalSpent);
		return response;
	}

	/** Delegates to PointsCalculationService; kept public for existing tests. */
	public long calculatePointsForTransaction(BigDecimal amount) {
		return pointsCalculationService.calculatePoints(amount);
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

	private LocalDate parseDateOrDefault(String dateStr, LocalDate defaultValue) {
		if (dateStr == null || dateStr.trim().isEmpty()) {
			return defaultValue;
		}
		try {
			return LocalDate.parse(dateStr.trim());
		} catch (DateTimeParseException e) {
			throw new InvalidInputException("Invalid date format: '" + dateStr.trim() + "'. Expected YYYY-MM-DD");
		}
	}
}
