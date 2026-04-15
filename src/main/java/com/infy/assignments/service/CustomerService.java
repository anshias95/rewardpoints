package com.infy.assignments.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import com.infy.assignments.model.Customer;

@Service
public class CustomerService {

	private static final Customer[] CUSTOMERS = {
		new Customer(1L, "John Doe", "john.doe@example.com"),
		new Customer(2L, "Jane Smith", "jane.smith@example.com"),
		new Customer(3L, "Bob Johnson", "bob.johnson@example.com")
	};

	public Optional<Customer> getCustomer(Long customerId) {
		for (Customer customer : CUSTOMERS) {
			if (customer.getId().equals(customerId)) {
				return Optional.of(customer);
			}
		}
		return Optional.empty();
	}
}