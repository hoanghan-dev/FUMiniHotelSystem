package com.fu.hotel.repository;

import com.fu.hotel.entity.BookingDetail;
import com.fu.hotel.entity.BookingDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingDetailRepository extends JpaRepository<BookingDetail, BookingDetailId> {

    boolean existsByRoom_RoomID(Integer roomID);

    List<BookingDetail> findByRoom_RoomID(Integer roomID);

    @Query("SELECT b FROM BookingDetail b WHERE b.room.roomID = :roomId " +
           "AND b.startDate < :endDate AND b.endDate > :startDate " +
           "AND b.bookingReservation.bookingStatus NOT IN ('0', 'Cancelled')")
    List<BookingDetail> findOverlappingBookings(@Param("roomId") Integer roomId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT b FROM BookingDetail b WHERE b.room.roomID = :roomId " +
           "AND b.startDate < :endDate AND b.endDate > :startDate " +
           "AND b.bookingReservation.bookingStatus NOT IN ('0', 'Cancelled') " +
           "AND (:excludeReservationId IS NULL OR b.bookingReservation.bookingReservationID <> :excludeReservationId)")
    List<BookingDetail> findOverlappingBookingsExcludingReservation(@Param("roomId") Integer roomId,
                                                                   @Param("startDate") LocalDate startDate,
                                                                   @Param("endDate") LocalDate endDate,
                                                                   @Param("excludeReservationId") Integer excludeReservationId);
}
