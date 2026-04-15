package com.infy.assignments.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaction {

	private Long id;
	private Long customerId;
	private BigDecimal amount;
	private LocalDate transactionDate;

	public Transaction() {
	}

	public Transaction(Long customerId, BigDecimal amount, LocalDate transactionDate) {
		this.customerId = customerId;
		this.amount = amount;
		this.transactionDate = transactionDate;
	}

	public Transaction(Long id, Long customerId, BigDecimal amount, LocalDate transactionDate) {
		this.id = id;
		this.customerId = customerId;
		this.amount = amount;
		this.transactionDate = transactionDate;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public LocalDate getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(LocalDate transactionDate) {
		this.transactionDate = transactionDate;
	}

	@Override
	public String toString() {
		return "Transaction{" +
				"id=" + id +
				", customerId=" + customerId +
				", amount=" + amount +
				", transactionDate=" + transactionDate +
				'}';
	}
}