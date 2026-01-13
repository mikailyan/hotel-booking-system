package com.example.booking.api.dto;

import com.example.booking.domain.User;

public record UserDto(Long id, String username, User.Role role) {}
