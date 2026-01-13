package com.example.booking.api;

import com.example.booking.api.dto.*;
import com.example.booking.api.mapper.BookingMapper;
import com.example.booking.domain.User;
import com.example.booking.hotelclient.HotelClient;
import com.example.booking.repo.UserRepository;
import com.example.booking.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookingController {

  private final BookingOrchestrationService orchestrationService;
  private final BookingQueryService bookingQueryService;
  private final BookingMapper bookingMapper;
  private final UserRepository userRepository;
  private final HotelClient hotelClient;

  @PostMapping("/api/booking")
  @PreAuthorize("hasRole('USER')")
  public BookingDto create(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                           Authentication authentication,
                           @Valid @RequestBody CreateBookingRequest req) {
    User user = userRepository.findByUsername(authentication.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unknown user"));

    Long roomId = req.roomId();
    if (Boolean.TRUE.equals(req.autoSelect())) {
      var rooms = hotelClient.getRecommendedRooms(authHeader, req.startDate(), req.endDate());
      if (rooms.isEmpty()) throw new ResponseStatusException(HttpStatus.CONFLICT, "no rooms available");
      roomId = rooms.get(0).id();
    } else {
      if (roomId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "roomId required when autoSelect=false");
    }

    var booking = orchestrationService.createAndConfirm(user.getId(), roomId, req.startDate(), req.endDate(), req.requestId(), authHeader);
    return bookingMapper.toDto(booking);
  }

  @GetMapping("/api/bookings")
  @PreAuthorize("hasRole('USER')")
  public List<BookingDto> history(Authentication authentication) {
    User user = userRepository.findByUsername(authentication.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unknown user"));
    return bookingQueryService.history(user.getId()).stream().map(bookingMapper::toDto).toList();
  }

  @GetMapping("/api/booking/{id}")
  @PreAuthorize("hasRole('USER')")
  public BookingDto get(@PathVariable(name = "id") Long id, Authentication authentication) {
    User user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unknown user"));
    var b = bookingQueryService.get(id);
    if (!b.getUserId().equals(user.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not your booking");
    return bookingMapper.toDto(b);
  }

  @DeleteMapping("/api/booking/{id}")
  @PreAuthorize("hasRole('USER')")
  public BookingDto cancel(@RequestHeader(name = HttpHeaders.AUTHORIZATION) String authHeader,
                           @PathVariable(name = "id") Long id,
                           @RequestParam(name = "requestId", required = false) String requestId,
                           Authentication authentication) {

    if (requestId == null || requestId.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "requestId is required");
    }

    User user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unknown user"));

    return bookingMapper.toDto(bookingQueryService.cancel(id, user.getId(), requestId, authHeader));
  }


}
