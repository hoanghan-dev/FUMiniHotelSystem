package com.fu.hotel.dto.request;

import jakarta.validation.Valid;
import java.util.List;

public class BookingUpdateRequest {

    private String bookingStatus; // "1" for Confirmed, "0" for Cancelled

    @Valid
    private List<BookingDetailItemRequest> details;

    public BookingUpdateRequest() {
    }

    public BookingUpdateRequest(String bookingStatus, List<BookingDetailItemRequest> details) {
        this.bookingStatus = bookingStatus;
        this.details = details;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public List<BookingDetailItemRequest> getDetails() {
        return details;
    }

    public void setDetails(List<BookingDetailItemRequest> details) {
        this.details = details;
    }
}
