package com.example.booking.api.mapper;

import com.example.booking.api.dto.BookingDto;
import com.example.booking.domain.Booking;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookingMapper {
  BookingDto toDto(Booking booking);
}
