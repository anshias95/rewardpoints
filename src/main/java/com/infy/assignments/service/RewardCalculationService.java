package com.infy.assignments.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.infy.assignments.model.Customer;
import com.infy.assignments.model.MonthlyRewards;
import com.infy.assignments.model.RewardsResponse;
import com.infy.assignments.model.Transaction;
import com.infy.assignments.exception.InvalidInputException;
import com.infy.assignments.exception.CustomerNotFoundException;

@Service
public class RewardCalculationService {

	private static final Logger logger = LoggerFactory.getLogger(RewardCalculationService.class);
	private static final BigDecimal HUNDRED = new BigDecimal("100");
	private static final BigDecimal FIFTY = new BigDecimal("50");

	private final DataService dataService;
	private final CustomerService customerService;

	public RewardCalculationService(DataService dataService, CustomerService customerService) {
		this.dataService = dataService;
		this.customerService = customerService;
	}

	public RewardsResponse calculateRewardsForCustomer(Long customerId, LocalDate startDate, LocalDate endDate) {
		logger.info("Calculating rewards for customer: {} from {} to {}", customerId, startDate, endDate);

		// Validate input
		if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
			logger.warn("Invalid date range: startDate {} is after endDate {}", startDate, endDate);
			throw new InvalidInputException("Start date cannot be after end date");
		}

		// Get customer details
		Customer customer = customerService.getCustomer(customerId)
				.orElseThrow(() -> {
					logger.warn("Customer not found: {}", customerId);
					return new CustomerNotFoundException("Customer not found: " + customerId);
				});

		// Get transactions for customer
		List<Transaction> transactions = dataService.getTransactionsByCustomerId(customerId);
		logger.info("Found {} transactions for customer: {}", transactions.size(), customerId);

		// Filter by date range
		List<Transaction> filteredTransactions = filterTransactionsByDateRange(transactions, startDate, endDate);
		logger.info("After date filtering: {} transactions", filteredTransactions.size());

		// Calculate rewards
		RewardsResponse response = new RewardsResponse();
		response.setCustomerId(customerId);
		response.setCustomerName(customer.getName());
		response.setEmail(customer.getEmail());
		response.setQueryStartDate(startDate);
		response.setQueryEndDate(endDate);
		response.setTransactionCount(filteredTransactions.size());
		response.setTransactions(filteredTransactions);

		// Calculate monthly rewards and totals
		Map<YearMonth, List<Transaction>> groupedByMonth = groupTransactionsByMonth(filteredTransactions);
		List<MonthlyRewards> monthlyRewardsList = new ArrayList<>();
		long totalRewardsPoints = 0;
		BigDecimal totalPurchaseAmount = BigDecimal.ZERO;

		List<YearMonth> sortedMonths = groupedByMonth.keySet().stream()
				.sorted()
				.collect(Collectors.toList());

		for (YearMonth month : sortedMonths) {
			List<Transaction> monthTransactions = groupedByMonth.get(month);
			BigDecimal monthTotal = BigDecimal.ZERO;
			long monthRewards = 0;

			for (Transaction txn : monthTransactions) {
				monthTotal = monthTotal.add(txn.getAmount());
				monthRewards += calculatePointsForTransaction(txn.getAmount());
			}

			MonthlyRewards monthlyReward = new MonthlyRewards();
			monthlyReward.setMonth(month);
			monthlyReward.setTransactionCount(monthTransactions.size());
			monthlyReward.setTotalSpent(monthTotal);
			monthlyReward.setRewardsEarned(monthRewards);
			monthlyRewardsList.add(monthlyReward);

			totalRewardsPoints += monthRewards;
			totalPurchaseAmount = totalPurchaseAmount.add(monthTotal);
		}

		response.setMonthlyRewards(monthlyRewardsList);
		response.setTotalRewardsPoints(totalRewardsPoints);
		response.setTotalPurchaseAmount(totalPurchaseAmount);

		logger.info("Reward calculation complete. Total points: {}, Total spent: {}",
				totalRewardsPoints, totalPurchaseAmount);

		return response;
	}

	private List<Transaction> filterTransactionsByDateRange(List<Transaction> transactions,
			LocalDate startDate, LocalDate endDate) {
		return transactions.stream()
				.filter(txn -> {
					if (startDate != null && txn.getTransactionDate().isBefore(startDate)) {
						return false;
					}
					if (endDate != null && txn.getTransactionDate().isAfter(endDate)) {
						return false;
					}
					return true;
				})
				.collect(Collectors.toList());
	}

	private Map<YearMonth, List<Transaction>> groupTransactionsByMonth(List<Transaction> transactions) {
		return transactions.stream()
				.collect(Collectors.groupingBy(txn -> YearMonth.from(txn.getTransactionDate())));
	}

	/**
	 * Calculate reward points based on transaction amount.
	 * - 2 points for every dollar spent over $100
	 * - 1 point for every dollar spent between $50 and $100
	 *
	 * @param amount The transaction amount
	 * @return The number of reward points
	 */
	public long calculatePointsForTransaction(BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			return 0;
		}

		long points = 0;

		// Points for amount over $100: 2 points per dollar
		if (amount.compareTo(HUNDRED) > 0) {
			BigDecimal amountOverHundred = amount.subtract(HUNDRED);
			points += amountOverHundred.longValue() * 2;
		}

		// Points for amount between $50 and $100: 1 point per dollar
		BigDecimal cappedAmount = amount.min(HUNDRED);
		if (cappedAmount.compareTo(FIFTY) > 0) {
			BigDecimal amountBetweenFiftyAndHundred = cappedAmount.subtract(FIFTY);
			points += amountBetweenFiftyAndHundred.longValue();
		}

		return points;
	}
}
