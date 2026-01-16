package com.example.minidoodle.application;

import com.example.minidoodle.infrastructure.persistence.entity.UserEntity;
import com.example.minidoodle.infrastructure.persistence.repo.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

import static com.example.minidoodle.application.Exceptions.*;

@Service
public class UserService {

  private final UserRepository users;

  public UserService(UserRepository users) {
    this.users = users;
  }

  public UserEntity create(String email, String name) {
    var now = Instant.now();
    var entity = new UserEntity(UUID.randomUUID(), email, name, now);
    try {
      return users.save(entity);
    } catch (DataIntegrityViolationException e) {
      throw new Conflict("email already exists");
    }
  }

  public UserEntity get(UUID id) {
    return users.findById(id).orElseThrow(() -> new NotFound("user not found"));
  }
}
