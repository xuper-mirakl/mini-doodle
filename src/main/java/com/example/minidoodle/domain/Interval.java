package com.example.minidoodle.domain;

import java.time.Instant;

public record Interval(Instant start, Instant end) {
  public Interval {
    if (start == null || end == null) throw new IllegalArgumentException("start/end required");
    if (!end.isAfter(start)) throw new IllegalArgumentException("end must be after start");
  }

  public boolean overlaps(Interval other) {
    return this.start.isBefore(other.end) && this.end.isAfter(other.start);
  }
}
