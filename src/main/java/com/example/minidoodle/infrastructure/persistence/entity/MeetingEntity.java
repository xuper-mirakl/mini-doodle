package com.example.minidoodle.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "meetings")
public class MeetingEntity {

  @Id
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "organizer_id", nullable = false)
  private UserEntity organizer;

  @Column(name = "start_ts", nullable = false)
  private Instant startTs;

  @Column(name = "end_ts", nullable = false)
  private Instant endTs;

  @Column(nullable = false)
  private String title;

  @Column
  private String description;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @ManyToMany
  @JoinTable(
      name = "meeting_participants",
      joinColumns = @JoinColumn(name = "meeting_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id")
  )
  private Set<UserEntity> participants = new LinkedHashSet<>();

  public MeetingEntity() {}

  public MeetingEntity(UUID id, UserEntity organizer, Instant startTs, Instant endTs,
                       String title, String description, Instant createdAt) {
    this.id = id;
    this.organizer = organizer;
    this.startTs = startTs;
    this.endTs = endTs;
    this.title = title;
    this.description = description;
    this.createdAt = createdAt;
  }

  public UUID getId() { return id; }
  public UserEntity getOrganizer() { return organizer; }
  public Instant getStartTs() { return startTs; }
  public Instant getEndTs() { return endTs; }
  public String getTitle() { return title; }
  public String getDescription() { return description; }
  public Instant getCreatedAt() { return createdAt; }
  public Set<UserEntity> getParticipants() { return participants; }

  public void setParticipants(Set<UserEntity> participants) { this.participants = participants; }
}
