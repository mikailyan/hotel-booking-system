package com.example.hotel.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRoomRequest(@NotNull Long hotelId, @NotBlank String number, Boolean available) {}
