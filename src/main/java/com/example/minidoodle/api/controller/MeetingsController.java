package com.example.minidoodle.api.controller;

import com.example.minidoodle.api.dto.CreateMeetingRequest;
import com.example.minidoodle.api.dto.MeetingResponse;
import com.example.minidoodle.application.MeetingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/meetings")
public class MeetingsController {

  private final MeetingService meetings;

  public MeetingsController(MeetingService meetings) {
    this.meetings = meetings;
  }

  @PostMapping
  public MeetingResponse create(@Valid @RequestBody CreateMeetingRequest req) {
    var m = meetings.schedule(
        req.organizerId(),
        req.slotId(),
        req.title(),
        req.description(),
        req.participantIds()
    );
    List<UUID> participants = m.getParticipants().stream().map(p -> p.getId()).toList();
    return new MeetingResponse(
        m.getId(),
        m.getOrganizer().getId(),
        m.getStartTs(),
        m.getEndTs(),
        m.getTitle(),
        m.getDescription(),
        participants
    );
  }

  @GetMapping("/{id}")
  public MeetingResponse get(@PathVariable UUID id) {
    var m = meetings.get(id);
    List<UUID> participants = m.getParticipants().stream().map(p -> p.getId()).toList();
    return new MeetingResponse(
        m.getId(),
        m.getOrganizer().getId(),
        m.getStartTs(),
        m.getEndTs(),
        m.getTitle(),
        m.getDescription(),
        participants
    );
  }
}
