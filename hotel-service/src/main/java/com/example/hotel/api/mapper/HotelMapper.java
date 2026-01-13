package com.example.hotel.api.mapper;

import com.example.hotel.api.dto.HotelDto;
import com.example.hotel.domain.Hotel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HotelMapper {
  HotelDto toDto(Hotel hotel);
}
