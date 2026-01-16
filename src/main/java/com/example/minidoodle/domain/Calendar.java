package com.example.minidoodle.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * "Calendar" exists ONLY in the domain. It is not exposed as an API resource
 * and does not appear in table names or endpoint paths.
 */
public final class Calendar {
  private final UUID ownerId;

  public Calendar(UUID ownerId) {
    this.ownerId = ownerId;
  }

  public UUID ownerId() {
    return ownerId;
  }

  /**
   * Effective free intervals = AVAILABLE - BUSY within [from, to).
   * The heavy lifting is done in the AvailabilityService.
   */
  public record Snapshot(UUID userId, Instant from, Instant to,
                         List<Interval> available, List<Interval> busy, List<Interval> free) {}
}
