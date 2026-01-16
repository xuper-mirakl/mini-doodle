package com.example.minidoodle.infrastructure.persistence.entity;

import com.example.minidoodle.domain.SlotStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "time_slots")
public class TimeSlotEntity {

  @Id
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(name = "start_ts", nullable = false)
  private Instant startTs;

  @Column(name = "end_ts", nullable = false)
  private Instant endTs;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(nullable = false, columnDefinition = "slot_status")
  private SlotStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "meeting_id")
  private MeetingEntity meeting;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Version
  private long version;

  public TimeSlotEntity() {}

  public TimeSlotEntity(UUID id, UserEntity user, Instant startTs, Instant endTs,
                        SlotStatus status, MeetingEntity meeting,
                        Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.user = user;
    this.startTs = startTs;
    this.endTs = endTs;
    this.status = status;
    this.meeting = meeting;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  @PreUpdate
  void touch() {
    this.updatedAt = Instant.now();
  }

  public UUID getId() { return id; }
  public UserEntity getUser() { return user; }
  public Instant getStartTs() { return startTs; }
  public Instant getEndTs() { return endTs; }
  public SlotStatus getStatus() { return status; }
  public MeetingEntity getMeeting() { return meeting; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public long getVersion() { return version; }

  public void setStartTs(Instant startTs) { this.startTs = startTs; }
  public void setEndTs(Instant endTs) { this.endTs = endTs; }
  public void setStatus(SlotStatus status) { this.status = status; }
  public void setMeeting(MeetingEntity meeting) { this.meeting = meeting; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
