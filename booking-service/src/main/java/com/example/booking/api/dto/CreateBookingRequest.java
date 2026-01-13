package com.example.booking.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateBookingRequest(
    @NotBlank String requestId,
    @NotNull Boolean autoSelect,
    Long roomId,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate
) {}
