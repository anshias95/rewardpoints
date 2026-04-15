package com.infy.assignments.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class RewardsControllerTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	void testGetRewardsWithValidCustomerAndDates() throws Exception {
		mockMvc.perform(get("/api/rewards/1")
				.param("startDate", "2026-01-01")
				.param("endDate", "2026-03-31"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.customerId").value(1))
				.andExpect(jsonPath("$.customerName").value("John Doe"))
				.andExpect(jsonPath("$.email").value("john.doe@example.com"))
				.andExpect(jsonPath("$.queryStartDate").value("2026-01-01"))
				.andExpect(jsonPath("$.queryEndDate").value("2026-03-31"))
				.andExpect(jsonPath("$.totalRewardsPoints").isNumber())
				.andExpect(jsonPath("$.monthlyRewards").isArray());
	}

	@Test
	void testGetRewardsWithValidCustomerNoDateRange() throws Exception {
		mockMvc.perform(get("/api/rewards/2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.customerId").value(2))
				.andExpect(jsonPath("$.customerName").value("Jane Smith"));
	}

	@Test
	void testGetRewardsWithInvalidCustomerId() throws Exception {
		mockMvc.perform(get("/api/rewards/999")
				.param("startDate", "2026-01-01")
				.param("endDate", "2026-03-31"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.errorCode").value("CUSTOMER_NOT_FOUND"));
	}

	@Test
	void testGetRewardsWithInvalidDateFormat() throws Exception {
		mockMvc.perform(get("/api/rewards/1")
				.param("startDate", "invalid-date")
				.param("endDate", "2026-03-31"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
	}

	@Test
	void testGetRewardsWithStartDateAfterEndDate() throws Exception {
		mockMvc.perform(get("/api/rewards/1")
				.param("startDate", "2026-03-31")
				.param("endDate", "2026-01-01"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
	}

	@Test
	void testGetRewardsWithFutureEndDate() throws Exception {
		mockMvc.perform(get("/api/rewards/1")
				.param("startDate", "2026-01-01")
				.param("endDate", "2099-12-31"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
	}

	@Test
	void testGetRewardsContainsTransactionDetails() throws Exception {
		mockMvc.perform(get("/api/rewards/1")
				.param("startDate", "2026-01-01")
				.param("endDate", "2026-03-31"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.transactions").isArray())
				.andExpect(jsonPath("$.transactionCount").isNumber())
				.andExpect(jsonPath("$.totalPurchaseAmount").isNumber());
	}

	@Test
	void testGetRewardsContainsMonthlyBreakdown() throws Exception {
		mockMvc.perform(get("/api/rewards/1")
				.param("startDate", "2026-01-01")
				.param("endDate", "2026-03-31"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.monthlyRewards").isArray())
				.andExpect(jsonPath("$.monthlyRewards[0].month").exists())
				.andExpect(jsonPath("$.monthlyRewards[0].transactionCount").isNumber())
				.andExpect(jsonPath("$.monthlyRewards[0].totalSpent").isNumber())
				.andExpect(jsonPath("$.monthlyRewards[0].rewardsEarned").isNumber());
	}

	@Test
	void testGetRewardsForMultipleCustomers() throws Exception {
		// Test customer 1
		mockMvc.perform(get("/api/rewards/1")
				.param("startDate", "2026-01-01")
				.param("endDate", "2026-03-31"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.customerId").value(1));

		// Test customer 2
		mockMvc.perform(get("/api/rewards/2")
				.param("startDate", "2026-01-01")
				.param("endDate", "2026-03-31"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.customerId").value(2));

		// Test customer 3
		mockMvc.perform(get("/api/rewards/3")
				.param("startDate", "2026-01-01")
				.param("endDate", "2026-03-31"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.customerId").value(3));
	}

	@Test
	void testGetRewardsResponseStructure() throws Exception {
		mockMvc.perform(get("/api/rewards/1")
				.param("startDate", "2026-01-01")
				.param("endDate", "2026-03-31"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.customerId").exists())
				.andExpect(jsonPath("$.customerName").exists())
				.andExpect(jsonPath("$.email").exists())
				.andExpect(jsonPath("$.queryStartDate").exists())
				.andExpect(jsonPath("$.queryEndDate").exists())
				.andExpect(jsonPath("$.transactionCount").exists())
				.andExpect(jsonPath("$.totalPurchaseAmount").exists())
				.andExpect(jsonPath("$.totalRewardsPoints").exists())
				.andExpect(jsonPath("$.monthlyRewards").exists())
				.andExpect(jsonPath("$.transactions").exists());
	}
}