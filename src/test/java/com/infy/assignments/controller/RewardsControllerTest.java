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

	private static final String CALCULATE_URL = "/api/v1/rewards/calculate";

	@Autowired
	private WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	// --- Happy path ---

	@Test
	void calculateRewards_withBothDates_returnsAllCustomers() throws Exception {
		mockMvc.perform(get(CALCULATE_URL)
				.param("startDate", "2026-01-01")
				.param("endDate", "2026-03-31"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(3));
	}

	@Test
	void calculateRewards_withBothDates_returnsCorrectStructurePerCustomer() throws Exception {
		mockMvc.perform(get(CALCULATE_URL)
				.param("startDate", "2026-01-01")
				.param("endDate", "2026-03-31"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].customerId").value(1))
				.andExpect(jsonPath("$[0].customerName").value("John Doe"))
				.andExpect(jsonPath("$[0].email").value("john.doe@example.com"))
				.andExpect(jsonPath("$[0].queryStartDate").value("2026-01-01"))
				.andExpect(jsonPath("$[0].queryEndDate").value("2026-03-31"))
				.andExpect(jsonPath("$[0].totalRewardsPoints").isNumber())
				.andExpect(jsonPath("$[0].transactionCount").isNumber())
				.andExpect(jsonPath("$[0].totalPurchaseAmount").isNumber())
				.andExpect(jsonPath("$[0].monthlyRewards").isArray())
				.andExpect(jsonPath("$[0].transactions").isArray());
	}

	@Test
	void calculateRewards_withNoDates_defaultsToLastThreeMonths() throws Exception {
		mockMvc.perform(get(CALCULATE_URL))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(3));
	}

	@Test
	void calculateRewards_withBothDates_monthlyBreakdownPresent() throws Exception {
		mockMvc.perform(get(CALCULATE_URL)
				.param("startDate", "2026-01-01")
				.param("endDate", "2026-03-31"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].monthlyRewards[0].transactionCount").isNumber())
				.andExpect(jsonPath("$[0].monthlyRewards[0].totalSpent").isNumber())
				.andExpect(jsonPath("$[0].monthlyRewards[0].rewardsEarned").isNumber());
	}

	@Test
	void calculateRewards_withDateRangeContainingNoTransactions_returnsZeroPoints() throws Exception {
		mockMvc.perform(get(CALCULATE_URL)
				.param("startDate", "2025-01-01")
				.param("endDate", "2025-01-31"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].totalRewardsPoints").value(0))
				.andExpect(jsonPath("$[0].transactionCount").value(0));
	}

	// --- Validation errors ---

	@Test
	void calculateRewards_withOnlyStartDate_returnsBadRequest() throws Exception {
		mockMvc.perform(get(CALCULATE_URL)
				.param("startDate", "2026-01-01"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
	}

	@Test
	void calculateRewards_withOnlyEndDate_returnsBadRequest() throws Exception {
		mockMvc.perform(get(CALCULATE_URL)
				.param("endDate", "2026-03-31"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
	}

	@Test
	void calculateRewards_withStartDateAfterEndDate_returnsBadRequest() throws Exception {
		mockMvc.perform(get(CALCULATE_URL)
				.param("startDate", "2026-03-31")
				.param("endDate", "2026-01-01"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
	}

	@Test
	void calculateRewards_withFutureEndDate_returnsBadRequest() throws Exception {
		mockMvc.perform(get(CALCULATE_URL)
				.param("startDate", "2026-01-01")
				.param("endDate", "2099-12-31"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
	}

	@Test
	void calculateRewards_withInvalidStartDateFormat_returnsBadRequest() throws Exception {
		mockMvc.perform(get(CALCULATE_URL)
				.param("startDate", "01-01-2026")
				.param("endDate", "2026-03-31"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
	}

	@Test
	void calculateRewards_withInvalidEndDateFormat_returnsBadRequest() throws Exception {
		mockMvc.perform(get(CALCULATE_URL)
				.param("startDate", "2026-01-01")
				.param("endDate", "not-a-date"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
	}

	@Test
	void calculateRewards_withSameStartAndEndDate_returnsOk() throws Exception {
		mockMvc.perform(get(CALCULATE_URL)
				.param("startDate", "2026-01-10")
				.param("endDate", "2026-01-10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}
}
