package com.example.booking.api.dto;

import com.example.booking.domain.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(@NotBlank String username, @NotBlank String password, @NotNull User.Role role) {}
