package com.example.hotel.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReleaseRequest(@NotBlank String requestId, @NotNull Long bookingId) {}
