package com.example.booking.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(name="uk_user_username", columnNames = {"username"})})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class User {

  public enum Role { USER, ADMIN }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 64)
  private String username;

  @Column(nullable = false, length = 200)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private Role role;
}
