package com.webappproject.meetingapp.mappers;

import com.webappproject.meetingapp.dto.RecordingDto;
import com.webappproject.meetingapp.models.Recording;

public class RecordingMapper {
    public static Recording mapToRecording(RecordingDto recordingDto) {
        return Recording.builder()
                .id(recordingDto.getId())
                .filePath(recordingDto.getFilePath())
                .createdOn(recordingDto.getCreatedOn())
                .session(recordingDto.getSession())
                .build();
    }

    public static RecordingDto mapToRecordingDto(Recording recording) {
        return RecordingDto.builder()
                .id(recording.getId())
                .filePath(recording.getFilePath())
                .createdOn(recording.getCreatedOn())
                .session(recording.getSession())
                .build();
    }
}


