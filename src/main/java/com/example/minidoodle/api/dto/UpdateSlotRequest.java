package com.example.minidoodle.api.dto;

import com.example.minidoodle.domain.SlotStatus;
import jakarta.validation.constraints.Min;

import java.time.Instant;

public record UpdateSlotRequest(
    Instant start,
    @Min(1) Integer durationMinutes,
    SlotStatus status
) {}
