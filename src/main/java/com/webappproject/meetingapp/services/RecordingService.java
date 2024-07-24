package com.webappproject.meetingapp.services;

import com.webappproject.meetingapp.dto.RecordingDto;
import com.webappproject.meetingapp.models.Recording;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RecordingService {
    void createRecording(Long sessionId, RecordingDto recordingDto, MultipartFile file);
    List<RecordingDto> findAllRecordings();
    RecordingDto findByRecordingId(Long recordingId);
    void updateRecording(Long recordingId,RecordingDto recordingDto,MultipartFile file);
    void deleteRecording(long recordingId);
    List<RecordingDto> getRecordingsBySessionId(Long sessionId);
    Page<Recording> findRecordingPaginated(int pageNo, int pageSize, String sortField, String sortDir);
    Resource loadFileAsResource(String fileName);
}






