package com.fu.hotel.service.impl;

import com.fu.hotel.dto.request.RoomTypeRequest;
import com.fu.hotel.dto.response.ApiResponse;
import com.fu.hotel.dto.response.RoomTypeResponse;
import com.fu.hotel.entity.RoomType;
import com.fu.hotel.exception.ConflictException;
import com.fu.hotel.exception.ResourceNotFoundException;
import com.fu.hotel.repository.RoomInformationRepository;
import com.fu.hotel.repository.RoomTypeRepository;
import com.fu.hotel.service.RoomTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;
    private final RoomInformationRepository roomInformationRepository;

    public RoomTypeServiceImpl(RoomTypeRepository roomTypeRepository,
                               RoomInformationRepository roomInformationRepository) {
        this.roomTypeRepository = roomTypeRepository;
        this.roomInformationRepository = roomInformationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomTypeResponse> getAllRoomTypes() {
        return roomTypeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoomTypeResponse getRoomTypeById(Integer id) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found with ID: " + id));
        return mapToResponse(roomType);
    }

    @Override
    @Transactional
    public RoomTypeResponse createRoomType(RoomTypeRequest request) {
        if (roomTypeRepository.existsByRoomTypeName(request.getRoomTypeName().trim())) {
            throw new ConflictException("Room type with name '" + request.getRoomTypeName() + "' already exists");
        }

        RoomType roomType = new RoomType();
        roomType.setRoomTypeName(request.getRoomTypeName().trim());
        roomType.setTypeDescription(request.getTypeDescription());
        roomType.setTypeNote(request.getTypeNote());

        roomType = roomTypeRepository.save(roomType);
        return mapToResponse(roomType);
    }

    @Override
    @Transactional
    public RoomTypeResponse updateRoomType(Integer id, RoomTypeRequest request) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found with ID: " + id));

        String newName = request.getRoomTypeName().trim();
        if (!roomType.getRoomTypeName().equalsIgnoreCase(newName) &&
                roomTypeRepository.existsByRoomTypeName(newName)) {
            throw new ConflictException("Room type with name '" + newName + "' already exists");
        }

        roomType.setRoomTypeName(newName);
        roomType.setTypeDescription(request.getTypeDescription());
        roomType.setTypeNote(request.getTypeNote());

        roomType = roomTypeRepository.save(roomType);
        return mapToResponse(roomType);
    }

    @Override
    @Transactional
    public ApiResponse deleteRoomType(Integer id) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found with ID: " + id));

        if (roomInformationRepository.existsByRoomType_RoomTypeID(id)) {
            throw new ConflictException("Cannot delete room type because it is associated with existing rooms");
        }

        roomTypeRepository.delete(roomType);
        return new ApiResponse(true, "Room type deleted successfully", "DELETED");
    }

    private RoomTypeResponse mapToResponse(RoomType entity) {
        return new RoomTypeResponse(
                entity.getRoomTypeID(),
                entity.getRoomTypeName(),
                entity.getTypeDescription(),
                entity.getTypeNote()
        );
    }
}
