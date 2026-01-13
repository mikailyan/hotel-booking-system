package com.example.booking.service;

import com.example.booking.domain.Booking;
import com.example.booking.domain.Booking.Status;
import com.example.booking.hotelclient.HotelClient;
import com.example.booking.hotelclient.ReleaseRequest;
import com.example.booking.repo.BookingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingQueryService {
  private static final Logger log = LoggerFactory.getLogger(BookingQueryService.class);

  private final BookingRepository bookingRepository;
  private final HotelClient hotelClient;

  public List<Booking> history(Long userId) {
    return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
  }

  public Booking get(Long id) {
    return bookingRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "booking not found"));
  }

  @Transactional
  public Booking cancel(Long bookingId, Long userId, String requestId, String bearerToken) {
    Booking b = bookingRepository.findById(bookingId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "booking not found"));
    if (!b.getUserId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not your booking");

    if (b.getStatus() == Status.CANCELLED) return b;

    b.setStatus(Status.CANCELLED);
    b = bookingRepository.save(b);
    log.info("BOOKING_CANCELLED_BY_USER bookingId={} userId={} requestId={}", bookingId, userId, requestId);

    try {
      hotelClient.release(b.getRoomId(), new ReleaseRequest(requestId, bookingId), bearerToken);
    } catch (Exception ex) {
      log.warn("HOTEL_RELEASE_FAILED bookingId={} requestId={} err={}", bookingId, requestId, ex.getClass().getSimpleName());
    }
    return b;
  }
}
