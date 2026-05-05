package com.infy.assignments.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.infy.assignments.exception.InvalidInputException;
import com.infy.assignments.model.Customer;
import com.infy.assignments.model.RewardsResponse;
import com.infy.assignments.model.Transaction;

@ExtendWith(MockitoExtension.class)
class RewardCalculationServiceTest {

	@Mock
	private DataService dataService;

	@Mock
	private CustomerService customerService;

	private RewardCalculationService service;

	private final Customer customer1 = new Customer(1L, "John Doe", "john@example.com");
	private final Customer customer2 = new Customer(2L, "Jane Smith", "jane@example.com");

	@BeforeEach
	void setUp() {
		service = new RewardCalculationService(dataService, customerService, new PointsCalculationService());
	}

	// --- Date validation ---

	@Test
	void calculateRewards_withNoDates_usesDefaultLastThreeMonths() {
		when(customerService.getAllCustomers()).thenReturn(Collections.singletonList(customer1));
		when(customerService.getCustomer(1L)).thenReturn(Optional.of(customer1));
		when(dataService.getTransactionsByCustomerId(1L)).thenReturn(Collections.emptyList());

		List<RewardsResponse> result = service.calculateRewardsForAllCustomers(null, null);

		assertNotNull(result);
		assertEquals(1, result.size());
		// Default end date is today; start date is first day of month 3 months ago
		LocalDate expectedEnd = LocalDate.now();
		LocalDate expectedStart = expectedEnd.minusMonths(3).withDayOfMonth(1);
		assertEquals(expectedStart, result.get(0).getQueryStartDate());
		assertEquals(expectedEnd, result.get(0).getQueryEndDate());
	}

