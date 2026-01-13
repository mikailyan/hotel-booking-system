package com.example.booking.service;

import com.example.booking.domain.User;
import com.example.booking.repo.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserAdminService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public User create(String username, String rawPassword, User.Role role) {
    if (userRepository.findByUsername(username).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "username already exists");
    }
    return userRepository.save(User.builder()
        .username(username)
        .password(passwordEncoder.encode(rawPassword))
        .role(role)
        .build());
  }

  @Transactional
  public User update(String username, String newUsername, String newPasswordOrNull) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
    user.setUsername(newUsername);
    if (newPasswordOrNull != null && !newPasswordOrNull.isBlank()) {
      user.setPassword(passwordEncoder.encode(newPasswordOrNull));
    }
    return userRepository.save(user);
  }

  @Transactional
  public void delete(String username) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
    userRepository.delete(user);
  }
}
