package com.fu.hotel.service;

import com.fu.hotel.dto.request.AuthRequest;
import com.fu.hotel.dto.request.CustomerRegisterRequest;
import com.fu.hotel.dto.response.AuthResponse;
import com.fu.hotel.dto.response.CustomerResponse;

public interface AuthService {
    AuthResponse login(AuthRequest request);
    CustomerResponse register(CustomerRegisterRequest request);
}
