package com.fu.hotel.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class RoomRequest {

    @NotBlank(message = "Room number is required")
    @Size(max = 50, message = "Room number must be at most 50 characters")
    private String roomNumber;

    @Size(max = 220, message = "Room detail description must be at most 220 characters")
    private String roomDetailDescription;

    @NotNull(message = "Room max capacity is required")
    @Min(value = 1, message = "Room max capacity must be at least 1")
    private Integer roomMaxCapacity;

    @NotNull(message = "Room type ID is required")
    private Integer roomTypeID;

    private String roomStatus; // "1" for Active, "0" for Inactive

    @NotNull(message = "Room price per day is required")
    @DecimalMin(value = "0.01", message = "Room price per day must be greater than 0")
    private BigDecimal roomPricePerDay;

    public RoomRequest() {
    }

    public RoomRequest(String roomNumber, String roomDetailDescription, Integer roomMaxCapacity,
                       Integer roomTypeID, String roomStatus, BigDecimal roomPricePerDay) {
        this.roomNumber = roomNumber;
        this.roomDetailDescription = roomDetailDescription;
        this.roomMaxCapacity = roomMaxCapacity;
        this.roomTypeID = roomTypeID;
        this.roomStatus = roomStatus;
        this.roomPricePerDay = roomPricePerDay;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomDetailDescription() {
        return roomDetailDescription;
    }

    public void setRoomDetailDescription(String roomDetailDescription) {
        this.roomDetailDescription = roomDetailDescription;
    }

    public Integer getRoomMaxCapacity() {
        return roomMaxCapacity;
    }

    public void setRoomMaxCapacity(Integer roomMaxCapacity) {
        this.roomMaxCapacity = roomMaxCapacity;
    }

    public Integer getRoomTypeID() {
        return roomTypeID;
    }

    public void setRoomTypeID(Integer roomTypeID) {
        this.roomTypeID = roomTypeID;
    }

    public String getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(String roomStatus) {
        this.roomStatus = roomStatus;
    }

    public BigDecimal getRoomPricePerDay() {
        return roomPricePerDay;
    }

    public void setRoomPricePerDay(BigDecimal roomPricePerDay) {
        this.roomPricePerDay = roomPricePerDay;
    }
}
