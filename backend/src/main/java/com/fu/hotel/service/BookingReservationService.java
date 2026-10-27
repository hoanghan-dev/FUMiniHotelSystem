package com.fu.hotel.service;

import com.fu.hotel.dto.request.BookingCreateRequest;
import com.fu.hotel.dto.request.BookingUpdateRequest;
import com.fu.hotel.dto.response.ApiResponse;
import com.fu.hotel.dto.response.BookingResponse;

import java.time.LocalDate;
import java.util.List;

public interface BookingReservationService {
    List<BookingResponse> getAllReservations(Integer customerId, String status, LocalDate fromDate, LocalDate toDate);
    BookingResponse getReservationById(Integer id, String currentUserEmail, boolean isStaff);
    BookingResponse createOnlineBooking(String customerEmail, BookingCreateRequest request);
    BookingResponse createStaffBooking(BookingCreateRequest request);
    BookingResponse updateBooking(Integer id, BookingUpdateRequest request);
    ApiResponse deleteBooking(Integer id);
    List<BookingResponse> getCustomerBookingHistory(String customerEmail);
}
