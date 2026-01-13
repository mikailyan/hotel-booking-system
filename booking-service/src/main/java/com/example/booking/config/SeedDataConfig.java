package com.example.booking.config;

import com.example.booking.domain.User;
import com.example.booking.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class SeedDataConfig {

  private final PasswordEncoder passwordEncoder;

  @Bean
  CommandLineRunner seedUsers(UserRepository repo) {
    return args -> {
      repo.findByUsername("admin").orElseGet(() -> repo.save(User.builder()
          .username("admin")
          .password(passwordEncoder.encode("admin"))
          .role(User.Role.ADMIN)
          .build()));
      repo.findByUsername("user").orElseGet(() -> repo.save(User.builder()
          .username("user")
          .password(passwordEncoder.encode("user"))
          .role(User.Role.USER)
          .build()));
    };
  }
}
