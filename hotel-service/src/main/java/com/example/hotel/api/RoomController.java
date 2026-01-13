package com.example.hotel.api;

import com.example.hotel.api.dto.*;
import com.example.hotel.api.mapper.RoomMapper;
import com.example.hotel.service.HotelCrudService;
import com.example.hotel.service.RoomQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class RoomController {

  private final RoomQueryService query;
  private final HotelCrudService crud;
  private final RoomMapper mapper;

  @GetMapping
  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  public List<RoomDto> list(@RequestParam(value="startDate", required=false) LocalDate startDate,
                           @RequestParam(value="endDate", required=false) LocalDate endDate) {
    return query.listAvailableRooms(startDate, endDate).stream().map(mapper::toDto).toList();
  }

  @GetMapping("/recommend")
  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  public List<RoomDto> recommend(@RequestParam(value="startDate", required=false) LocalDate startDate,
                                @RequestParam(value="endDate", required=false) LocalDate endDate) {
    return query.recommendAvailableRooms(startDate, endDate).stream().map(mapper::toDto).toList();
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public RoomDto create(@Valid @RequestBody CreateRoomRequest req) {
    return mapper.toDto(crud.createRoom(req.hotelId(), req.number(), req.available()));
  }
}
