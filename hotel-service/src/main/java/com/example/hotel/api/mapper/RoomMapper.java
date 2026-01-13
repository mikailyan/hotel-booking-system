package com.example.hotel.api.mapper;

import com.example.hotel.api.dto.RoomDto;
import com.example.hotel.domain.Room;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoomMapper {
  RoomDto toDto(Room room);
}
