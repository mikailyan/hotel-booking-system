package com.example.hotel.api;

import com.example.hotel.api.dto.ConfirmAvailabilityRequest;
import com.example.hotel.api.dto.ReleaseRequest;
import com.example.hotel.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/rooms")
public class InternalRoomController {

  private final AvailabilityService availabilityService;

  @PostMapping("/{id}/confirm-availability")
  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  public ResponseEntity<Void> confirm(@PathVariable("id") Long roomId, @Valid @RequestBody ConfirmAvailabilityRequest req) {
    availabilityService.confirmAvailability(roomId, req.bookingId(), req.requestId(), req.startDate(), req.endDate());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{id}/release")
  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  public ResponseEntity<Void> release(@PathVariable("id") Long roomId, @Valid @RequestBody ReleaseRequest req) {
    availabilityService.release(roomId, req.bookingId(), req.requestId());
    return ResponseEntity.ok().build();
  }
}
