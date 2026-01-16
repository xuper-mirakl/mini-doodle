package com.example.minidoodle.api.controller;

import com.example.minidoodle.api.dto.*;
import com.example.minidoodle.application.SlotService;
import com.example.minidoodle.domain.SlotStatus;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class SlotsController {

  private final SlotService slots;

  public SlotsController(SlotService slots) {
    this.slots = slots;
  }

  @PostMapping("/users/{userId}/slots")
  public SlotResponse create(@PathVariable UUID userId, @Valid @RequestBody CreateSlotRequest req) {
    var s = slots.create(userId, req.start(), Duration.ofMinutes(req.durationMinutes()), req.status());
    return toResponse(s);
  }

  @GetMapping("/users/{userId}/slots")
  public List<SlotResponse> list(@PathVariable UUID userId,
                                @RequestParam Instant from,
                                @RequestParam Instant to,
                                @RequestParam(required = false) SlotStatus status) {
    return slots.list(userId, from, to, status).stream().map(this::toResponse).toList();
  }

  @PatchMapping("/slots/{slotId}")
  public SlotResponse update(@PathVariable UUID slotId, @Valid @RequestBody UpdateSlotRequest req) {
    Duration d = req.durationMinutes() == null ? null : Duration.ofMinutes(req.durationMinutes());
    var s = slots.update(slotId, req.start(), d, req.status());
    return toResponse(s);
  }

  @DeleteMapping("/slots/{slotId}")
  public void delete(@PathVariable UUID slotId) {
    slots.delete(slotId);
  }

  private SlotResponse toResponse(com.example.minidoodle.infrastructure.persistence.entity.TimeSlotEntity s) {
    return new SlotResponse(
        s.getId(),
        s.getUser().getId(),
        s.getStartTs(),
        s.getEndTs(),
        s.getStatus(),
        s.getMeeting() == null ? null : s.getMeeting().getId()
    );
  }
}
