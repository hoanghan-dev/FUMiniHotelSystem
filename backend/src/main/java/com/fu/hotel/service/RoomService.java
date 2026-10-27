package com.fu.hotel.service;

import com.fu.hotel.dto.request.RoomRequest;
import com.fu.hotel.dto.response.ApiResponse;
import com.fu.hotel.dto.response.RoomResponse;

import java.util.List;

public interface RoomService {
    List<RoomResponse> getAllRooms(String roomNumber, Integer roomTypeId, String roomStatus, Integer minCapacity);
    RoomResponse getRoomById(Integer id);
    RoomResponse createRoom(RoomRequest request);
    RoomResponse updateRoom(Integer id, RoomRequest request);
    ApiResponse deleteRoom(Integer id);
}
