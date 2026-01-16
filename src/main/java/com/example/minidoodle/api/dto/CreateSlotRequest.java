package com.example.minidoodle.api.dto;

import com.example.minidoodle.domain.SlotStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateSlotRequest(
    @NotNull Instant start,
    @Min(1) int durationMinutes,
    SlotStatus status
) {}
