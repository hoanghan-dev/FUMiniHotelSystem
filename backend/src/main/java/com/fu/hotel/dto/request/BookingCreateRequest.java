package com.fu.hotel.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class BookingCreateRequest {

    private Integer customerID; // Required for staff, auto-filled from JWT for customer

    @NotEmpty(message = "At least one room must be included in the booking")
    @Valid
    private List<BookingDetailItemRequest> details;

    public BookingCreateRequest() {
    }

    public BookingCreateRequest(Integer customerID, List<BookingDetailItemRequest> details) {
        this.customerID = customerID;
        this.details = details;
    }

    public Integer getCustomerID() {
        return customerID;
    }

    public void setCustomerID(Integer customerID) {
        this.customerID = customerID;
    }

    public List<BookingDetailItemRequest> getDetails() {
        return details;
    }

    public void setDetails(List<BookingDetailItemRequest> details) {
        this.details = details;
    }
}
