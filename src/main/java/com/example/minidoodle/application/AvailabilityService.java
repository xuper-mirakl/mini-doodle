package com.example.minidoodle.application;

import com.example.minidoodle.domain.Interval;
import com.example.minidoodle.domain.SlotStatus;
import com.example.minidoodle.domain.Calendar;
import com.example.minidoodle.infrastructure.persistence.entity.TimeSlotEntity;
import com.example.minidoodle.infrastructure.persistence.repo.TimeSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

  private final TimeSlotRepository slots;

  public AvailabilityService(TimeSlotRepository slots) {
    this.slots = slots;
  }

  @Transactional(readOnly = true)
  public AvailabilityResult availability(List<UUID> userIds, Instant from, Instant to) {
    if (userIds == null || userIds.isEmpty()) {
      return new AvailabilityResult(from, to, List.of(), List.of());
    }

    // Fetch all slots for all users in one DB query (then split in-memory)
    List<TimeSlotEntity> all = slots.findAllUsersInRange(userIds, from, to);

    Map<UUID, List<TimeSlotEntity>> byUser = all.stream()
        .collect(Collectors.groupingBy(s -> s.getUser().getId()));

    List<UserAvailability> perUser = new ArrayList<>();

    for (UUID userId : userIds) {
      List<TimeSlotEntity> userSlots = byUser.getOrDefault(userId, List.of());

      List<Interval> available = userSlots.stream()
          .filter(s -> s.getStatus() == SlotStatus.AVAILABLE)
          .map(s -> new Interval(max(s.getStartTs(), from), min(s.getEndTs(), to)))
          .sorted(Comparator.comparing(Interval::start))
          .toList();

      List<Interval> busy = userSlots.stream()
          .filter(s -> s.getStatus() == SlotStatus.BUSY)
          .map(s -> new Interval(max(s.getStartTs(), from), min(s.getEndTs(), to)))
          .sorted(Comparator.comparing(Interval::start))
          .toList();

      List<Interval> free = subtract(available, busy);

      // Domain "Calendar" snapshot (domain-only concept)
      @SuppressWarnings("unused")
      Calendar.Snapshot snapshot = new Calendar.Snapshot(userId, from, to, available, busy, free);

      perUser.add(new UserAvailability(userId, free, busy));
    }

    // Common free = intersection across all users
    List<Interval> commonFree = intersectAll(perUser.stream().map(UserAvailability::free).toList());

    return new AvailabilityResult(from, to, perUser, commonFree);
  }

  // AVAILABLE - BUSY (both lists are sorted, non-overlapping within themselves)
  static List<Interval> subtract(List<Interval> available, List<Interval> busy) {
    List<Interval> out = new ArrayList<>();
    int j = 0;

    for (Interval a : available) {
      Instant cur = a.start();
      while (j < busy.size() && !busy.get(j).end().isAfter(a.start())) j++; // busy ends before a starts

      int k = j;
      while (k < busy.size() && busy.get(k).start().isBefore(a.end())) {
        Interval b = busy.get(k);

        if (b.start().isAfter(cur)) {
          out.add(new Interval(cur, min(b.start(), a.end())));
        }
        if (b.end().isAfter(cur)) {
          cur = max(cur, b.end());
        }
        if (!a.end().isAfter(cur)) break;
        k++;
      }

      if (a.end().isAfter(cur)) {
        out.add(new Interval(cur, a.end()));
      }
    }
    return out;
  }

  static List<Interval> intersectAll(List<List<Interval>> lists) {
    if (lists.isEmpty()) return List.of();
    List<Interval> acc = lists.get(0);
    for (int i = 1; i < lists.size(); i++) {
      acc = intersectTwo(acc, lists.get(i));
      if (acc.isEmpty()) break;
    }
    return acc;
  }

  static List<Interval> intersectTwo(List<Interval> a, List<Interval> b) {
    List<Interval> out = new ArrayList<>();
    int i = 0, j = 0;
    while (i < a.size() && j < b.size()) {
      Interval x = a.get(i);
      Interval y = b.get(j);

      Instant start = max(x.start(), y.start());
      Instant end = min(x.end(), y.end());
      if (end.isAfter(start)) out.add(new Interval(start, end));

      if (x.end().isBefore(y.end())) i++;
      else j++;
    }
    return out;
  }

  static Instant max(Instant a, Instant b) { return a.isAfter(b) ? a : b; }
  static Instant min(Instant a, Instant b) { return a.isBefore(b) ? a : b; }

  public record UserAvailability(UUID userId, List<Interval> free, List<Interval> busy) {}
  public record AvailabilityResult(Instant from, Instant to, List<UserAvailability> users, List<Interval> commonFree) {}
}
