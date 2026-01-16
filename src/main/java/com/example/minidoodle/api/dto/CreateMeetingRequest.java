package com.example.minidoodle.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateMeetingRequest(
    @NotNull UUID organizerId,
    @NotNull UUID slotId,
    @NotBlank String title,
    String description,
    List<UUID> participantIds
) {}
