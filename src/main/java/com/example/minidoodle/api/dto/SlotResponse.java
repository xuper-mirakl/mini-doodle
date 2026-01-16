package com.example.minidoodle.api.dto;

import com.example.minidoodle.domain.SlotStatus;

import java.time.Instant;
import java.util.UUID;

public record SlotResponse(
    UUID id,
    UUID userId,
    Instant start,
    Instant end,
    SlotStatus status,
    UUID meetingId
) {}
