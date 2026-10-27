package com.fu.hotel.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingResponse {

    private Integer bookingReservationID;
    private LocalDate bookingDate;
    private BigDecimal totalPrice;
    private Integer customerID;
    private String customerFullName;
    private String customerEmail;
    private String customerPhone;
    private String bookingStatus;
    private List<BookingDetailResponse> bookingDetails = new ArrayList<>();

    public BookingResponse() {
    }

    public BookingResponse(Integer bookingReservationID, LocalDate bookingDate, BigDecimal totalPrice,
                           Integer customerID, String customerFullName, String customerEmail,
                           String customerPhone, String bookingStatus, List<BookingDetailResponse> bookingDetails) {
        this.bookingReservationID = bookingReservationID;
        this.bookingDate = bookingDate;
        this.totalPrice = totalPrice;
        this.customerID = customerID;
        this.customerFullName = customerFullName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.bookingStatus = bookingStatus;
        this.bookingDetails = bookingDetails != null ? bookingDetails : new ArrayList<>();
    }

    public Integer getBookingReservationID() {
        return bookingReservationID;
    }

    public void setBookingReservationID(Integer bookingReservationID) {
        this.bookingReservationID = bookingReservationID;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Integer getCustomerID() {
        return customerID;
    }

    public void setCustomerID(Integer customerID) {
        this.customerID = customerID;
    }

    public String getCustomerFullName() {
        return customerFullName;
    }

    public void setCustomerFullName(String customerFullName) {
        this.customerFullName = customerFullName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public List<BookingDetailResponse> getBookingDetails() {
        return bookingDetails;
    }

    public void setBookingDetails(List<BookingDetailResponse> bookingDetails) {
        this.bookingDetails = bookingDetails;
    }
}
