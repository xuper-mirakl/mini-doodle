package com.example.minidoodle.infrastructure.persistence.repo;

import com.example.minidoodle.infrastructure.persistence.entity.MeetingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MeetingRepository extends JpaRepository<MeetingEntity, UUID> {
}
