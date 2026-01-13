package com.example.booking.api.mapper;

import com.example.booking.api.dto.UserDto;
import com.example.booking.domain.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
  UserDto toDto(User user);
}
