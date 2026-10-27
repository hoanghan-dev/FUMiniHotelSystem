package com.fu.hotel.repository;

import com.fu.hotel.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RepositoryIntegrationTest {

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private RoomInformationRepository roomInformationRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BookingReservationRepository bookingReservationRepository;

    @Autowired
    private BookingDetailRepository bookingDetailRepository;

    @Test
    @DisplayName("Should persist and query entities properly in JPA")
    void testPersistAndQueryEntities() {
        // RoomType
        RoomType roomType = new RoomType(null, "Standard", "Standard Room", "Single Bed");
        roomType = roomTypeRepository.save(roomType);
        assertThat(roomType.getRoomTypeID()).isNotNull();

        // RoomInformation
        RoomInformation room = new RoomInformation(null, "101", "Cozy Room", 2, roomType, "1", new BigDecimal("100.00"));
        room = roomInformationRepository.save(room);
        assertThat(room.getRoomID()).isNotNull();

        // Customer
        Customer customer = new Customer(null, "John Doe", "0901234567", "john@example.com",
                LocalDate.of(1995, 5, 20), "1", "hashedPassword");
        customer = customerRepository.save(customer);
        assertThat(customer.getCustomerID()).isNotNull();

        // BookingReservation
        BookingReservation reservation = new BookingReservation(null, LocalDate.now(), new BigDecimal("200.00"), customer, "1");
        reservation = bookingReservationRepository.save(reservation);
        assertThat(reservation.getBookingReservationID()).isNotNull();

        // BookingDetail
        BookingDetail detail = new BookingDetail(reservation, room,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), new BigDecimal("200.00"));
        detail = bookingDetailRepository.save(detail);

        // Verification
        assertThat(bookingDetailRepository.existsByRoom_RoomID(room.getRoomID())).isTrue();
        assertThat(bookingReservationRepository.existsByCustomer_CustomerID(customer.getCustomerID())).isTrue();

        // Overlap verification
        List<BookingDetail> overlaps = bookingDetailRepository.findOverlappingBookings(
                room.getRoomID(), LocalDate.now().plusDays(2), LocalDate.now().plusDays(4));
        assertThat(overlaps).hasSize(1);
    }
}
