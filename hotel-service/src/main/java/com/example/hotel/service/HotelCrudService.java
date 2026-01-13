package com.example.hotel.service;

import com.example.hotel.domain.Hotel;
import com.example.hotel.domain.Room;
import com.example.hotel.repo.HotelRepository;
import com.example.hotel.repo.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelCrudService {
  private final HotelRepository hotelRepository;
  private final RoomRepository roomRepository;

  public List<Hotel> listHotels() {
    return hotelRepository.findAll();
  }

  @Transactional
  public Hotel createHotel(String name, String address) {
    return hotelRepository.save(Hotel.builder().name(name).address(address).build());
  }

  @Transactional
  public Room createRoom(Long hotelId, String number, Boolean available) {
    return roomRepository.save(Room.builder()
        .hotelId(hotelId)
        .number(number)
        .available(available == null ? true : available)
        .timesBooked(0)
        .build());
  }
}
