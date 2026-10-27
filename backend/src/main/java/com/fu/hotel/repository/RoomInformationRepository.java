package com.fu.hotel.repository;

import com.fu.hotel.entity.RoomInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomInformationRepository extends JpaRepository<RoomInformation, Integer> {

    Optional<RoomInformation> findByRoomNumber(String roomNumber);

    boolean existsByRoomNumber(String roomNumber);

    boolean existsByRoomNumberAndRoomIDNot(String roomNumber, Integer roomID);

    boolean existsByRoomType_RoomTypeID(Integer roomTypeID);

    List<RoomInformation> findByRoomStatus(String roomStatus);

    @Query("SELECT r FROM RoomInformation r WHERE " +
           "(:roomNumber IS NULL OR LOWER(r.roomNumber) LIKE LOWER(CONCAT('%', :roomNumber, '%'))) AND " +
           "(:roomTypeId IS NULL OR r.roomType.roomTypeID = :roomTypeId) AND " +
           "(:roomStatus IS NULL OR LOWER(r.roomStatus) = LOWER(:roomStatus)) AND " +
           "(:minCapacity IS NULL OR r.roomMaxCapacity >= :minCapacity)")
    List<RoomInformation> searchRooms(@Param("roomNumber") String roomNumber,
                                      @Param("roomTypeId") Integer roomTypeId,
                                      @Param("roomStatus") String roomStatus,
                                      @Param("minCapacity") Integer minCapacity);
}
