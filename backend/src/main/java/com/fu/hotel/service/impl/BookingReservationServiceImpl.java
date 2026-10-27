package com.fu.hotel.service.impl;

import com.fu.hotel.dto.request.BookingCreateRequest;
import com.fu.hotel.dto.request.BookingDetailItemRequest;
import com.fu.hotel.dto.request.BookingUpdateRequest;
import com.fu.hotel.dto.response.ApiResponse;
import com.fu.hotel.dto.response.BookingDetailResponse;
import com.fu.hotel.dto.response.BookingResponse;
import com.fu.hotel.entity.BookingDetail;
import com.fu.hotel.entity.BookingReservation;
import com.fu.hotel.entity.Customer;
import com.fu.hotel.entity.RoomInformation;
import com.fu.hotel.exception.BadRequestException;
import com.fu.hotel.exception.ConflictException;
import com.fu.hotel.exception.ForbiddenException;
import com.fu.hotel.exception.ResourceNotFoundException;
import com.fu.hotel.repository.BookingDetailRepository;
import com.fu.hotel.repository.BookingReservationRepository;
import com.fu.hotel.repository.CustomerRepository;
import com.fu.hotel.repository.RoomInformationRepository;
import com.fu.hotel.service.BookingReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingReservationServiceImpl implements BookingReservationService {

    private final BookingReservationRepository reservationRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final CustomerRepository customerRepository;
    private final RoomInformationRepository roomRepository;

    public BookingReservationServiceImpl(BookingReservationRepository reservationRepository,
                                         BookingDetailRepository bookingDetailRepository,
                                         CustomerRepository customerRepository,
                                         RoomInformationRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.bookingDetailRepository = bookingDetailRepository;
        this.customerRepository = customerRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllReservations(Integer customerId, String status, LocalDate fromDate, LocalDate toDate) {
        List<BookingReservation> reservations;
        if (customerId == null && status == null && fromDate == null && toDate == null) {
            reservations = reservationRepository.findAll();
        } else {
            reservations = reservationRepository.searchReservations(
                    customerId,
                    status != null && !status.isBlank() ? status.trim() : null,
                    fromDate,
                    toDate
            );
        }
        return reservations.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getReservationById(Integer id, String currentUserEmail, boolean isStaff) {
        BookingReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking reservation not found with ID: " + id));

        if (!isStaff && (currentUserEmail == null || !currentUserEmail.equalsIgnoreCase(reservation.getCustomer().getEmailAddress()))) {
            throw new ForbiddenException("You are not authorized to view this booking reservation");
        }

        return mapToResponse(reservation);
    }

    @Override
    @Transactional
    public BookingResponse createOnlineBooking(String customerEmail, BookingCreateRequest request) {
        Customer customer = customerRepository.findByEmailAddress(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for email: " + customerEmail));

        boolean isActive = customer.getCustomerStatus() != null &&
                (customer.getCustomerStatus().equals("1") || customer.getCustomerStatus().equalsIgnoreCase("Active"));
        if (!isActive) {
            throw new ForbiddenException("Customer account is inactive. Online booking is prohibited");
        }

        return processBooking(customer, request.getDetails(), null);
    }

    @Override
    @Transactional
    public BookingResponse createStaffBooking(BookingCreateRequest request) {
        if (request.getCustomerID() == null) {
            throw new BadRequestException("Customer ID is required for staff booking creation");
        }

        Customer customer = customerRepository.findById(request.getCustomerID())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + request.getCustomerID()));

        return processBooking(customer, request.getDetails(), null);
    }

    @Override
    @Transactional
    public BookingResponse updateBooking(Integer id, BookingUpdateRequest request) {
        BookingReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking reservation not found with ID: " + id));

        if (request.getBookingStatus() != null && !request.getBookingStatus().isBlank()) {
            reservation.setBookingStatus(request.getBookingStatus().trim());
        }

        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            // Re-validate and compute details
            validateAndBuildDetails(reservation, request.getDetails(), id);
        }

        reservation = reservationRepository.save(reservation);
        return mapToResponse(reservation);
    }

    @Override
    @Transactional
    public ApiResponse deleteBooking(Integer id) {
        BookingReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking reservation not found with ID: " + id));

        reservationRepository.delete(reservation);
        return new ApiResponse(true, "Booking reservation deleted successfully", "DELETED");
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getCustomerBookingHistory(String customerEmail) {
        Customer customer = customerRepository.findByEmailAddress(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for email: " + customerEmail));

        List<BookingReservation> history = reservationRepository
                .findByCustomer_CustomerIDOrderByBookingDateDesc(customer.getCustomerID());

        return history.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private BookingResponse processBooking(Customer customer, List<BookingDetailItemRequest> detailRequests, Integer excludeReservationId) {
        if (detailRequests == null || detailRequests.isEmpty()) {
            throw new BadRequestException("At least one room must be included in the booking");
        }

        BookingReservation reservation = new BookingReservation();
        reservation.setBookingDate(LocalDate.now());
        reservation.setCustomer(customer);
        reservation.setBookingStatus("1"); // Confirmed
        reservation.setTotalPrice(BigDecimal.ZERO);

        reservation = reservationRepository.save(reservation);

        validateAndBuildDetails(reservation, detailRequests, excludeReservationId);

        reservation = reservationRepository.save(reservation);
        return mapToResponse(reservation);
    }

    private void validateAndBuildDetails(BookingReservation reservation, List<BookingDetailItemRequest> items, Integer excludeReservationId) {
        LocalDate today = LocalDate.now();
        BigDecimal grandTotal = BigDecimal.ZERO;
        List<BookingDetail> details = new ArrayList<>();

        // Check intra-request overlaps
        for (int i = 0; i < items.size(); i++) {
            BookingDetailItemRequest a = items.get(i);
            for (int j = i + 1; j < items.size(); j++) {
                BookingDetailItemRequest b = items.get(j);
                if (a.getRoomID().equals(b.getRoomID())) {
                    if (a.getStartDate().isBefore(b.getEndDate()) && a.getEndDate().isAfter(b.getStartDate())) {
                        throw new BadRequestException("Duplicate overlapping booking request for room ID: " + a.getRoomID());
                    }
                }
            }
        }

        for (BookingDetailItemRequest item : items) {
            LocalDate start = item.getStartDate();
            LocalDate end = item.getEndDate();

            if (start == null || end == null) {
                throw new BadRequestException("Start date and end date are required");
            }

            if (!start.isBefore(end)) {
                throw new BadRequestException("Start date (" + start + ") must be before end date (" + end + ")");
            }

            if (start.isBefore(today)) {
                throw new BadRequestException("Start date (" + start + ") cannot be in the past");
            }

            RoomInformation room = roomRepository.findById(item.getRoomID())
                    .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + item.getRoomID()));

            boolean isRoomActive = room.getRoomStatus() != null &&
                    (room.getRoomStatus().equals("1") || room.getRoomStatus().equalsIgnoreCase("Active"));
            if (!isRoomActive) {
                throw new BadRequestException("Room " + room.getRoomNumber() + " is currently inactive and cannot be booked");
            }

            // Database overlap check
            List<BookingDetail> overlaps = bookingDetailRepository.findOverlappingBookingsExcludingReservation(
                    room.getRoomID(), start, end, excludeReservationId);
            if (!overlaps.isEmpty()) {
                throw new ConflictException("Room " + room.getRoomNumber() + " is already booked between " + start + " and " + end);
            }

            long days = ChronoUnit.DAYS.between(start, end);
            if (days <= 0) {
                throw new BadRequestException("Booking duration must be at least 1 day");
            }

            BigDecimal actualPrice = room.getRoomPricePerDay().multiply(BigDecimal.valueOf(days));
            grandTotal = grandTotal.add(actualPrice);

            BookingDetail detail = new BookingDetail(reservation, room, start, end, actualPrice);
            details.add(detail);
        }

        reservation.getBookingDetails().clear();
        for (BookingDetail detail : details) {
            reservation.addBookingDetail(detail);
        }
        reservation.setTotalPrice(grandTotal);
    }

    private BookingResponse mapToResponse(BookingReservation entity) {
        List<BookingDetailResponse> detailResponses = new ArrayList<>();
        if (entity.getBookingDetails() != null) {
            for (BookingDetail d : entity.getBookingDetails()) {
                RoomInformation r = d.getRoom();
                long days = ChronoUnit.DAYS.between(d.getStartDate(), d.getEndDate());
                detailResponses.add(new BookingDetailResponse(
                        r != null ? r.getRoomID() : null,
                        r != null ? r.getRoomNumber() : null,
                        r != null && r.getRoomType() != null ? r.getRoomType().getRoomTypeName() : null,
                        r != null ? r.getRoomPricePerDay() : null,
                        d.getStartDate(),
                        d.getEndDate(),
                        days,
                        d.getActualPrice()
                ));
            }
        }

        Customer c = entity.getCustomer();
        return new BookingResponse(
                entity.getBookingReservationID(),
                entity.getBookingDate(),
                entity.getTotalPrice(),
                c != null ? c.getCustomerID() : null,
                c != null ? c.getCustomerFullName() : null,
                c != null ? c.getEmailAddress() : null,
                c != null ? c.getTelephone() : null,
                entity.getBookingStatus(),
                detailResponses
        );
    }
}
