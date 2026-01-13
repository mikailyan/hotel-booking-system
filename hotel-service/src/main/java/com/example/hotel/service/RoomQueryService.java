package com.example.hotel.service;

import com.example.hotel.domain.Room;
import com.example.hotel.repo.RoomRepository;
import com.example.hotel.repo.RoomHoldRepository;
import com.example.hotel.domain.RoomHold;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomQueryService {
  private final RoomRepository roomRepository;
  private final RoomHoldRepository holdRepository;

  public List<Room> listAvailableRooms(LocalDate start, LocalDate end) {
    var rooms = roomRepository.findByAvailableTrue();
    if (start == null || end == null) return rooms;
    return rooms.stream()
        .filter(r -> holdRepository.countOverlaps(r.getId(), RoomHold.Status.ACTIVE, start, end) == 0)
        .toList();
  }

public List<Room> recommendAvailableRooms(LocalDate start, LocalDate end) {
    return listAvailableRooms(start, end).stream()
        .sorted(Comparator.comparingLong(Room::getTimesBooked).thenComparing(Room::getId))
        .toList();
  }
}
