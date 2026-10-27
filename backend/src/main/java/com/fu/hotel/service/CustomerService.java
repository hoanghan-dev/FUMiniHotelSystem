package com.fu.hotel.service;

import com.fu.hotel.dto.request.CustomerRegisterRequest;
import com.fu.hotel.dto.request.CustomerUpdateRequest;
import com.fu.hotel.dto.response.ApiResponse;
import com.fu.hotel.dto.response.CustomerResponse;

import java.util.List;

public interface CustomerService {
    List<CustomerResponse> getAllCustomers(String keyword, String status);
    CustomerResponse getCustomerById(Integer id);
    CustomerResponse createCustomer(CustomerRegisterRequest request);
    CustomerResponse updateCustomer(Integer id, CustomerUpdateRequest request);
    ApiResponse deleteCustomer(Integer id);

    CustomerResponse getCurrentCustomerProfile(String email);
    CustomerResponse updateCurrentCustomerProfile(String email, CustomerUpdateRequest request);
}
