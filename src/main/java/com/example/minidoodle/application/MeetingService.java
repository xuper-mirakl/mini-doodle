package com.example.minidoodle.application;

import com.example.minidoodle.domain.SlotStatus;
import com.example.minidoodle.infrastructure.persistence.entity.MeetingEntity;
import com.example.minidoodle.infrastructure.persistence.entity.TimeSlotEntity;
import com.example.minidoodle.infrastructure.persistence.repo.MeetingRepository;
import com.example.minidoodle.infrastructure.persistence.repo.TimeSlotRepository;
import com.example.minidoodle.infrastructure.persistence.repo.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.example.minidoodle.application.Exceptions.*;

@Service
public class MeetingService {

  private final MeetingRepository meetings;
  private final TimeSlotRepository slots;
  private final UserRepository users;
  private final UserService userService;

  public MeetingService(MeetingRepository meetings, TimeSlotRepository slots,
                        UserRepository users, UserService userService) {
    this.meetings = meetings;
    this.slots = slots;
    this.users = users;
    this.userService = userService;
  }

  @Transactional
  public MeetingEntity schedule(UUID organizerId,
                                UUID slotId,
                                String title,
                                String description,
                                List<UUID> participantIds) {

    if (title == null || title.isBlank()) throw new BadRequest("title is required");

    // Lock slot to prevent double-booking races
    TimeSlotEntity slot = slots.findByIdForUpdate(slotId)
        .orElseThrow(() -> new NotFound("slot not found"));

    if (!slot.getUser().getId().equals(organizerId)) {
      throw new Conflict("slot does not belong to organizer");
    }
    if (slot.getStatus() != SlotStatus.AVAILABLE) {
      throw new Conflict("slot is not AVAILABLE");
    }

    var organizer = userService.get(organizerId);
    var now = Instant.now();

    var meeting = new MeetingEntity(
        UUID.randomUUID(),
        organizer,
        slot.getStartTs(),
        slot.getEndTs(),
        title,
        description,
        now
    );

    // Load participants in one query
    Set<UUID> uniqueIds = new LinkedHashSet<>();
    if (participantIds != null) uniqueIds.addAll(participantIds);
    uniqueIds.remove(organizerId); // organizer doesn't need a participant busy-slot

    var participantEntities = uniqueIds.isEmpty()
        ? List.<com.example.minidoodle.infrastructure.persistence.entity.UserEntity>of()
        : users.findAllById(uniqueIds);

    if (participantEntities.size() != uniqueIds.size()) {
      throw new BadRequest("one or more participantIds do not exist");
    }

    meeting.setParticipants(new LinkedHashSet<>(participantEntities));
    meeting = meetings.save(meeting);

    // Convert organizer slot to BUSY + link to meeting
    slot.setStatus(SlotStatus.BUSY);
    slot.setMeeting(meeting);
    slots.save(slot);

    // Create BUSY slots for participants (protected by DB overlap constraint on BUSY slots)
    for (var p : participantEntities) {
      var busySlot = new TimeSlotEntity(
          UUID.randomUUID(),
          p,
          meeting.getStartTs(),
          meeting.getEndTs(),
          SlotStatus.BUSY,
          meeting,
          now,
          now
      );
      try {
        slots.save(busySlot);
      } catch (DataIntegrityViolationException e) {
        throw new Conflict("participant " + p.getId() + " has a conflicting BUSY slot");
      }
    }

    return meeting;
  }

  @Transactional(readOnly = true)
  public MeetingEntity get(UUID id) {
    return meetings.findById(id).orElseThrow(() -> new NotFound("meeting not found"));
  }
}
