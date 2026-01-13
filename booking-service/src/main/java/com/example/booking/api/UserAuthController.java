package com.example.booking.api;

import com.example.booking.api.dto.*;
import com.example.booking.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserAuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public AuthResponse register(@Valid @RequestBody AuthRequest req) {
    return new AuthResponse(authService.register(req.username(), req.password()));
  }

  @PostMapping("/auth")
  public AuthResponse auth(@Valid @RequestBody AuthRequest req) {
    return new AuthResponse(authService.authenticate(req.username(), req.password()));
  }
}
