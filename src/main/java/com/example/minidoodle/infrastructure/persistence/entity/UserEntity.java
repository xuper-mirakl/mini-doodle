package com.example.minidoodle.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

  @Id
  private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String name;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  public UserEntity() {}

  public UserEntity(UUID id, String email, String name, Instant createdAt) {
    this.id = id;
    this.email = email;
    this.name = name;
    this.createdAt = createdAt;
  }

  public UUID getId() { return id; }
  public String getEmail() { return email; }
  public String getName() { return name; }
  public Instant getCreatedAt() { return createdAt; }

  public void setEmail(String email) { this.email = email; }
  public void setName(String name) { this.name = name; }
}
