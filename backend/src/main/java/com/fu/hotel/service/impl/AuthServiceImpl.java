package com.fu.hotel.service.impl;

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
import com.fu.hotel.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final StaffProperties staffProperties;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthServiceImpl(StaffProperties staffProperties,
                           CustomerRepository customerRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider tokenProvider) {
        this.staffProperties = staffProperties;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        String email = request.getEmail().trim();
        String password = request.getPassword();

        // 1. Staff login check from application.properties
        if (staffProperties.getEmail() != null && staffProperties.getEmail().equalsIgnoreCase(email)) {
            if (!staffProperties.getPassword().equals(password)) {
                throw new UnauthorizedException("Invalid email or password");
            }
            String token = tokenProvider.generateToken(staffProperties.getEmail(), "STAFF", null);
            return new AuthResponse(token, "STAFF", null, "Hotel Staff", staffProperties.getEmail());
        }

        // 2. Customer login check
        Customer customer = customerRepository.findByEmailAddress(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(password, customer.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        // Check if customer status is inactive
        boolean isActive = customer.getCustomerStatus() != null &&
                (customer.getCustomerStatus().equals("1") || customer.getCustomerStatus().equalsIgnoreCase("Active"));
        if (!isActive) {
            throw new ForbiddenException("Account is inactive. Please contact staff for assistance");
        }

        String token = tokenProvider.generateToken(customer.getEmailAddress(), "CUSTOMER", customer.getCustomerID());
        return new AuthResponse(token, "CUSTOMER", customer.getCustomerID(), customer.getCustomerFullName(), customer.getEmailAddress());
    }

    @Override
    @Transactional
    public CustomerResponse register(CustomerRegisterRequest request) {
        String email = request.getEmailAddress().trim();

        // Check if email collides with staff email
        if (staffProperties.getEmail() != null && staffProperties.getEmail().equalsIgnoreCase(email)) {
            throw new ConflictException("Email address is already in use");
        }

        // Check if email exists in Customer table
        if (customerRepository.existsByEmailAddress(email)) {
            throw new ConflictException("Email address is already registered");
        }

        Customer customer = new Customer();
        customer.setCustomerFullName(request.getCustomerFullName().trim());
        customer.setTelephone(request.getTelephone().trim());
        customer.setEmailAddress(email);
        customer.setCustomerBirthday(request.getCustomerBirthday());
        customer.setCustomerStatus("1"); // Active
        customer.setPassword(passwordEncoder.encode(request.getPassword()));

        customer = customerRepository.save(customer);

        return new CustomerResponse(
                customer.getCustomerID(),
                customer.getCustomerFullName(),
                customer.getTelephone(),
                customer.getEmailAddress(),
                customer.getCustomerBirthday(),
                customer.getCustomerStatus()
        );
    }
}
