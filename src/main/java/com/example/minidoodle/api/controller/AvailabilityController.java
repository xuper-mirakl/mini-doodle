package com.example.minidoodle.api.controller;

import com.example.minidoodle.api.dto.AvailabilityResponse;
import com.example.minidoodle.api.dto.IntervalResponse;
import com.example.minidoodle.application.AvailabilityService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/availability")
public class AvailabilityController {

  private final AvailabilityService availability;

  public AvailabilityController(AvailabilityService availability) {
    this.availability = availability;
  }

  @GetMapping
  public AvailabilityResponse get(@RequestParam List<UUID> userIds,
                                  @RequestParam Instant from,
                                  @RequestParam Instant to) {
    var res = availability.availability(userIds, from, to);

    var users = res.users().stream()
        .map(u -> new AvailabilityResponse.UserAvailability(
            u.userId(),
            u.free().stream().map(i -> new IntervalResponse(i.start(), i.end())).toList(),
            u.busy().stream().map(i -> new IntervalResponse(i.start(), i.end())).toList()
        ))
        .toList();

    var common = res.commonFree().stream()
        .map(i -> new IntervalResponse(i.start(), i.end()))
        .toList();

    return new AvailabilityResponse(res.from(), res.to(), users, common);
  }
}
