package com.fu.hotel.service;

import com.fu.hotel.dto.request.RoomTypeRequest;
import com.fu.hotel.dto.response.ApiResponse;
import com.fu.hotel.dto.response.RoomTypeResponse;

import java.util.List;

public interface RoomTypeService {
    List<RoomTypeResponse> getAllRoomTypes();
    RoomTypeResponse getRoomTypeById(Integer id);
    RoomTypeResponse createRoomType(RoomTypeRequest request);
    RoomTypeResponse updateRoomType(Integer id, RoomTypeRequest request);
    ApiResponse deleteRoomType(Integer id);
}
