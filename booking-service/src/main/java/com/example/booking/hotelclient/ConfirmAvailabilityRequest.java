package com.example.booking.hotelclient;

import java.time.LocalDate;

public record ConfirmAvailabilityRequest(String requestId, Long bookingId, LocalDate startDate, LocalDate endDate) {}
