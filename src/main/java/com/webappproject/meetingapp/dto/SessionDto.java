package com.webappproject.meetingapp.dto;

import com.webappproject.meetingapp.models.User;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SessionDto {
        private Long id;
        @NotEmpty(message = "Title is required")
        private String title;
        @NotNull(message = "Start time is required")
        private LocalDateTime startTime;
        @NotNull(message = "End time is required")
        private LocalDateTime endTime;
        private User createdBy;
        private List<RecordingDto> recordings;
}






