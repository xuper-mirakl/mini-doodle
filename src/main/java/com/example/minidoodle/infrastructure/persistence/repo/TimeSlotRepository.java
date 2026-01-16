package com.example.minidoodle.infrastructure.persistence.repo;

import com.example.minidoodle.domain.SlotStatus;
import com.example.minidoodle.infrastructure.persistence.entity.TimeSlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimeSlotRepository extends JpaRepository<TimeSlotEntity, UUID> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from TimeSlotEntity s where s.id = :id")
  Optional<TimeSlotEntity> findByIdForUpdate(@Param("id") UUID id);

  @Query("""
      select s from TimeSlotEntity s
      where s.user.id = :userId
        and s.endTs > :from
        and s.startTs < :to
      order by s.startTs
      """)
  List<TimeSlotEntity> findAllInRange(@Param("userId") UUID userId,
                                      @Param("from") Instant from,
                                      @Param("to") Instant to);

  @Query("""
      select s from TimeSlotEntity s
      where s.user.id = :userId
        and s.status = :status
        and s.endTs > :from
        and s.startTs < :to
      order by s.startTs
      """)
  List<TimeSlotEntity> findAllInRangeByStatus(@Param("userId") UUID userId,
                                              @Param("status") SlotStatus status,
                                              @Param("from") Instant from,
                                              @Param("to") Instant to);

  @Query("""
      select s from TimeSlotEntity s
      where s.user.id in :userIds
        and s.endTs > :from
        and s.startTs < :to
      order by s.user.id, s.startTs
      """)
  List<TimeSlotEntity> findAllUsersInRange(@Param("userIds") List<UUID> userIds,
                                           @Param("from") Instant from,
                                           @Param("to") Instant to);
}