	@Test
	void calculateRewards_withEmptyStringDates_usesDefaultLastThreeMonths() {
		when(customerService.getAllCustomers()).thenReturn(Collections.singletonList(customer1));
		when(customerService.getCustomer(1L)).thenReturn(Optional.of(customer1));
		when(dataService.getTransactionsByCustomerId(1L)).thenReturn(Collections.emptyList());

		List<RewardsResponse> result = service.calculateRewardsForAllCustomers("", "");
		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void calculateRewards_withOnlyStartDate_throwsInvalidInputException() {
		assertThrows(InvalidInputException.class,
				() -> service.calculateRewardsForAllCustomers("2026-01-01", null));
	}

	@Test
	void calculateRewards_withOnlyEndDate_throwsInvalidInputException() {
		assertThrows(InvalidInputException.class,
				() -> service.calculateRewardsForAllCustomers(null, "2026-03-31"));
	}

	@Test
	void calculateRewards_withStartDateAfterEndDate_throwsInvalidInputException() {
		assertThrows(InvalidInputException.class,
				() -> service.calculateRewardsForAllCustomers("2026-03-31", "2026-01-01"));
	}

	@Test
	void calculateRewards_withFutureEndDate_throwsInvalidInputException() {
		assertThrows(InvalidInputException.class,
				() -> service.calculateRewardsForAllCustomers("2026-01-01", "2099-12-31"));
	}

	@Test
	void calculateRewards_withInvalidDateFormat_throwsInvalidInputException() {
		assertThrows(InvalidInputException.class,
				() -> service.calculateRewardsForAllCustomers("01/01/2026", "2026-03-31"));
	}

	@Test
	void calculateRewards_withSameDateForStartAndEnd_isValid() {
		when(customerService.getAllCustomers()).thenReturn(Collections.singletonList(customer1));
		when(customerService.getCustomer(1L)).thenReturn(Optional.of(customer1));
		when(dataService.getTransactionsByCustomerId(1L)).thenReturn(Collections.emptyList());

		List<RewardsResponse> result = service.calculateRewardsForAllCustomers("2026-01-10", "2026-01-10");
		assertEquals(1, result.size());
	}

	// --- Points calculation with decimal amounts ---

	@Test
	void calculateRewards_withDecimalAmountOver100_roundsHalfUp() {
		// $120.75: over-$100 portion = 20.75 * 2 = 41.5 → rounds to 42; $50–$100 = 50 → total 92
		Transaction txn = new Transaction(1L, 1L, new BigDecimal("120.75"), LocalDate.of(2026, 1, 10));
		when(customerService.getAllCustomers()).thenReturn(Collections.singletonList(customer1));
		when(customerService.getCustomer(1L)).thenReturn(Optional.of(customer1));
		when(dataService.getTransactionsByCustomerId(1L)).thenReturn(Collections.singletonList(txn));

		List<RewardsResponse> result = service.calculateRewardsForAllCustomers("2026-01-01", "2026-01-31");
		assertEquals(92L, result.get(0).getTotalRewardsPoints());
	}

	@Test
	void calculateRewards_withDecimalAmountBetween50And100_roundsHalfUp() {
		// $75.50: $50–$100 portion = 25.50 → rounds to 26 points
		Transaction txn = new Transaction(2L, 1L, new BigDecimal("75.50"), LocalDate.of(2026, 1, 20));
		when(customerService.getAllCustomers()).thenReturn(Collections.singletonList(customer1));
		when(customerService.getCustomer(1L)).thenReturn(Optional.of(customer1));
		when(dataService.getTransactionsByCustomerId(1L)).thenReturn(Collections.singletonList(txn));

		List<RewardsResponse> result = service.calculateRewardsForAllCustomers("2026-01-01", "2026-01-31");
		assertEquals(26L, result.get(0).getTotalRewardsPoints());
	}

	// --- Business logic ---

	@Test
	void calculateRewards_transactionsOutsideDateRange_areExcluded() {
		Transaction inside = new Transaction(1L, 1L, new BigDecimal("120.00"), LocalDate.of(2026, 2, 10));
		Transaction outside = new Transaction(2L, 1L, new BigDecimal("200.00"), LocalDate.of(2026, 4, 1));
		when(customerService.getAllCustomers()).thenReturn(Collections.singletonList(customer1));
		when(customerService.getCustomer(1L)).thenReturn(Optional.of(customer1));
		when(dataService.getTransactionsByCustomerId(1L)).thenReturn(Arrays.asList(inside, outside));

		List<RewardsResponse> result = service.calculateRewardsForAllCustomers("2026-01-01", "2026-03-31");
		assertEquals(1, result.get(0).getTransactionCount());
	}

	@Test
	void calculateRewards_noTransactionsInRange_returnsZeroPoints() {
		when(customerService.getAllCustomers()).thenReturn(Collections.singletonList(customer1));
		when(customerService.getCustomer(1L)).thenReturn(Optional.of(customer1));
		when(dataService.getTransactionsByCustomerId(1L)).thenReturn(Collections.emptyList());

		List<RewardsResponse> result = service.calculateRewardsForAllCustomers("2026-01-01", "2026-03-31");
		assertEquals(0, result.get(0).getTotalRewardsPoints());
		assertEquals(0, result.get(0).getTransactionCount());
	}

	@Test
	void calculateRewards_monthlyBreakdownSumsToTotal() {
		Transaction t1 = new Transaction(1L, 1L, new BigDecimal("120.00"), LocalDate.of(2026, 1, 10));
		Transaction t2 = new Transaction(2L, 1L, new BigDecimal("200.00"), LocalDate.of(2026, 2, 5));
		when(customerService.getAllCustomers()).thenReturn(Collections.singletonList(customer1));
		when(customerService.getCustomer(1L)).thenReturn(Optional.of(customer1));
		when(dataService.getTransactionsByCustomerId(1L)).thenReturn(Arrays.asList(t1, t2));

		List<RewardsResponse> result = service.calculateRewardsForAllCustomers("2026-01-01", "2026-03-31");
		RewardsResponse response = result.get(0);

		long sumFromMonthly = response.getMonthlyRewards().stream()
				.mapToLong(RewardsResponse.MonthlyRewards::getRewardsEarned)
				.sum();
		assertEquals(response.getTotalRewardsPoints(), sumFromMonthly);
	}

	@Test
	void calculateRewards_multipleCustomers_returnsOneEntryEach() {
		when(customerService.getAllCustomers()).thenReturn(Arrays.asList(customer1, customer2));
		when(customerService.getCustomer(1L)).thenReturn(Optional.of(customer1));
		when(customerService.getCustomer(2L)).thenReturn(Optional.of(customer2));
		when(dataService.getTransactionsByCustomerId(1L)).thenReturn(Collections.emptyList());
		when(dataService.getTransactionsByCustomerId(2L)).thenReturn(Collections.emptyList());

		List<RewardsResponse> result = service.calculateRewardsForAllCustomers("2026-01-01", "2026-03-31");
		assertEquals(2, result.size());
	}

	@Test
	void calculateRewards_amountBelowFifty_earnsZeroPoints() {
		Transaction txn = new Transaction(1L, 1L, new BigDecimal("45.00"), LocalDate.of(2026, 1, 10));
		when(customerService.getAllCustomers()).thenReturn(Collections.singletonList(customer1));
		when(customerService.getCustomer(1L)).thenReturn(Optional.of(customer1));
		when(dataService.getTransactionsByCustomerId(1L)).thenReturn(Collections.singletonList(txn));

		List<RewardsResponse> result = service.calculateRewardsForAllCustomers("2026-01-01", "2026-01-31");
		assertEquals(0L, result.get(0).getTotalRewardsPoints());
	}
}
