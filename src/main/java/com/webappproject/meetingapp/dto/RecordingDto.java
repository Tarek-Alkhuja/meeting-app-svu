package com.webappproject.meetingapp.dto;

import com.webappproject.meetingapp.models.Session;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
public class RecordingDto {
    private Long id;
    private String filePath;
    private LocalDateTime createdOn;
    private Session session;
}



