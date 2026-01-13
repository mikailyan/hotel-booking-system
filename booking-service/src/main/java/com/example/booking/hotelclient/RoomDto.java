package com.example.booking.hotelclient;

public record RoomDto(Long id, Long hotelId, String number, boolean available, long timesBooked) {}
