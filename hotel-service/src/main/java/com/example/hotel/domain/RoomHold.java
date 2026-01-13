package com.example.hotel.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "room_holds",
       uniqueConstraints = {@UniqueConstraint(name="uk_hold_request", columnNames = {"request_id"})},
       indexes = {@Index(name="idx_hold_room", columnList="room_id"), @Index(name="idx_hold_booking", columnList="booking_id")})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RoomHold {

  public enum Status { ACTIVE, RELEASED }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name="room_id", nullable = false)
  private Long roomId;

  @Column(name="booking_id", nullable = false)
  private Long bookingId;

  @Column(name="request_id", nullable = false, length = 64)
  private String requestId;

  @Column(name="start_date", nullable = false)
  private LocalDate startDate;

  @Column(name="end_date", nullable = false)
  private LocalDate endDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Status status;

  @Column(name="created_at", nullable = false)
  private Instant createdAt;
}
