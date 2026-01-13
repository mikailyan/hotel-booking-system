package com.example.hotel.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateHotelRequest(@NotBlank String name, @NotBlank String address) {}
