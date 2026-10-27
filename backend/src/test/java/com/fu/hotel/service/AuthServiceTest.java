package com.fu.hotel.service;

import com.fu.hotel.config.StaffProperties;
import com.fu.hotel.dto.request.AuthRequest;
import com.fu.hotel.dto.request.CustomerRegisterRequest;
import com.fu.hotel.dto.response.AuthResponse;
import com.fu.hotel.dto.response.CustomerResponse;
import com.fu.hotel.entity.Customer;
import com.fu.hotel.exception.ConflictException;
import com.fu.hotel.exception.ForbiddenException;
import com.fu.hotel.exception.UnauthorizedException;
import com.fu.hotel.repository.CustomerRepository;
import com.fu.hotel.security.JwtTokenProvider;
import com.fu.hotel.service.impl.AuthServiceImpl;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private StaffProperties staffProperties;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private Customer activeCustomer;
    private Customer inactiveCustomer;

    @BeforeEach
    void setUp() {
        activeCustomer = new Customer(1, "Alice", "0912345678", "alice@hotel.com",
                LocalDate.of(1996, 4, 10), "1", "hashedPassword");

        inactiveCustomer = new Customer(2, "Bob", "0987654321", "bob@hotel.com",
                LocalDate.of(1997, 6, 15), "0", "hashedPassword");
    }

    @Test
    @DisplayName("Staff from properties can log in and receives JWT with role STAFF")
    void testStaffLoginSuccess() {
        when(staffProperties.getEmail()).thenReturn("staff@fuminihotel.com");
        when(staffProperties.getPassword()).thenReturn("Staff@123");
        when(tokenProvider.generateToken("staff@fuminihotel.com", "STAFF", null)).thenReturn("mockStaffToken");

        AuthResponse response = authService.login(new AuthRequest("staff@fuminihotel.com", "Staff@123"));

        assertThat(response).isNotNull();
        assertThat(response.getRole()).isEqualTo("STAFF");
        assertThat(response.getToken()).isEqualTo("mockStaffToken");
        assertThat(response.getEmail()).isEqualTo("staff@fuminihotel.com");
        verify(customerRepository, never()).findByEmailAddress(any());
    }

    @Test
    @DisplayName("Customer can log in and receives JWT with role CUSTOMER")
    void testCustomerLoginSuccess() {
        when(staffProperties.getEmail()).thenReturn("staff@fuminihotel.com");
        when(customerRepository.findByEmailAddress("alice@hotel.com")).thenReturn(Optional.of(activeCustomer));
        when(passwordEncoder.matches("Alice@123", "hashedPassword")).thenReturn(true);
        when(tokenProvider.generateToken("alice@hotel.com", "CUSTOMER", 1)).thenReturn("mockCustomerToken");

        AuthResponse response = authService.login(new AuthRequest("alice@hotel.com", "Alice@123"));

        assertThat(response).isNotNull();
        assertThat(response.getRole()).isEqualTo("CUSTOMER");
        assertThat(response.getCustomerId()).isEqualTo(1);
        assertThat(response.getToken()).isEqualTo("mockCustomerToken");
    }

    @Test
    @DisplayName("Inactive customer is blocked from logging in with ForbiddenException")
    void testInactiveCustomerLoginBlocked() {
        when(staffProperties.getEmail()).thenReturn("staff@fuminihotel.com");
        when(customerRepository.findByEmailAddress("bob@hotel.com")).thenReturn(Optional.of(inactiveCustomer));
        when(passwordEncoder.matches("Bob@123", "hashedPassword")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new AuthRequest("bob@hotel.com", "Bob@123")))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Account is inactive");
    }

    @Test
    @DisplayName("Login with invalid password throws UnauthorizedException")
    void testInvalidPasswordThrowsUnauthorized() {
        when(staffProperties.getEmail()).thenReturn("staff@fuminihotel.com");
        when(customerRepository.findByEmailAddress("alice@hotel.com")).thenReturn(Optional.of(activeCustomer));
        when(passwordEncoder.matches("WrongPassword", "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new AuthRequest("alice@hotel.com", "WrongPassword")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("Customer registration throws ConflictException on duplicate email")
    void testRegisterDuplicateEmailThrowsConflict() {
        CustomerRegisterRequest request = new CustomerRegisterRequest(
                "Alice Again", "0912345678", "alice@hotel.com", LocalDate.of(1996, 4, 10), "Secret@123");

        when(customerRepository.existsByEmailAddress("alice@hotel.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already registered");
    }
}
