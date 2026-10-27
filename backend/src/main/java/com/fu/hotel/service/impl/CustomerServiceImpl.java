package com.fu.hotel.service.impl;

import com.fu.hotel.config.StaffProperties;
import com.fu.hotel.dto.request.CustomerRegisterRequest;
import com.fu.hotel.dto.request.CustomerUpdateRequest;
import com.fu.hotel.dto.response.ApiResponse;
import com.fu.hotel.dto.response.CustomerResponse;
import com.fu.hotel.entity.Customer;
import com.fu.hotel.exception.ConflictException;
import com.fu.hotel.exception.ResourceNotFoundException;
import com.fu.hotel.repository.BookingReservationRepository;
import com.fu.hotel.repository.CustomerRepository;
import com.fu.hotel.service.CustomerService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final BookingReservationRepository bookingReservationRepository;
    private final StaffProperties staffProperties;
    private final PasswordEncoder passwordEncoder;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                               BookingReservationRepository bookingReservationRepository,
                               StaffProperties staffProperties,
                               PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.bookingReservationRepository = bookingReservationRepository;
        this.staffProperties = staffProperties;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers(String keyword, String status) {
        List<Customer> customers;
        if (keyword == null && status == null) {
            customers = customerRepository.findAll();
        } else {
            customers = customerRepository.searchCustomers(
                    keyword != null && !keyword.isBlank() ? keyword.trim() : null,
                    status != null && !status.isBlank() ? status.trim() : null
            );
        }
        return customers.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Integer id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));
        return mapToResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerRegisterRequest request) {
        String email = request.getEmailAddress().trim();

        if (staffProperties.getEmail() != null && staffProperties.getEmail().equalsIgnoreCase(email)) {
            throw new ConflictException("Email address is already in use");
        }

        if (customerRepository.existsByEmailAddress(email)) {
            throw new ConflictException("Customer with email '" + email + "' already exists");
        }

        Customer customer = new Customer();
        customer.setCustomerFullName(request.getCustomerFullName().trim());
        customer.setTelephone(request.getTelephone().trim());
        customer.setEmailAddress(email);
        customer.setCustomerBirthday(request.getCustomerBirthday());
        customer.setCustomerStatus("1");
        customer.setPassword(passwordEncoder.encode(request.getPassword()));

        customer = customerRepository.save(customer);
        return mapToResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(Integer id, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));

        String email = request.getEmailAddress().trim();
        if (staffProperties.getEmail() != null && staffProperties.getEmail().equalsIgnoreCase(email)) {
            throw new ConflictException("Email address is already in use");
        }

        if (customerRepository.existsByEmailAddressAndCustomerIDNot(email, id)) {
            throw new ConflictException("Customer with email '" + email + "' already exists");
        }

        customer.setCustomerFullName(request.getCustomerFullName().trim());
        customer.setTelephone(request.getTelephone().trim());
        customer.setEmailAddress(email);
        customer.setCustomerBirthday(request.getCustomerBirthday());

        if (request.getCustomerStatus() != null && !request.getCustomerStatus().isBlank()) {
            customer.setCustomerStatus(request.getCustomerStatus().trim());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            customer.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        customer = customerRepository.save(customer);
        return mapToResponse(customer);
    }

    @Override
    @Transactional
    public ApiResponse deleteCustomer(Integer id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));

        // Delete rule: if customer has booking reservations -> deactivate instead of delete
        boolean hasBookings = bookingReservationRepository.existsByCustomer_CustomerID(id);
        if (hasBookings) {
            customer.setCustomerStatus("0"); // Inactive
            customerRepository.save(customer);
            return new ApiResponse(true, "Customer has booking history and was deactivated instead of deleted.", "DEACTIVATED");
        } else {
            customerRepository.delete(customer);
            return new ApiResponse(true, "Customer deleted successfully.", "DELETED");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCurrentCustomerProfile(String email) {
        Customer customer = customerRepository.findByEmailAddress(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for email: " + email));
        return mapToResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse updateCurrentCustomerProfile(String email, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findByEmailAddress(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for email: " + email));

        String newEmail = request.getEmailAddress().trim();
        if (staffProperties.getEmail() != null && staffProperties.getEmail().equalsIgnoreCase(newEmail)) {
            throw new ConflictException("Email address is already in use");
        }

        if (customerRepository.existsByEmailAddressAndCustomerIDNot(newEmail, customer.getCustomerID())) {
            throw new ConflictException("Email address is already in use by another account");
        }

        customer.setCustomerFullName(request.getCustomerFullName().trim());
        customer.setTelephone(request.getTelephone().trim());
        customer.setEmailAddress(newEmail);
        customer.setCustomerBirthday(request.getCustomerBirthday());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            customer.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        customer = customerRepository.save(customer);
        return mapToResponse(customer);
    }

    private CustomerResponse mapToResponse(Customer entity) {
        return new CustomerResponse(
                entity.getCustomerID(),
                entity.getCustomerFullName(),
                entity.getTelephone(),
                entity.getEmailAddress(),
                entity.getCustomerBirthday(),
                entity.getCustomerStatus()
        );
    }
}
