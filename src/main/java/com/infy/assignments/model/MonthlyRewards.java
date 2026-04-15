package com.infy.assignments.model;

import java.math.BigDecimal;
import java.time.YearMonth;

public class MonthlyRewards {

	private YearMonth month;
	private int transactionCount;
	private BigDecimal totalSpent;
	private long rewardsEarned;

	public MonthlyRewards() {
	}

	public MonthlyRewards(YearMonth month, int transactionCount, BigDecimal totalSpent, long rewardsEarned) {
		this.month = month;
		this.transactionCount = transactionCount;
		this.totalSpent = totalSpent;
		this.rewardsEarned = rewardsEarned;
	}

	public YearMonth getMonth() {
		return month;
	}

	public void setMonth(YearMonth month) {
		this.month = month;
	}

	public int getTransactionCount() {
		return transactionCount;
	}

	public void setTransactionCount(int transactionCount) {
		this.transactionCount = transactionCount;
	}

	public BigDecimal getTotalSpent() {
		return totalSpent;
	}

	public void setTotalSpent(BigDecimal totalSpent) {
		this.totalSpent = totalSpent;
	}

	public long getRewardsEarned() {
		return rewardsEarned;
	}

	public void setRewardsEarned(long rewardsEarned) {
		this.rewardsEarned = rewardsEarned;
	}

	@Override
	public String toString() {
		return "MonthlyRewards{" +
				"month=" + month +
				", transactionCount=" + transactionCount +
				", totalSpent=" + totalSpent +
				", rewardsEarned=" + rewardsEarned +
				'}';
	}
}
