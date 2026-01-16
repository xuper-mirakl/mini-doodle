package com.example.minidoodle.application;

import com.example.minidoodle.domain.SlotStatus;
import com.example.minidoodle.infrastructure.persistence.entity.TimeSlotEntity;
import com.example.minidoodle.infrastructure.persistence.repo.TimeSlotRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.example.minidoodle.application.Exceptions.*;

@Service
public class SlotService {

  private final TimeSlotRepository slots;
  private final UserService userService;

  public SlotService(TimeSlotRepository slots, UserService userService) {
    this.slots = slots;
    this.userService = userService;
  }

  @Transactional
  public TimeSlotEntity create(UUID userId, Instant start, Duration duration, SlotStatus status) {
    if (duration == null || duration.isZero() || duration.isNegative()) {
      throw new BadRequest("durationMinutes must be > 0");
    }
    var end = start.plus(duration);
    var user = userService.get(userId);
    var now = Instant.now();

    var entity = new TimeSlotEntity(
        UUID.randomUUID(),
        user,
        start,
        end,
        status == null ? SlotStatus.AVAILABLE : status,
        null,
        now,
        now
    );

    try {
      return slots.save(entity);
    } catch (DataIntegrityViolationException e) {
      // Most common cause: exclusion constraint (overlap)
      throw new Conflict("slot overlaps an existing slot for this user/status");
    }
  }

  @Transactional(readOnly = true)
  public TimeSlotEntity get(UUID slotId) {
    return slots.findById(slotId).orElseThrow(() -> new NotFound("slot not found"));
  }

  @Transactional
  public TimeSlotEntity update(UUID slotId, Instant newStart, Duration newDuration, SlotStatus newStatus) {
    var slot = get(slotId);

    if (slot.getMeeting() != null && newStatus == SlotStatus.AVAILABLE) {
      throw new Conflict("cannot set to AVAILABLE: slot is linked to a meeting");
    }

    if (newStart != null) slot.setStartTs(newStart);
    if (newDuration != null) {
      if (newDuration.isZero() || newDuration.isNegative()) throw new BadRequest("durationMinutes must be > 0");
      slot.setEndTs(slot.getStartTs().plus(newDuration));
    }
    if (newStatus != null) slot.setStatus(newStatus);

    try {
      return slots.save(slot);
    } catch (DataIntegrityViolationException e) {
      throw new Conflict("update would cause an overlap for this user/status");
    }
  }

  @Transactional
  public void delete(UUID slotId) {
    var slot = get(slotId);
    if (slot.getMeeting() != null) {
      throw new Conflict("cannot delete: slot is linked to a meeting");
    }
    slots.delete(slot);
  }

  @Transactional(readOnly = true)
  public List<TimeSlotEntity> list(UUID userId, Instant from, Instant to, SlotStatus status) {
    if (status == null) {
      return slots.findAllInRange(userId, from, to);
    }
    return slots.findAllInRangeByStatus(userId, status, from, to);
  }
}
