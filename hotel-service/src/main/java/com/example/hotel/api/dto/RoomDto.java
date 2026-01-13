package com.example.hotel.api.dto;

public record RoomDto(Long id, Long hotelId, String number, boolean available, long timesBooked) {}
