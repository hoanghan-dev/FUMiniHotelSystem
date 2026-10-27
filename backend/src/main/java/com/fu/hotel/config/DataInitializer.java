package com.fu.hotel.config;

import com.fu.hotel.entity.BookingDetail;
import com.fu.hotel.entity.BookingReservation;
import com.fu.hotel.entity.Customer;
import com.fu.hotel.entity.RoomInformation;
import com.fu.hotel.entity.RoomType;
import com.fu.hotel.repository.BookingDetailRepository;
import com.fu.hotel.repository.BookingReservationRepository;
import com.fu.hotel.repository.CustomerRepository;
import com.fu.hotel.repository.RoomInformationRepository;
import com.fu.hotel.repository.RoomTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoomTypeRepository roomTypeRepository;
    private final RoomInformationRepository roomRepository;
    private final CustomerRepository customerRepository;
    private final BookingReservationRepository reservationRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoomTypeRepository roomTypeRepository,
                           RoomInformationRepository roomRepository,
                           CustomerRepository customerRepository,
                           BookingReservationRepository reservationRepository,
                           BookingDetailRepository bookingDetailRepository,
                           PasswordEncoder passwordEncoder) {
        this.roomTypeRepository = roomTypeRepository;
        this.roomRepository = roomRepository;
        this.customerRepository = customerRepository;
        this.reservationRepository = reservationRepository;
        this.bookingDetailRepository = bookingDetailRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (roomTypeRepository.count() > 0) {
            return; // Idempotent check
        }

        // 1. Seed Room Types
        RoomType standard = roomTypeRepository.save(new RoomType(
                null,
                "Standard Room",
                "Comfortable standard room with essential amenities and air conditioning",
                "Single / Double occupancy"
        ));

        RoomType deluxe = roomTypeRepository.save(new RoomType(
                null,
                "Deluxe Room",
                "Spacious room with king bed, balcony, and scenic garden view",
                "Complimentary breakfast included"
        ));

        RoomType suite = roomTypeRepository.save(new RoomType(
                null,
                "Presidential Suite",
                "Luxurious suite featuring private living room, ocean view, and jacuzzi",
                "VIP lounge access & concierge"
        ));

        // 2. Seed Rooms
        RoomInformation room101 = roomRepository.save(new RoomInformation(
                null, "101", "Cozy standard room on 1st floor", 2, standard, "1", new BigDecimal("50.00")
        ));
        RoomInformation room102 = roomRepository.save(new RoomInformation(
                null, "102", "Quiet standard room with courtyard view", 2, standard, "1", new BigDecimal("55.00")
        ));
        RoomInformation room201 = roomRepository.save(new RoomInformation(
                null, "201", "Deluxe room with balcony and morning sunlight", 3, deluxe, "1", new BigDecimal("90.00")
        ));
        RoomInformation room202 = roomRepository.save(new RoomInformation(
                null, "202", "Spacious deluxe corner room", 4, deluxe, "1", new BigDecimal("110.00")
        ));
        RoomInformation room301 = roomRepository.save(new RoomInformation(
                null, "301", "Executive presidential suite with panoramic terrace", 4, suite, "1", new BigDecimal("220.00")
        ));
        RoomInformation room302 = roomRepository.save(new RoomInformation(
                null, "302", "Penthouse presidential suite with private rooftop", 6, suite, "1", new BigDecimal("300.00")
        ));

        // 3. Seed Customers
        Customer customer1 = customerRepository.save(new Customer(
                null,
                "John Doe",
                "0912345678",
                "customer1@hotel.com",
                LocalDate.of(1995, 5, 15),
                "1",
                passwordEncoder.encode("Customer@123")
        ));

        Customer customer2 = customerRepository.save(new Customer(
                null,
                "Jane Smith",
                "0987654321",
                "customer2@hotel.com",
                LocalDate.of(1998, 8, 20),
                "1",
                passwordEncoder.encode("Customer@123")
        ));

        // 4. Seed a sample past/active reservation for customer1 with room101
        LocalDate bookingDate = LocalDate.now().minusDays(5);
        LocalDate startDate = LocalDate.now().plusDays(10);
        LocalDate endDate = LocalDate.now().plusDays(13);
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        BigDecimal actualPrice = room101.getRoomPricePerDay().multiply(BigDecimal.valueOf(days));

        BookingReservation sampleReservation = new BookingReservation();
        sampleReservation.setBookingDate(bookingDate);
        sampleReservation.setTotalPrice(actualPrice);
        sampleReservation.setCustomer(customer1);
        sampleReservation.setBookingStatus("1");
        sampleReservation = reservationRepository.save(sampleReservation);

        BookingDetail detail = new BookingDetail(sampleReservation, room101, startDate, endDate, actualPrice);
        bookingDetailRepository.save(detail);
    }
}
