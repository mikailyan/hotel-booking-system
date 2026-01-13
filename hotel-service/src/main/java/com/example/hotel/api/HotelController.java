package com.example.hotel.api;

import com.example.hotel.api.dto.*;
import com.example.hotel.api.mapper.HotelMapper;
import com.example.hotel.service.HotelCrudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hotels")
public class HotelController {

  private final HotelCrudService crud;
  private final HotelMapper mapper;

  @GetMapping
  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  public List<HotelDto> list() {
    return crud.listHotels().stream().map(mapper::toDto).toList();
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public HotelDto create(@Valid @RequestBody CreateHotelRequest req) {
    return mapper.toDto(crud.createHotel(req.name(), req.address()));
  }
}
