package com.webappproject.meetingapp.repositories;

import com.webappproject.meetingapp.dto.RecordingDto;
import com.webappproject.meetingapp.models.Recording;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordingRepository extends JpaRepository<Recording, Long> {
    List<RecordingDto> findBySessionId(Long sessionId);

    Page<Recording> findAll(Pageable pageable);
}



