package com.example.hotel.service;

import com.example.hotel.domain.Room;
import com.example.hotel.domain.RoomHold;
import com.example.hotel.domain.RoomHold.Status;
import com.example.hotel.repo.RoomHoldRepository;
import com.example.hotel.repo.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AvailabilityService {
  private static final Logger log = LoggerFactory.getLogger(AvailabilityService.class);

  private final RoomRepository roomRepository;
  private final RoomHoldRepository holdRepository;

  @Transactional
  public void confirmAvailability(Long roomId, Long bookingId, String requestId, LocalDate start, LocalDate end) {
    var existing = holdRepository.findByRequestId(requestId);
    if (existing.isPresent()) {
      var h = existing.get();
      if (!h.getRoomId().equals(roomId) || !h.getBookingId().equals(bookingId)) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "requestId already used for another booking/room");
      }
      if (h.getStatus() == Status.ACTIVE) return;
      throw new ResponseStatusException(HttpStatus.CONFLICT, "hold already released");
    }

    if (!start.isBefore(end)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate must be before endDate");
    }

    Room room = roomRepository.findById(roomId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "room not found"));

    if (!room.isAvailable()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "room not operationally available");
    }

    long overlaps = holdRepository.countOverlapsForUpdate(roomId, Status.ACTIVE, start, end);
    if (overlaps > 0) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "room already reserved for dates");
    }

    room.setTimesBooked(room.getTimesBooked() + 1);
    roomRepository.save(room);

    holdRepository.save(RoomHold.builder()
        .roomId(roomId)
        .bookingId(bookingId)
        .requestId(requestId)
        .startDate(start)
        .endDate(end)
        .status(Status.ACTIVE)
        .createdAt(Instant.now())
        .build());

    log.info("HOLD_ACTIVE roomId={} bookingId={} requestId={} {}..{}", roomId, bookingId, requestId, start, end);
  }

  @Transactional
  public void release(Long roomId, Long bookingId, String requestId) {
    var byReq = holdRepository.findByRequestId(requestId);
    if (byReq.isPresent()) {
      var h = byReq.get();
      if (!h.getRoomId().equals(roomId) || !h.getBookingId().equals(bookingId)) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "requestId already used for another booking/room");
      }
      if (h.getStatus() == Status.RELEASED) return;
      h.setStatus(Status.RELEASED);
      holdRepository.save(h);
      log.info("HOLD_RELEASED(byRequest) roomId={} bookingId={} requestId={}", roomId, bookingId, requestId);
      return;
    }

    var active = holdRepository.findByRoomAndBookingForUpdate(roomId, bookingId, Status.ACTIVE);
    if (active.isEmpty()) return; // idempotent
    var h = active.get();
    h.setStatus(Status.RELEASED);
    holdRepository.save(h);
    log.info("HOLD_RELEASED roomId={} bookingId={} requestId={}", roomId, bookingId, requestId);
  }
}
