package com.fu.hotel.repository;

import com.fu.hotel.entity.BookingReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingReservationRepository extends JpaRepository<BookingReservation, Integer> {

    List<BookingReservation> findByCustomer_CustomerIDOrderByBookingDateDesc(Integer customerID);

    boolean existsByCustomer_CustomerID(Integer customerID);

    @Query("SELECT b FROM BookingReservation b WHERE " +
           "(:customerId IS NULL OR b.customer.customerID = :customerId) AND " +
           "(:status IS NULL OR LOWER(b.bookingStatus) = LOWER(:status)) AND " +
           "(:fromDate IS NULL OR b.bookingDate >= :fromDate) AND " +
           "(:toDate IS NULL OR b.bookingDate <= :toDate) " +
           "ORDER BY b.bookingDate DESC")
    List<BookingReservation> searchReservations(@Param("customerId") Integer customerId,
                                                @Param("status") String status,
                                                @Param("fromDate") LocalDate fromDate,
                                                @Param("toDate") LocalDate toDate);
}
