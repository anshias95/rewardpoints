package com.infy.assignments.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.infy.assignments.model.Transaction;

@Service
public class DataService {

	private static final Logger logger = LoggerFactory.getLogger(DataService.class);
	private final List<Transaction> mockTransactions;

	public DataService() {
		this.mockTransactions = initializeMockData();
	}

	private List<Transaction> initializeMockData() {
		List<Transaction> transactions = new ArrayList<>();

		// Customer 1
		transactions.add(new Transaction(1L, new BigDecimal("120.00"), LocalDate.of(2026, 1, 10)));
		transactions.add(new Transaction(1L, new BigDecimal("75.50"), LocalDate.of(2026, 1, 20)));
		transactions.add(new Transaction(1L, new BigDecimal("45.00"), LocalDate.of(2026, 1, 28)));
		transactions.add(new Transaction(1L, new BigDecimal("200.00"), LocalDate.of(2026, 2, 5)));
		transactions.add(new Transaction(1L, new BigDecimal("60.00"), LocalDate.of(2026, 2, 15)));
		transactions.add(new Transaction(1L, new BigDecimal("150.00"), LocalDate.of(2026, 3, 10)));
		transactions.add(new Transaction(1L, new BigDecimal("55.25"), LocalDate.of(2026, 3, 22)));

		// Customer 2
		transactions.add(new Transaction(2L, new BigDecimal("95.99"), LocalDate.of(2026, 1, 5)));
		transactions.add(new Transaction(2L, new BigDecimal("140.00"), LocalDate.of(2026, 1, 18)));
		transactions.add(new Transaction(2L, new BigDecimal("35.50"), LocalDate.of(2026, 2, 10)));
		transactions.add(new Transaction(2L, new BigDecimal("250.00"), LocalDate.of(2026, 2, 20)));
		transactions.add(new Transaction(2L, new BigDecimal("80.00"), LocalDate.of(2026, 3, 8)));
		transactions.add(new Transaction(2L, new BigDecimal("110.50"), LocalDate.of(2026, 3, 25)));

		// Customer 3
		transactions.add(new Transaction(3L, new BigDecimal("50.00"), LocalDate.of(2026, 1, 12)));
		transactions.add(new Transaction(3L, new BigDecimal("175.00"), LocalDate.of(2026, 1, 25)));
		transactions.add(new Transaction(3L, new BigDecimal("65.75"), LocalDate.of(2026, 2, 8)));
		transactions.add(new Transaction(3L, new BigDecimal("300.00"), LocalDate.of(2026, 2, 28)));
		transactions.add(new Transaction(3L, new BigDecimal("90.00"), LocalDate.of(2026, 3, 12)));

		logger.info("Initialized {} mock transactions", transactions.size());
		return transactions;
	}

	public CompletableFuture<List<Transaction>> getTransactionsByCustomerIdAsync(Long customerId) {
		logger.debug("Fetching transactions asynchronously for customer: {}", customerId);
		return CompletableFuture.supplyAsync(() -> getTransactionsByCustomerId(customerId));
	}

	public List<Transaction> getTransactionsByCustomerId(Long customerId) {
		logger.debug("Fetching transactions for customer: {}", customerId);
		return mockTransactions.stream()
				.filter(txn -> txn.getCustomerId().equals(customerId))
				.collect(java.util.stream.Collectors.toList());
	}

	public List<Transaction> getAllTransactions() {
		return new ArrayList<>(mockTransactions);
	}
}