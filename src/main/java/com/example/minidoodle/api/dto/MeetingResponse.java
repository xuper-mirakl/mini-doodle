package com.example.minidoodle.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MeetingResponse(
    UUID id,
    UUID organizerId,
    Instant start,
    Instant end,
    String title,
    String description,
    List<UUID> participantIds
) {}
