package com.fu.hotel.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "BookingDetail")
@IdClass(BookingDetailId.class)
public class BookingDetail {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BookingReservationID", nullable = false)
    private BookingReservation bookingReservation;

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "RoomID", nullable = false)
    private RoomInformation room;

    @Column(name = "StartDate", nullable = false)
    private LocalDate startDate;

    @Column(name = "EndDate", nullable = false)
    private LocalDate endDate;

    @Column(name = "ActualPrice", precision = 18, scale = 2, nullable = false)
    private BigDecimal actualPrice;

    public BookingDetail() {
    }

    public BookingDetail(BookingReservation bookingReservation, RoomInformation room,
                         LocalDate startDate, LocalDate endDate, BigDecimal actualPrice) {
        this.bookingReservation = bookingReservation;
        this.room = room;
        this.startDate = startDate;
        this.endDate = endDate;
        this.actualPrice = actualPrice;
    }

    public BookingReservation getBookingReservation() {
        return bookingReservation;
    }

    public void setBookingReservation(BookingReservation bookingReservation) {
        this.bookingReservation = bookingReservation;
    }

    public RoomInformation getRoom() {
        return room;
    }

    public void setRoom(RoomInformation room) {
        this.room = room;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getActualPrice() {
        return actualPrice;
    }

    public void setActualPrice(BigDecimal actualPrice) {
        this.actualPrice = actualPrice;
    }
}
