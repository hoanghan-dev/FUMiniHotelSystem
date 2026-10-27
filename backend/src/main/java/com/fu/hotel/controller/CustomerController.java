package com.fu.hotel.controller;

import com.fu.hotel.dto.request.CustomerRegisterRequest;
import com.fu.hotel.dto.request.CustomerUpdateRequest;
import com.fu.hotel.dto.response.ApiResponse;
import com.fu.hotel.dto.response.CustomerResponse;
import com.fu.hotel.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // Customer self-service: View own profile
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerResponse> getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(customerService.getCurrentCustomerProfile(email));
    }

    // Customer self-service: Update own profile
    @PutMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerResponse> updateMyProfile(Authentication authentication,
                                                            @Valid @RequestBody CustomerUpdateRequest request) {
        String email = authentication.getName();
        return ResponseEntity.ok(customerService.updateCurrentCustomerProfile(email, request));
    }

    // Staff: List customers with search/filter
    @GetMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<CustomerResponse>> getAllCustomers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(customerService.getAllCustomers(keyword, status));
    }

    // Staff: Get customer by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Integer id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    // Staff: Create customer
    @PostMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRegisterRequest request) {
        return new ResponseEntity<>(customerService.createCustomer(request), HttpStatus.CREATED);
    }

    // Staff: Update customer
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable Integer id,
                                                           @Valid @RequestBody CustomerUpdateRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    // Staff: Delete customer (or deactivate if has bookings)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse> deleteCustomer(@PathVariable Integer id) {
        return ResponseEntity.ok(customerService.deleteCustomer(id));
    }
}
