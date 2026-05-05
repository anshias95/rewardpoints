package com.infy.assignments.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.infy.assignments.model.Transaction;

class DataServiceTest {

	private DataService dataService;

	@BeforeEach
	void setUp() {
		dataService = new DataService();
	}

	@Test
	void getTransactionsByCustomerId_returnsOnlyMatchingCustomer() {
		List<Transaction> transactions = dataService.getTransactionsByCustomerId(1L);

		assertNotNull(transactions);
		assertTrue(transactions.size() > 0);
		transactions.forEach(txn -> assertEquals(1L, txn.getCustomerId()));
	}

	@Test
	void getTransactionsByCustomerId_eachKnownCustomerHasTransactions() {
		assertTrue(dataService.getTransactionsByCustomerId(1L).size() > 0);
		assertTrue(dataService.getTransactionsByCustomerId(2L).size() > 0);
		assertTrue(dataService.getTransactionsByCustomerId(3L).size() > 0);
	}

	@Test
	void getTransactionsByCustomerId_unknownCustomerReturnsEmptyList() {
		List<Transaction> transactions = dataService.getTransactionsByCustomerId(999L);

		assertNotNull(transactions);
		assertEquals(0, transactions.size());
	}

	@Test
	void getAllTransactions_returnsAllRecords() {
		List<Transaction> all = dataService.getAllTransactions();

		assertNotNull(all);
		int expected = dataService.getTransactionsByCustomerId(1L).size()
				+ dataService.getTransactionsByCustomerId(2L).size()
				+ dataService.getTransactionsByCustomerId(3L).size();
		assertEquals(expected, all.size());
	}

	@Test
	void getAllTransactions_returnsDefensiveCopy() {
		List<Transaction> first = dataService.getAllTransactions();
		first.clear();
		List<Transaction> second = dataService.getAllTransactions();
		assertTrue(second.size() > 0, "Mutating the returned list must not affect the store");
	}

	@Test
	void getTransactionsByCustomerId_allAmountsArePositive() {
		dataService.getAllTransactions().forEach(txn -> {
			assertNotNull(txn.getAmount());
			assertTrue(txn.getAmount().compareTo(java.math.BigDecimal.ZERO) > 0);
		});
	}

	@Test
	void getTransactionsByCustomerId_allFieldsNonNull() {
		dataService.getAllTransactions().forEach(txn -> {
			assertNotNull(txn.getCustomerId());
			assertNotNull(txn.getAmount());
			assertNotNull(txn.getTransactionDate());
		});
	}
}
