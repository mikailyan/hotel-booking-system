package com.example.booking;

import com.example.booking.domain.Booking;
import com.example.booking.repo.BookingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookingIdempotencyTest {

  @Autowired
  BookingRepository bookingRepository;

  @Test
  void requestIdCanBeQueried() {
    bookingRepository.save(Booking.builder()
        .userId(1L).roomId(1L)
        .startDate(LocalDate.of(2026,1,20))
        .endDate(LocalDate.of(2026,1,22))
        .status(Booking.Status.PENDING)
        .createdAt(Instant.now())
        .requestId("req-1")
        .build());

    assertThat(bookingRepository.findByRequestId("req-1")).isPresent();
  }
}
