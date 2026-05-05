package com.infy.assignments.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaction {

	private final Long id;
	private final Long customerId;
	private final BigDecimal amount;
	private final LocalDate transactionDate;

	public Transaction(Long id, Long customerId, BigDecimal amount, LocalDate transactionDate) {
		this.id = id;
		this.customerId = customerId;
		this.amount = amount;
		this.transactionDate = transactionDate;
	}

	public Long getId() { return id; }
	public Long getCustomerId() { return customerId; }
	public BigDecimal getAmount() { return amount; }
	public LocalDate getTransactionDate() { return transactionDate; }

	@Override
	public String toString() {
		return "Transaction{id=" + id + ", customerId=" + customerId
				+ ", amount=" + amount + ", transactionDate=" + transactionDate + "}";
	}
}
