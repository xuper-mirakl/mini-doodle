package com.example.minidoodle.api.controller;

import com.example.minidoodle.api.dto.CreateUserRequest;
import com.example.minidoodle.api.dto.UserResponse;
import com.example.minidoodle.application.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UsersController {

  private final UserService users;

  public UsersController(UserService users) {
    this.users = users;
  }

  @PostMapping
  public UserResponse create(@Valid @RequestBody CreateUserRequest req) {
    var u = users.create(req.email(), req.name());
    return new UserResponse(u.getId(), u.getEmail(), u.getName(), u.getCreatedAt());
  }

  @GetMapping("/{id}")
  public UserResponse get(@PathVariable UUID id) {
    var u = users.get(id);
    return new UserResponse(u.getId(), u.getEmail(), u.getName(), u.getCreatedAt());
  }
}
