package com.example.hotel.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rooms", indexes = {@Index(name="idx_room_hotel", columnList="hotel_id")})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Room {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name="hotel_id", nullable = false)
  private Long hotelId;

  @Column(nullable = false)
  private String number;

  @Column(nullable = false)
  private boolean available = true;

  @Column(name="times_booked", nullable = false)
  private long timesBooked = 0;
}
