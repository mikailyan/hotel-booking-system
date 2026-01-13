package com.example.hotel.repo;

import com.example.hotel.domain.RoomHold;
import com.example.hotel.domain.RoomHold.Status;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;

public interface RoomHoldRepository extends JpaRepository<RoomHold, Long> {

  Optional<RoomHold> findByRequestId(String requestId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select count(h) from RoomHold h " +
         "where h.roomId = :roomId and h.status = :status " +
         "and :startDate < h.endDate and :endDate > h.startDate")
  long countOverlapsForUpdate(@Param("roomId") Long roomId,
                              @Param("status") Status status,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select h from RoomHold h " +
         "where h.roomId = :roomId and h.bookingId = :bookingId and h.status = :status")
  Optional<RoomHold> findByRoomAndBookingForUpdate(@Param("roomId") Long roomId,
                                                   @Param("bookingId") Long bookingId,
                                                   @Param("status") Status status);

@Query("select count(h) from RoomHold h " +
       "where h.roomId = :roomId and h.status = :status " +
       "and :startDate < h.endDate and :endDate > h.startDate")
long countOverlaps(@Param("roomId") Long roomId,
                   @Param("status") Status status,
                   @Param("startDate") LocalDate startDate,
                   @Param("endDate") LocalDate endDate);
}
