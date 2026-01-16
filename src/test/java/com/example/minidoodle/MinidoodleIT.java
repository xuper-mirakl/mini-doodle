package com.example.minidoodle;

import com.example.minidoodle.application.MeetingService;
import com.example.minidoodle.application.SlotService;
import com.example.minidoodle.application.UserService;
import com.example.minidoodle.application.Exceptions.BadRequest;
import com.example.minidoodle.application.Exceptions.Conflict;
import com.example.minidoodle.domain.SlotStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:16-alpine:///minidoodle",
    "spring.datasource.username=minidoodle",
    "spring.datasource.password=minidoodle"
})
class MeetingSchedulingIT {

  @Autowired private UserService users;
  @Autowired private SlotService slots;
  @Autowired private MeetingService meetings;

  private static final Instant BASE = Instant.parse("2026-01-15T10:00:00Z");

  // --------------------------------------------------
  // Happy path
  // --------------------------------------------------

  @Test
  void schedulesMeeting_andConvertsSlotToBusy() {
    var alice = users.create("alice@test.com", "Alice");
    var bob   = users.create("bob@test.com", "Bob");

    var slot = slots.create(
        alice.getId(),
        BASE,
        Duration.ofMinutes(60),
        SlotStatus.AVAILABLE
    );

    var meeting = meetings.schedule(
        alice.getId(),
        slot.getId(),
        "Standup",
        "Daily sync",
        List.of(bob.getId())
    );

    assertNotNull(meeting.getId());

    var updatedSlot = slots.get(slot.getId());
    assertEquals(SlotStatus.BUSY, updatedSlot.getStatus());
    assertNotNull(updatedSlot.getMeeting());
    assertEquals(meeting.getId(), updatedSlot.getMeeting().getId());
  }

  // --------------------------------------------------
  // Authorization
  // --------------------------------------------------

  @Test
  void cannotScheduleMeeting_onAnotherUsersSlot() {
    var alice = users.create("alice2@test.com", "Alice");
    var bob   = users.create("bob2@test.com", "Bob");

    var slot = slots.create(
        alice.getId(),
        BASE,
        Duration.ofMinutes(30),
        SlotStatus.AVAILABLE
    );

    assertThrows(RuntimeException.class, () ->
        meetings.schedule(
            bob.getId(),
            slot.getId(),
            "Invalid",
            "Should fail",
            List.of()
        )
    );
  }

  // --------------------------------------------------
  // Slot state validation
  // --------------------------------------------------

  @Test
  void cannotScheduleMeeting_onBusySlot() {
    var alice = users.create("alice3@test.com", "Alice");

    var slot = slots.create(
        alice.getId(),
        BASE,
        Duration.ofMinutes(30),
        SlotStatus.AVAILABLE
    );

    meetings.schedule(
        alice.getId(),
        slot.getId(),
        "First",
        "Initial booking",
        List.of()
    );

    assertThrows(Conflict.class, () ->
        meetings.schedule(
            alice.getId(),
            slot.getId(),
            "Second",
            "Should fail",
            List.of()
        )
    );
  }

  // --------------------------------------------------
  // Participant conflict
  // --------------------------------------------------

  @Test
  void participantCannotBeDoubleBooked() {
    var alice = users.create("alice4@test.com", "Alice");
    var bob   = users.create("bob4@test.com", "Bob");
    var carl  = users.create("carl@test.com", "Carl");

    var slot1 = slots.create(
        alice.getId(),
        BASE,
        Duration.ofMinutes(60),
        SlotStatus.AVAILABLE
    );

    meetings.schedule(
        alice.getId(),
        slot1.getId(),
        "M1",
        "First",
        List.of(bob.getId())
    );

    var slot2 = slots.create(
        carl.getId(),
        BASE.plus(Duration.ofMinutes(30)),
        Duration.ofMinutes(60),
        SlotStatus.AVAILABLE
    );

    assertThrows(RuntimeException.class, () ->
        meetings.schedule(
            carl.getId(),
            slot2.getId(),
            "M2",
            "Overlap",
            List.of(bob.getId())
        )
    );
  }

  // --------------------------------------------------
  // Organizer conflict
  // --------------------------------------------------

  @Test
  void cannotCreateOverlappingSlots_forSameUser() {
    var alice = users.create("alice5@test.com", "Alice");

    slots.create(
        alice.getId(),
        BASE,
        Duration.ofMinutes(60),
        SlotStatus.AVAILABLE
    );

    // Overlapping slot should be rejected by DB exclusion constraint
    assertThrows(DataIntegrityViolationException.class, () ->
        slots.create(
            alice.getId(),
            BASE.plus(Duration.ofMinutes(15)),
            Duration.ofMinutes(30),
            SlotStatus.AVAILABLE
        )
    );
  }

  // --------------------------------------------------
  // Atomicity
  // --------------------------------------------------

  @Test
  void scheduleIsAtomic_onFailure_slotRemainsAvailable() {
    var alice = users.create("alice6@test.com", "Alice");

    var slot = slots.create(
        alice.getId(),
        BASE,
        Duration.ofMinutes(30),
        SlotStatus.AVAILABLE
    );

    assertThrows(BadRequest.class, () ->
        meetings.schedule(
            alice.getId(),
            slot.getId(),
            null,
            "Invalid title",
            List.of()
        )
    );

    var unchanged = slots.get(slot.getId());
    assertEquals(SlotStatus.AVAILABLE, unchanged.getStatus());
    assertNull(unchanged.getMeeting());
  }

  // --------------------------------------------------
  // Concurrency (very important)
  // --------------------------------------------------

  @Test
  void concurrentScheduling_onlyOneSucceeds() throws Exception {
    var alice = users.create("alice7@test.com", "Alice");

    var slot = slots.create(
        alice.getId(),
        BASE,
        Duration.ofMinutes(30),
        SlotStatus.AVAILABLE
    );

    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch ready = new CountDownLatch(2);
    CountDownLatch start = new CountDownLatch(1);

    Callable<UUID> task = () -> {
      ready.countDown();
      start.await();
      return meetings.schedule(
          alice.getId(),
          slot.getId(),
          "Race",
          "Concurrent",
          List.of()
      ).getId();
    };

    Future<UUID> f1 = pool.submit(task);
    Future<UUID> f2 = pool.submit(task);

    ready.await();
    start.countDown();

    int success = 0;

    try { f1.get(); success++; } catch (ExecutionException ignored) {}
    try { f2.get(); success++; } catch (ExecutionException ignored) {}

    pool.shutdownNow();

    assertEquals(1, success, "Only one meeting should be created");

    var updatedSlot = slots.get(slot.getId());
    assertEquals(SlotStatus.BUSY, updatedSlot.getStatus());
    assertNotNull(updatedSlot.getMeeting());
  }
}
