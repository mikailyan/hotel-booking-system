package com.example.booking.service;

import com.example.booking.domain.Booking;
import com.example.booking.domain.Booking.Status;
import com.example.booking.hotelclient.*;
import com.example.booking.repo.BookingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BookingOrchestrationService {
  private static final Logger log = LoggerFactory.getLogger(BookingOrchestrationService.class);

  private final BookingRepository bookingRepository;
  private final HotelClient hotelClient;

  @Transactional
  public Booking createPendingIdempotent(Long userId, Long roomId, LocalDate start, LocalDate end, String requestId) {
    var existing = bookingRepository.findByRequestId(requestId);
    if (existing.isPresent()) return existing.get();

    if (!start.isBefore(end)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate must be before endDate");
    }

    Booking booking = bookingRepository.save(Booking.builder()
        .userId(userId)
        .roomId(roomId)
        .startDate(start)
        .endDate(end)
        .status(Status.PENDING)
        .createdAt(Instant.now())
        .requestId(requestId)
        .build());

    log.info("BOOKING_PENDING bookingId={} requestId={} userId={} roomId={} {}..{}", booking.getId(), requestId, userId, roomId, start, end);
    return booking;
  }

  public Booking createAndConfirm(Long userId, Long roomId, LocalDate start, LocalDate end, String requestId, String bearerToken) {
    Booking booking = createPendingIdempotent(userId, roomId, start, end, requestId);

    if (booking.getStatus() == Status.CONFIRMED || booking.getStatus() == Status.CANCELLED) {
      return booking;
    }

    try {
      hotelClient.confirmAvailability(roomId, new ConfirmAvailabilityRequest(requestId, booking.getId(), start, end), bearerToken);
      return markConfirmed(booking.getId(), requestId);
    } catch (HttpStatusCodeException e) {
      if (e.getStatusCode().value() == 409) {
        log.info("HOTEL_CONFIRM_CONFLICT bookingId={} requestId={} roomId={}", booking.getId(), requestId, roomId);
        cancelWithCompensation(booking.getId(), roomId, requestId, bearerToken);
        throw new ResponseStatusException(HttpStatus.CONFLICT, "room is not available for dates");
      }
      log.warn("HOTEL_CONFIRM_ERROR bookingId={} requestId={} status={}", booking.getId(), requestId, e.getStatusCode());
      cancelWithCompensation(booking.getId(), roomId, requestId, bearerToken);
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "hotel service error");
    } catch (ResourceAccessException e) {
      log.warn("HOTEL_CONFIRM_TIMEOUT bookingId={} requestId={}", booking.getId(), requestId);
      cancelWithCompensation(booking.getId(), roomId, requestId, bearerToken);
      throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "hotel service timeout");
    }
  }

  @Transactional
  public Booking markConfirmed(Long bookingId, String requestId) {
    Booking b = bookingRepository.findById(bookingId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "booking not found"));
    if (b.getStatus() == Status.CONFIRMED) return b;
    if (b.getStatus() == Status.CANCELLED) throw new ResponseStatusException(HttpStatus.CONFLICT, "booking already cancelled");
    b.setStatus(Status.CONFIRMED);
    b = bookingRepository.save(b);
    log.info("BOOKING_CONFIRMED bookingId={} requestId={}", bookingId, requestId);
    return b;
  }

  public void cancelWithCompensation(Long bookingId, Long roomId, String requestId, String bearerToken) {
    markCancelled(bookingId, requestId);
    try {
      hotelClient.release(roomId, new ReleaseRequest(requestId, bookingId), bearerToken);
    } catch (Exception ex) {
      log.warn("HOTEL_RELEASE_FAILED bookingId={} requestId={} err={}", bookingId, requestId, ex.getClass().getSimpleName());
    }
  }

  @Transactional
  public Booking markCancelled(Long bookingId, String requestId) {
    Booking b = bookingRepository.findById(bookingId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "booking not found"));
    if (b.getStatus() == Status.CANCELLED) return b;
    if (b.getStatus() == Status.PENDING) {
      b.setStatus(Status.CANCELLED);
      b = bookingRepository.save(b);
      log.info("BOOKING_CANCELLED bookingId={} requestId={}", bookingId, requestId);
    }
    return b;
  }
}
