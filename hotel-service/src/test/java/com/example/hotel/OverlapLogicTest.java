package com.example.hotel;

import com.example.hotel.domain.Room;
import com.example.hotel.domain.RoomHold;
import com.example.hotel.repo.RoomHoldRepository;
import com.example.hotel.repo.RoomRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OverlapLogicTest {

  @Autowired RoomRepository roomRepository;
  @Autowired RoomHoldRepository holdRepository;

  @Test
  void overlapQueryCountsOverlaps() {
    Room room = roomRepository.save(Room.builder().hotelId(1L).number("1").available(true).timesBooked(0).build());
    holdRepository.save(RoomHold.builder()
        .roomId(room.getId()).bookingId(1L).requestId("r1")
        .startDate(LocalDate.of(2026,1,10))
        .endDate(LocalDate.of(2026,1,12))
        .status(RoomHold.Status.ACTIVE)
        .createdAt(Instant.now())
        .build());

    long overlaps = holdRepository.countOverlapsForUpdate(room.getId(), RoomHold.Status.ACTIVE,
        LocalDate.of(2026,1,11), LocalDate.of(2026,1,13));
    assertThat(overlaps).isEqualTo(1);
  }
}
