package com.infy.assignments.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class RewardsResponse {

	private final Long customerId;
	private final String customerName;
	private final String email;
	private final LocalDate queryStartDate;
	private final LocalDate queryEndDate;
	private final int transactionCount;
	private final BigDecimal totalPurchaseAmount;
	private final long totalRewardsPoints;
	private final List<MonthlyRewards> monthlyRewards;
	private final List<Transaction> transactions;

	public RewardsResponse(Long customerId, String customerName, String email,
			LocalDate queryStartDate, LocalDate queryEndDate, int transactionCount,
			BigDecimal totalPurchaseAmount, long totalRewardsPoints,
			List<MonthlyRewards> monthlyRewards, List<Transaction> transactions) {
		this.customerId = customerId;
		this.customerName = customerName;
		this.email = email;
		this.queryStartDate = queryStartDate;
		this.queryEndDate = queryEndDate;
		this.transactionCount = transactionCount;
		this.totalPurchaseAmount = totalPurchaseAmount;
		this.totalRewardsPoints = totalRewardsPoints;
		this.monthlyRewards = monthlyRewards;
		this.transactions = transactions;
	}

	public Long getCustomerId() { return customerId; }
	public String getCustomerName() { return customerName; }
	public String getEmail() { return email; }
	public LocalDate getQueryStartDate() { return queryStartDate; }
	public LocalDate getQueryEndDate() { return queryEndDate; }
	public int getTransactionCount() { return transactionCount; }
	public BigDecimal getTotalPurchaseAmount() { return totalPurchaseAmount; }
	public long getTotalRewardsPoints() { return totalRewardsPoints; }
	public List<MonthlyRewards> getMonthlyRewards() { return monthlyRewards; }
	public List<Transaction> getTransactions() { return transactions; }

	@Override
	public String toString() {
		return "RewardsResponse{customerId=" + customerId + ", customerName='" + customerName + "'"
				+ ", totalRewardsPoints=" + totalRewardsPoints + "}";
	}

	public static class MonthlyRewards {

		private final YearMonth month;
		private final int transactionCount;
		private final BigDecimal totalSpent;
		private final long rewardsEarned;

		public MonthlyRewards(YearMonth month, int transactionCount, BigDecimal totalSpent, long rewardsEarned) {
			this.month = month;
			this.transactionCount = transactionCount;
			this.totalSpent = totalSpent;
			this.rewardsEarned = rewardsEarned;
		}

		public YearMonth getMonth() { return month; }
		public int getTransactionCount() { return transactionCount; }
		public BigDecimal getTotalSpent() { return totalSpent; }
		public long getRewardsEarned() { return rewardsEarned; }

		@Override
		public String toString() {
			return "MonthlyRewards{month=" + month + ", transactionCount=" + transactionCount
					+ ", totalSpent=" + totalSpent + ", rewardsEarned=" + rewardsEarned + "}";
		}
	}
}
