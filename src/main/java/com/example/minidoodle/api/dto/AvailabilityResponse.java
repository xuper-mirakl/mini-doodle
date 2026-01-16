package com.example.minidoodle.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AvailabilityResponse(
    Instant from,
    Instant to,
    List<UserAvailability> users,
    List<IntervalResponse> commonFree
) {
  public record UserAvailability(UUID userId, List<IntervalResponse> free, List<IntervalResponse> busy) {}
}
