package com.webappproject.meetingapp.controllers;

import com.webappproject.meetingapp.MeetingService;
import com.webappproject.meetingapp.dto.RecordingDto;
import com.webappproject.meetingapp.dto.SessionDto;
import com.webappproject.meetingapp.models.Recording;
import com.webappproject.meetingapp.models.Session;
import com.webappproject.meetingapp.services.RecordingService;
import com.webappproject.meetingapp.services.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
public class StudentController {
    private SessionService sessionService;
    private RecordingService recordingService;
    private MeetingService meetingService;
    @Autowired
    public StudentController(SessionService sessionService, RecordingService recordingService,
                             MeetingService meetingService) {
        this.sessionService = sessionService;
        this.recordingService = recordingService;
        this.meetingService = meetingService;
    }

    @GetMapping("/student/home")
    public String studentHome() {

        return "/student/studentHome";
    }

    @GetMapping("/student/meeting")
    public String studentMeeting(Model model) {
        String htmlContent = meetingService.getHtmlContent();
        model.addAttribute("content", htmlContent);
        return "meeting";
    }

    @GetMapping("/student/sessions")
    public String listSessions(Model model) {
        List<SessionDto> sessions = sessionService.findAllSessions();
        model.addAttribute("sessions", sessions);
        return findSessionPaginated(1, "title", "asc", model);
    }
    @GetMapping("/student/searchSessions")
    public String searchSessionsByUserId(@RequestParam(value = "userId", required = false) Long userId, Model model) {
        if (userId == null) {
            model.addAttribute("error", "User ID is required to search sessions.");
            return "redirect:/student/sessions";
        }
        List<SessionDto> sessions = sessionService.findSessionsByUserId(userId);
        model.addAttribute("sessions", sessions);
        return "/student/sessions-list";
    }

    @GetMapping("/student/sessions/{sessionId}/downloadRecordings")
    public String listRecordings(@PathVariable("sessionId") long sessionId, Model model) {
        List<RecordingDto> recordings = recordingService.getRecordingsBySessionId(sessionId);
        model.addAttribute("recordings", recordings);
        model.addAttribute("sessionId", sessionId);
        return findRecordingPaginated(1, "id", "asc", model);
    }

    @GetMapping("/student/recordings/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        Resource resource = recordingService.loadFileAsResource(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/student/sessionPage/{pageNo}")
    public String findSessionPaginated(@PathVariable(value = "pageNo") int pageNo,
                                       @RequestParam("sortField") String sortField,
                                       @RequestParam("sortDir") String sortDir,
                                       Model model) {
        int pageSize = 5;

        Page<Session> page = sessionService.findSessionPaginated(pageNo, pageSize, sortField, sortDir);
        List<Session> listSessions = page.getContent();

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        model.addAttribute("listSessions", listSessions);
        return "/student/sessions-list";
    }

    @GetMapping("/student/recordingPage/{pageNo}")
    public String findRecordingPaginated(@PathVariable (value = "pageNo") int pageNo,
                                         @RequestParam("sortField") String sortField,
                                         @RequestParam("sortDir") String sortDir,
                                         Model model) {
        int pageSize = 5;

        Page<Recording> page = recordingService.findRecordingPaginated(pageNo, pageSize, sortField, sortDir);
        List<Recording> listRecordings = page.getContent();;

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        model.addAttribute("listRecordings", listRecordings);
        return "/student/download-recordings";
    }
}

