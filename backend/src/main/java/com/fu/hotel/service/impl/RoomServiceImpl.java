package com.fu.hotel.service.impl;

import com.fu.hotel.dto.request.RoomRequest;
import com.fu.hotel.dto.response.ApiResponse;
import com.fu.hotel.dto.response.RoomResponse;
import com.fu.hotel.dto.response.RoomTypeResponse;
import com.fu.hotel.entity.RoomInformation;
import com.fu.hotel.entity.RoomType;
import com.fu.hotel.exception.ConflictException;
import com.fu.hotel.exception.ResourceNotFoundException;
import com.fu.hotel.repository.BookingDetailRepository;
import com.fu.hotel.repository.RoomInformationRepository;
import com.fu.hotel.repository.RoomTypeRepository;
import com.fu.hotel.service.RoomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomInformationRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final BookingDetailRepository bookingDetailRepository;

    public RoomServiceImpl(RoomInformationRepository roomRepository,
                           RoomTypeRepository roomTypeRepository,
                           BookingDetailRepository bookingDetailRepository) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.bookingDetailRepository = bookingDetailRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAllRooms(String roomNumber, Integer roomTypeId, String roomStatus, Integer minCapacity) {
        List<RoomInformation> rooms;
        if (roomNumber == null && roomTypeId == null && roomStatus == null && minCapacity == null) {
            rooms = roomRepository.findAll();
        } else {
            rooms = roomRepository.searchRooms(
                    roomNumber != null && !roomNumber.isBlank() ? roomNumber.trim() : null,
                    roomTypeId,
                    roomStatus != null && !roomStatus.isBlank() ? roomStatus.trim() : null,
                    minCapacity
            );
        }
        return rooms.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomById(Integer id) {
        RoomInformation room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + id));
        return mapToResponse(room);
    }

    @Override
    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        String roomNumber = request.getRoomNumber().trim();
        if (roomRepository.existsByRoomNumber(roomNumber)) {
            throw new ConflictException("Room number '" + roomNumber + "' already exists");
        }

        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeID())
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found with ID: " + request.getRoomTypeID()));

        RoomInformation room = new RoomInformation();
        room.setRoomNumber(roomNumber);
        room.setRoomDetailDescription(request.getRoomDetailDescription());
        room.setRoomMaxCapacity(request.getRoomMaxCapacity());
        room.setRoomType(roomType);
        room.setRoomStatus(request.getRoomStatus() != null ? request.getRoomStatus().trim() : "1");
        room.setRoomPricePerDay(request.getRoomPricePerDay());

        room = roomRepository.save(room);
        return mapToResponse(room);
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(Integer id, RoomRequest request) {
        RoomInformation room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + id));

        String roomNumber = request.getRoomNumber().trim();
        if (roomRepository.existsByRoomNumberAndRoomIDNot(roomNumber, id)) {
            throw new ConflictException("Room number '" + roomNumber + "' already exists");
        }

        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeID())
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found with ID: " + request.getRoomTypeID()));

        room.setRoomNumber(roomNumber);
        room.setRoomDetailDescription(request.getRoomDetailDescription());
        room.setRoomMaxCapacity(request.getRoomMaxCapacity());
        room.setRoomType(roomType);
        if (request.getRoomStatus() != null && !request.getRoomStatus().isBlank()) {
            room.setRoomStatus(request.getRoomStatus().trim());
        }
        room.setRoomPricePerDay(request.getRoomPricePerDay());

        room = roomRepository.save(room);
        return mapToResponse(room);
    }

    @Override
    @Transactional
    public ApiResponse deleteRoom(Integer id) {
        RoomInformation room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + id));

        // Delete rule: if referenced by BookingDetail -> soft delete (change status to "0"), else physically delete
        boolean hasBookingHistory = bookingDetailRepository.existsByRoom_RoomID(id);
        if (hasBookingHistory) {
            room.setRoomStatus("0"); // Inactive
            roomRepository.save(room);
            return new ApiResponse(true, "Room has booking history and was deactivated instead of deleted.", "DEACTIVATED");
        } else {
            roomRepository.delete(room);
            return new ApiResponse(true, "Room deleted successfully.", "DELETED");
        }
    }

    private RoomResponse mapToResponse(RoomInformation entity) {
        RoomTypeResponse roomTypeDto = null;
        if (entity.getRoomType() != null) {
            roomTypeDto = new RoomTypeResponse(
                    entity.getRoomType().getRoomTypeID(),
                    entity.getRoomType().getRoomTypeName(),
                    entity.getRoomType().getTypeDescription(),
                    entity.getRoomType().getTypeNote()
            );
        }

        return new RoomResponse(
                entity.getRoomID(),
                entity.getRoomNumber(),
                entity.getRoomDetailDescription(),
                entity.getRoomMaxCapacity(),
                roomTypeDto,
                entity.getRoomStatus(),
                entity.getRoomPricePerDay()
        );
    }
}
