package com.fu.hotel.security;

import com.fu.hotel.config.StaffProperties;
import com.fu.hotel.entity.Customer;
import com.fu.hotel.repository.CustomerRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;
    private final StaffProperties staffProperties;
    private final PasswordEncoder passwordEncoder;

    public CustomUserDetailsService(CustomerRepository customerRepository,
                                    StaffProperties staffProperties,
                                    @Lazy PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.staffProperties = staffProperties;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Check if email matches Staff from properties
        if (staffProperties.getEmail() != null && staffProperties.getEmail().equalsIgnoreCase(username)) {
            String encodedStaffPass = passwordEncoder.encode(staffProperties.getPassword());
            return new UserPrincipal(
                    staffProperties.getEmail(),
                    encodedStaffPass,
                    "STAFF",
                    null,
                    "Hotel Staff",
                    true
            );
        }

        // 2. Look up in Customer repository
        Customer customer = customerRepository.findByEmailAddress(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        boolean isActive = customer.getCustomerStatus() != null &&
                (customer.getCustomerStatus().equals("1") || customer.getCustomerStatus().equalsIgnoreCase("Active"));

        return new UserPrincipal(
                customer.getEmailAddress(),
                customer.getPassword(),
                "CUSTOMER",
                customer.getCustomerID(),
                customer.getCustomerFullName(),
                isActive
        );
    }
}
