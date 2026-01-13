package com.example.booking.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(@NotBlank String username, String password) {}
