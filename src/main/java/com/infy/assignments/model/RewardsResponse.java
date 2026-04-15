package com.infy.assignments.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RewardsResponse {

	private Long customerId;
	private String customerName;
	private String email;
	private LocalDate queryStartDate;
	private LocalDate queryEndDate;
	private int transactionCount;
	private BigDecimal totalPurchaseAmount;
	private long totalRewardsPoints;
	private List<MonthlyRewards> monthlyRewards;
	private List<Transaction> transactions;

	public RewardsResponse() {
	}

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

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public LocalDate getQueryStartDate() {
		return queryStartDate;
	}

	public void setQueryStartDate(LocalDate queryStartDate) {
		this.queryStartDate = queryStartDate;
	}

	public LocalDate getQueryEndDate() {
		return queryEndDate;
	}

	public void setQueryEndDate(LocalDate queryEndDate) {
		this.queryEndDate = queryEndDate;
	}

	public int getTransactionCount() {
		return transactionCount;
	}

	public void setTransactionCount(int transactionCount) {
		this.transactionCount = transactionCount;
	}

	public BigDecimal getTotalPurchaseAmount() {
		return totalPurchaseAmount;
	}

	public void setTotalPurchaseAmount(BigDecimal totalPurchaseAmount) {
		this.totalPurchaseAmount = totalPurchaseAmount;
	}

	public long getTotalRewardsPoints() {
		return totalRewardsPoints;
	}

	public void setTotalRewardsPoints(long totalRewardsPoints) {
		this.totalRewardsPoints = totalRewardsPoints;
	}

	public List<MonthlyRewards> getMonthlyRewards() {
		return monthlyRewards;
	}

	public void setMonthlyRewards(List<MonthlyRewards> monthlyRewards) {
		this.monthlyRewards = monthlyRewards;
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<Transaction> transactions) {
		this.transactions = transactions;
	}

	@Override
	public String toString() {
		return "RewardsResponse{" +
				"customerId='" + customerId + '\'' +
				", customerName='" + customerName + '\'' +
				", email='" + email + '\'' +
				", queryStartDate=" + queryStartDate +
				", queryEndDate=" + queryEndDate +
				", transactionCount=" + transactionCount +
				", totalPurchaseAmount=" + totalPurchaseAmount +
				", totalRewardsPoints=" + totalRewardsPoints +
				", monthlyRewards=" + monthlyRewards +
				", transactions=" + transactions +
				'}';
	}
}
