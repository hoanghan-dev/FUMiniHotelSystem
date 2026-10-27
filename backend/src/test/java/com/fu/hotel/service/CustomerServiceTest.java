package com.fu.hotel.service;

import com.fu.hotel.config.StaffProperties;
import com.fu.hotel.dto.request.CustomerUpdateRequest;
import com.fu.hotel.dto.response.ApiResponse;
import com.fu.hotel.entity.Customer;
import com.fu.hotel.exception.ConflictException;
import com.fu.hotel.repository.BookingReservationRepository;
import com.fu.hotel.repository.CustomerRepository;
import com.fu.hotel.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BookingReservationRepository bookingReservationRepository;

    @Mock
    private StaffProperties staffProperties;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer(1, "John Doe", "0901234567", "john@example.com",
                LocalDate.of(1995, 5, 20), "1", "hashedPassword");
    }

    @Test
    @DisplayName("Delete customer with booking reservations deactivates account instead of physical delete")
    void testDeleteCustomerWithBookingsDeactivates() {
        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
        when(bookingReservationRepository.existsByCustomer_CustomerID(1)).thenReturn(true);

        ApiResponse response = customerService.deleteCustomer(1);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getActionTaken()).isEqualTo("DEACTIVATED");
        assertThat(customer.getCustomerStatus()).isEqualTo("0");
        verify(customerRepository, times(1)).save(customer);
        verify(customerRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Delete customer without booking reservations physically deletes account")
    void testDeleteCustomerWithoutBookingsDeletes() {
        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
        when(bookingReservationRepository.existsByCustomer_CustomerID(1)).thenReturn(false);

        ApiResponse response = customerService.deleteCustomer(1);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getActionTaken()).isEqualTo("DELETED");
        verify(customerRepository, times(1)).delete(customer);
        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update customer profile throws ConflictException when new email is used by another customer")
    void testUpdateCustomerConflictEmailThrowsConflict() {
        CustomerUpdateRequest request = new CustomerUpdateRequest(
                "John Updated", "0901234567", "taken@example.com", LocalDate.of(1995, 5, 20), null, "1");

        when(customerRepository.findByEmailAddress("john@example.com")).thenReturn(Optional.of(customer));
        when(customerRepository.existsByEmailAddressAndCustomerIDNot("taken@example.com", 1)).thenReturn(true);

        assertThatThrownBy(() -> customerService.updateCurrentCustomerProfile("john@example.com", request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already in use");
    }
}
