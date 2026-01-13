package com.example.booking.api;

import com.example.booking.api.dto.*;
import com.example.booking.api.mapper.UserMapper;
import com.example.booking.service.UserAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserAdminController {

  private final UserAdminService adminService;
  private final UserMapper userMapper;

  @PostMapping("/api/user")
  @PreAuthorize("hasRole('ADMIN')")
  public UserDto create(@Valid @RequestBody CreateUserRequest req) {
    return userMapper.toDto(adminService.create(req.username(), req.password(), req.role()));
  }

  @PatchMapping("/api/user")
  @PreAuthorize("hasRole('ADMIN')")
  public UserDto update(@Valid @RequestBody UpdateUserRequest req) {
    return userMapper.toDto(adminService.update(req.username(), req.username(), req.password()));
  }

  @DeleteMapping("/api/user")
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(@RequestParam("username") String username) {
    adminService.delete(username);
  }
}
