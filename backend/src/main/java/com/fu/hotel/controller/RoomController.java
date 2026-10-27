package com.fu.hotel.controller;

import com.fu.hotel.dto.request.RoomRequest;
import com.fu.hotel.dto.response.ApiResponse;
import com.fu.hotel.dto.response.RoomResponse;
import com.fu.hotel.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms(
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) Integer roomTypeId,
            @RequestParam(required = false) String roomStatus,
            @RequestParam(required = false) Integer minCapacity) {
        return ResponseEntity.ok(roomService.getAllRooms(roomNumber, roomTypeId, roomStatus, minCapacity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Integer id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody RoomRequest request) {
        return new ResponseEntity<>(roomService.createRoom(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Integer id,
                                                   @Valid @RequestBody RoomRequest request) {
        return ResponseEntity.ok(roomService.updateRoom(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse> deleteRoom(@PathVariable Integer id) {
        return ResponseEntity.ok(roomService.deleteRoom(id));
    }
}
