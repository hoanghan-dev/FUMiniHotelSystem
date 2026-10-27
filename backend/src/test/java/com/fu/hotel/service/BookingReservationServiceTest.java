package com.fu.hotel.service;

import com.fu.hotel.dto.request.BookingCreateRequest;
import com.fu.hotel.dto.request.BookingDetailItemRequest;
import com.fu.hotel.dto.response.BookingResponse;
import com.fu.hotel.entity.*;
import com.fu.hotel.exception.BadRequestException;
import com.fu.hotel.exception.ConflictException;
import com.fu.hotel.exception.ForbiddenException;
import com.fu.hotel.repository.BookingDetailRepository;
import com.fu.hotel.repository.BookingReservationRepository;
import com.fu.hotel.repository.CustomerRepository;
import com.fu.hotel.repository.RoomInformationRepository;
import com.fu.hotel.service.impl.BookingReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingReservationServiceTest {

    @Mock
    private BookingReservationRepository reservationRepository;

    @Mock
    private BookingDetailRepository bookingDetailRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RoomInformationRepository roomRepository;

    @InjectMocks
    private BookingReservationServiceImpl bookingService;

    private Customer testCustomer;
    private RoomInformation testRoom;
    private RoomType roomType;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer(1, "John Doe", "0912345678", "john@hotel.com",
                LocalDate.of(1995, 1, 1), "1", "encodedPassword");

        roomType = new RoomType(1, "Deluxe", "Deluxe Room", "Note");
        testRoom = new RoomInformation(10, "201", "Deluxe 201", 3, roomType, "1", new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Online booking calculates price correctly: actualPrice = pricePerDay * days, totalPrice = sum")
    void testBookingPriceCalculation() {
        LocalDate start = LocalDate.now().plusDays(2);
        LocalDate end = LocalDate.now().plusDays(5); // 3 days
        BookingDetailItemRequest item = new BookingDetailItemRequest(10, start, end);
        BookingCreateRequest request = new BookingCreateRequest(null, List.of(item));

        when(customerRepository.findByEmailAddress("john@hotel.com")).thenReturn(Optional.of(testCustomer));
        when(roomRepository.findById(10)).thenReturn(Optional.of(testRoom));
        when(bookingDetailRepository.findOverlappingBookingsExcludingReservation(eq(10), eq(start), eq(end), any()))
                .thenReturn(Collections.emptyList());

        when(reservationRepository.save(any(BookingReservation.class))).thenAnswer(invocation -> {
            BookingReservation r = invocation.getArgument(0);
            if (r.getBookingReservationID() == null) {
                r.setBookingReservationID(100);
            }
            return r;
        });

        BookingResponse response = bookingService.createOnlineBooking("john@hotel.com", request);

        assertThat(response).isNotNull();
        // 3 days * 100.00 = 300.00
        assertThat(response.getTotalPrice()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(response.getBookingDetails()).hasSize(1);
        assertThat(response.getBookingDetails().get(0).getActualPrice()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(response.getBookingDetails().get(0).getNumberOfDays()).isEqualTo(3);
    }

    @Test
    @DisplayName("Booking throws BadRequestException when start date is in the past")
    void testBookingStartDateInPastThrowsBadRequest() {
        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now().plusDays(2);
        BookingDetailItemRequest item = new BookingDetailItemRequest(10, start, end);
        BookingCreateRequest request = new BookingCreateRequest(null, List.of(item));

        when(customerRepository.findByEmailAddress("john@hotel.com")).thenReturn(Optional.of(testCustomer));
        when(reservationRepository.save(any(BookingReservation.class))).thenAnswer(i -> i.getArgument(0));

        assertThatThrownBy(() -> bookingService.createOnlineBooking("john@hotel.com", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot be in the past");
    }

    @Test
    @DisplayName("Booking throws BadRequestException when start date is not before end date")
    void testBookingStartDateAfterEndDateThrowsBadRequest() {
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(2);
        BookingDetailItemRequest item = new BookingDetailItemRequest(10, start, end);
        BookingCreateRequest request = new BookingCreateRequest(null, List.of(item));

        when(customerRepository.findByEmailAddress("john@hotel.com")).thenReturn(Optional.of(testCustomer));
        when(reservationRepository.save(any(BookingReservation.class))).thenAnswer(i -> i.getArgument(0));

        assertThatThrownBy(() -> bookingService.createOnlineBooking("john@hotel.com", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must be before end date");
    }

    @Test
    @DisplayName("Booking throws BadRequestException when room is inactive")
    void testBookingInactiveRoomThrowsBadRequest() {
        testRoom.setRoomStatus("0"); // Inactive
        LocalDate start = LocalDate.now().plusDays(2);
        LocalDate end = LocalDate.now().plusDays(4);
        BookingDetailItemRequest item = new BookingDetailItemRequest(10, start, end);
        BookingCreateRequest request = new BookingCreateRequest(null, List.of(item));

        when(customerRepository.findByEmailAddress("john@hotel.com")).thenReturn(Optional.of(testCustomer));
        when(roomRepository.findById(10)).thenReturn(Optional.of(testRoom));
        when(reservationRepository.save(any(BookingReservation.class))).thenAnswer(i -> i.getArgument(0));

        assertThatThrownBy(() -> bookingService.createOnlineBooking("john@hotel.com", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("is currently inactive");
    }

    @Test
    @DisplayName("Booking throws ConflictException when room dates overlap with existing reservation")
    void testBookingOverlappingDatesThrowsConflict() {
        LocalDate start = LocalDate.now().plusDays(2);
        LocalDate end = LocalDate.now().plusDays(5);
        BookingDetailItemRequest item = new BookingDetailItemRequest(10, start, end);
        BookingCreateRequest request = new BookingCreateRequest(null, List.of(item));

        when(customerRepository.findByEmailAddress("john@hotel.com")).thenReturn(Optional.of(testCustomer));
        when(roomRepository.findById(10)).thenReturn(Optional.of(testRoom));
        when(reservationRepository.save(any(BookingReservation.class))).thenAnswer(i -> i.getArgument(0));

        BookingDetail existingDetail = new BookingDetail(new BookingReservation(), testRoom, start, end, new BigDecimal("300.00"));
        when(bookingDetailRepository.findOverlappingBookingsExcludingReservation(eq(10), eq(start), eq(end), any()))
                .thenReturn(List.of(existingDetail));

        assertThatThrownBy(() -> bookingService.createOnlineBooking("john@hotel.com", request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    @DisplayName("Customer cannot access another customer's booking reservation")
    void testCustomerCannotAccessAnotherCustomerBooking() {
        BookingReservation otherReservation = new BookingReservation(50, LocalDate.now(), new BigDecimal("200.00"), testCustomer, "1");
        when(reservationRepository.findById(50)).thenReturn(Optional.of(otherReservation));

        assertThatThrownBy(() -> bookingService.getReservationById(50, "other@hotel.com", false))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("not authorized");
    }
}
