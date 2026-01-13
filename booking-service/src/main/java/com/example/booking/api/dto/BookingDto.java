package com.example.booking.api.dto;

import com.example.booking.domain.Booking;

import java.time.Instant;
import java.time.LocalDate;

public record BookingDto(
    Long id,
    Long userId,
    Long roomId,
    LocalDate startDate,
    LocalDate endDate,
    Booking.Status status,
    Instant createdAt,
    String requestId
) {}
