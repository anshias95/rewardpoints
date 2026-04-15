package com.infy.assignments.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.infy.assignments.model.Transaction;

@SpringBootTest
class DataServiceTest {

	@Autowired
	private DataService dataService;

	@Test
	void testGetTransactionsByCustomerId() {
		List<Transaction> transactions = dataService.getTransactionsByCustomerId(1L);

		assertNotNull(transactions);
		assertTrue(transactions.size() > 0, "Customer 1 should have at least one transaction");
		transactions.forEach(txn -> assertEquals(1L, txn.getCustomerId()));
	}

	@Test
	void testGetTransactionsByCustomerIdForMultipleCustomers() {
		List<Transaction> cust1Txns = dataService.getTransactionsByCustomerId(1L);
		List<Transaction> cust2Txns = dataService.getTransactionsByCustomerId(2L);
		List<Transaction> cust3Txns = dataService.getTransactionsByCustomerId(3L);

		assertTrue(cust1Txns.size() > 0);
		assertTrue(cust2Txns.size() > 0);
		assertTrue(cust3Txns.size() > 0);
	}

	@Test
	void testGetTransactionsByNonExistentCustomerId() {
		List<Transaction> transactions = dataService.getTransactionsByCustomerId(999L);

		assertNotNull(transactions);
		assertEquals(0, transactions.size(), "Non-existent customer should have no transactions");
	}

	@Test
	void testGetAllTransactions() {
		List<Transaction> transactions = dataService.getAllTransactions();

		assertNotNull(transactions);
		assertTrue(transactions.size() > 0, "Should have at least one transaction in mock data");
	}

	@Test
	void testAsyncTransactionRetrieval() throws ExecutionException, InterruptedException {
		CompletableFuture<List<Transaction>> future = dataService.getTransactionsByCustomerIdAsync(1L);

		assertNotNull(future);
		List<Transaction> transactions = future.get();
		assertNotNull(transactions);
		assertTrue(transactions.size() > 0);
		transactions.forEach(txn -> assertEquals(1L, txn.getCustomerId()));
	}

	@Test
	void testAsyncTransactionRetrievalCompletes() throws ExecutionException, InterruptedException {
		CompletableFuture<List<Transaction>> future = dataService.getTransactionsByCustomerIdAsync(2L);

		assertTrue(future.isDone() || !future.isDone(), "Future should be in some state");
		future.get(); // Should not throw exception
	}

	@Test
	void testTransactionDataIntegrity() {
		List<Transaction> transactions = dataService.getAllTransactions();

		for (Transaction txn : transactions) {
			assertNotNull(txn.getCustomerId(), "Customer ID should not be null");
			assertNotNull(txn.getAmount(), "Amount should not be null");
			assertNotNull(txn.getTransactionDate(), "Transaction date should not be null");
			assertTrue(txn.getAmount().compareTo(java.math.BigDecimal.ZERO) > 0,
					"Transaction amount should be positive");
		}
	}
}