package com.fu.hotel.controller;

import com.fu.hotel.dto.request.BookingCreateRequest;
import com.fu.hotel.dto.request.BookingUpdateRequest;
import com.fu.hotel.dto.response.ApiResponse;
import com.fu.hotel.dto.response.BookingResponse;
import com.fu.hotel.service.BookingReservationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingReservationService bookingReservationService;

    public BookingController(BookingReservationService bookingReservationService) {
        this.bookingReservationService = bookingReservationService;
    }

    // Customer: Create online booking
    @PostMapping("/online")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BookingResponse> createOnlineBooking(Authentication authentication,
                                                               @Valid @RequestBody BookingCreateRequest request) {
        String email = authentication.getName();
        return new ResponseEntity<>(bookingReservationService.createOnlineBooking(email, request), HttpStatus.CREATED);
    }

    // Customer: View own booking history
    @GetMapping("/my-history")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<BookingResponse>> getMyBookingHistory(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(bookingReservationService.getCustomerBookingHistory(email));
    }

    // Staff: List all bookings with search/filter
    @GetMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<BookingResponse>> getAllBookings(
            @RequestParam(required = false) Integer customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(bookingReservationService.getAllReservations(customerId, status, fromDate, toDate));
    }

    // Staff or Customer: Get booking by ID (service enforces customer ownership)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER')")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Integer id,
                                                          Authentication authentication) {
        String email = authentication.getName();
        boolean isStaff = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));
        return ResponseEntity.ok(bookingReservationService.getReservationById(id, email, isStaff));
    }

    // Staff: Create booking
    @PostMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<BookingResponse> createStaffBooking(@Valid @RequestBody BookingCreateRequest request) {
        return new ResponseEntity<>(bookingReservationService.createStaffBooking(request), HttpStatus.CREATED);
    }

    // Staff: Update booking
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable Integer id,
                                                         @Valid @RequestBody BookingUpdateRequest request) {
        return ResponseEntity.ok(bookingReservationService.updateBooking(id, request));
    }

    // Staff: Delete booking
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse> deleteBooking(@PathVariable Integer id) {
        return ResponseEntity.ok(bookingReservationService.deleteBooking(id));
    }
}
