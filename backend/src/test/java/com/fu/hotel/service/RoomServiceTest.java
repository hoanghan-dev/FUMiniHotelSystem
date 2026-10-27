package com.fu.hotel.service;

import com.fu.hotel.dto.request.RoomRequest;
import com.fu.hotel.dto.response.ApiResponse;
import com.fu.hotel.dto.response.RoomResponse;
import com.fu.hotel.entity.RoomInformation;
import com.fu.hotel.entity.RoomType;
import com.fu.hotel.exception.ConflictException;
import com.fu.hotel.exception.ResourceNotFoundException;
import com.fu.hotel.repository.BookingDetailRepository;
import com.fu.hotel.repository.RoomInformationRepository;
import com.fu.hotel.repository.RoomTypeRepository;
import com.fu.hotel.service.impl.RoomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomInformationRepository roomRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private BookingDetailRepository bookingDetailRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    private RoomType standardType;
    private RoomInformation testRoom;

    @BeforeEach
    void setUp() {
        standardType = new RoomType(1, "Standard Room", "Standard description", "Note");
        testRoom = new RoomInformation(10, "101", "Standard room 101", 2, standardType, "1", new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Create room successfully when room number is unique")
    void testCreateRoomSuccess() {
        RoomRequest request = new RoomRequest("101", "Description", 2, 1, "1", new BigDecimal("50.00"));

        when(roomRepository.existsByRoomNumber("101")).thenReturn(false);
        when(roomTypeRepository.findById(1)).thenReturn(Optional.of(standardType));
        when(roomRepository.save(any(RoomInformation.class))).thenReturn(testRoom);

        RoomResponse response = roomService.createRoom(request);

        assertThat(response).isNotNull();
        assertThat(response.getRoomNumber()).isEqualTo("101");
        verify(roomRepository, times(1)).save(any(RoomInformation.class));
    }

    @Test
    @DisplayName("Create room throws ConflictException when room number already exists")
    void testCreateRoomDuplicateThrowsConflict() {
        RoomRequest request = new RoomRequest("101", "Description", 2, 1, "1", new BigDecimal("50.00"));

        when(roomRepository.existsByRoomNumber("101")).thenReturn(true);

        assertThatThrownBy(() -> roomService.createRoom(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(roomRepository, never()).save(any(RoomInformation.class));
    }

    @Test
    @DisplayName("Delete room with booking history deactivates room instead of physical delete")
    void testDeleteRoomWithBookingHistorySoftDeletes() {
        when(roomRepository.findById(10)).thenReturn(Optional.of(testRoom));
        when(bookingDetailRepository.existsByRoom_RoomID(10)).thenReturn(true);

        ApiResponse response = roomService.deleteRoom(10);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getActionTaken()).isEqualTo("DEACTIVATED");
        assertThat(testRoom.getRoomStatus()).isEqualTo("0");
        verify(roomRepository, times(1)).save(testRoom);
        verify(roomRepository, never()).delete(any(RoomInformation.class));
    }

    @Test
    @DisplayName("Delete room without booking history physically deletes the room")
    void testDeleteRoomWithoutBookingHistoryPhysicallyDeletes() {
        when(roomRepository.findById(10)).thenReturn(Optional.of(testRoom));
        when(bookingDetailRepository.existsByRoom_RoomID(10)).thenReturn(false);

        ApiResponse response = roomService.deleteRoom(10);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getActionTaken()).isEqualTo("DELETED");
        verify(roomRepository, times(1)).delete(testRoom);
        verify(roomRepository, never()).save(any(RoomInformation.class));
    }
}
