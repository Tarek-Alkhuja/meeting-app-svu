package com.webappproject.meetingapp.services.Impl;

import com.webappproject.meetingapp.dto.RecordingDto;
import com.webappproject.meetingapp.models.Recording;
import com.webappproject.meetingapp.models.Session;
import com.webappproject.meetingapp.repositories.RecordingRepository;
import com.webappproject.meetingapp.repositories.SessionRepository;
import com.webappproject.meetingapp.services.RecordingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.webappproject.meetingapp.mappers.RecordingMapper.mapToRecording;
import static com.webappproject.meetingapp.mappers.RecordingMapper.mapToRecordingDto;

@Service
public class RecordingServiceImpl implements RecordingService {
    private SessionRepository sessionRepository;
    private RecordingRepository recordingRepository;
    private final Path fileStorageLocation;

    @Autowired
    public RecordingServiceImpl(SessionRepository sessionRepository, RecordingRepository recordingRepository) {
        this.sessionRepository = sessionRepository;
        this.recordingRepository = recordingRepository;
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }
    @Override
    public void createRecording(Long sessionId, RecordingDto recordingDto, MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
            Recording recording = mapToRecording(recordingDto);
            recording.setFilePath(fileName);
            recording.setSession(session);
            recording.setCreatedOn(LocalDateTime.now()); // Set the current timestamp
            recordingRepository.save(recording);
        } catch (Exception ex) {
            throw new RuntimeException("Could not store file " + file.getOriginalFilename() + ". Please try again!", ex);
        }
    }
    @Override
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + fileName);
            }
        } catch (Exception ex) {
            throw new RuntimeException("File not found " + fileName, ex);
        }
    }

    @Override
    public List<RecordingDto> findAllRecordings() {

        List<Recording> recordings = recordingRepository.findAll();
        return recordings.stream().map(recording -> mapToRecordingDto(recording)).collect(Collectors.toList());
    }

    @Override
    public RecordingDto findByRecordingId(Long recordingId) {
        Recording recording = recordingRepository.findById(recordingId).get();
        return mapToRecordingDto(recording);
    }

    @Override
    public void updateRecording(Long recordingId, RecordingDto recordingDto, MultipartFile file) {
        try {
            Recording existingRecording = recordingRepository.findById(recordingId).orElseThrow(() -> new RuntimeException("Recording not found"));
            String oldFileName = existingRecording.getFilePath();
            String fileName = file.getOriginalFilename();

            if (fileName != null && !fileName.isEmpty()) {
                // Delete the old file
                Path oldFilePath = this.fileStorageLocation.resolve(oldFileName).normalize();
                Files.deleteIfExists(oldFilePath);

                // Save the new file
                Path targetLocation = this.fileStorageLocation.resolve(fileName);
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                existingRecording.setFilePath(fileName); // Update file path if a new file is uploaded
            }
            // Ensure the session is not lost during update
            existingRecording.setSession(recordingDto.getSession());
            recordingRepository.save(existingRecording);
        } catch (Exception ex) {
            throw new RuntimeException("Could not update recording. Please try again!", ex);
        }
    }


    @Override
    public void deleteRecording(long recordingId) {
        try {
            Recording recording = recordingRepository.findById(recordingId).orElseThrow(() -> new RuntimeException("Recording not found"));
            String fileName = recording.getFilePath();

            // Delete the file from the uploads directory
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);

            // Delete the recording from the repository
            recordingRepository.deleteById(recordingId);
        } catch (Exception ex) {
            throw new RuntimeException("Could not delete recording. Please try again!", ex);
        }
    }

    @Override
    public List<RecordingDto> getRecordingsBySessionId(Long sessionId) {
        return recordingRepository.findBySessionId(sessionId);
    }

    @Override
    public Page<Recording> findRecordingPaginated(int pageNo, int pageSize, String sortField, String sortDir) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sortDir.equals("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending());
        return recordingRepository.findAll(pageable);
    }
}



