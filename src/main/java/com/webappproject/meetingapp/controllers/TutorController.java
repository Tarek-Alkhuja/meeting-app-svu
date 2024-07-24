package com.webappproject.meetingapp.controllers;


import com.webappproject.meetingapp.MeetingService;
import com.webappproject.meetingapp.dto.RecordingDto;
import com.webappproject.meetingapp.dto.SessionDto;
import com.webappproject.meetingapp.models.Recording;
import com.webappproject.meetingapp.models.Role;
import com.webappproject.meetingapp.models.Session;
import com.webappproject.meetingapp.models.User;
import com.webappproject.meetingapp.security.SecurityUtil;
import com.webappproject.meetingapp.services.RecordingService;
import com.webappproject.meetingapp.services.SessionService;
import com.webappproject.meetingapp.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import static com.webappproject.meetingapp.mappers.SessionMapper.mapToSession;

@Controller
public class TutorController {
    private static final Logger logger = LoggerFactory.getLogger(TutorController.class);
    private SessionService sessionService;
    private UserService userService;
    private RecordingService recordingService;
    private MeetingService meetingService;

    @Autowired
    public TutorController(UserService userService, SessionService sessionService, RecordingService recordingService, MeetingService meetingService) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.recordingService = recordingService;
        this.meetingService = meetingService;
    }

    @GetMapping("/tutor/home")
    public String tutorHome(Model model) {

        return "tutor/tutorHome";
    }

    @GetMapping("/tutor/meeting")
    public String tutorMeeting(Model model) {
        String htmlContent = meetingService.getHtmlContent();
        model.addAttribute("content", htmlContent);
        return "meeting";
    }
    @GetMapping("/tutor/sessions")
    public String listSessions(Model model) {
        String email = SecurityUtil.getSessionUser();
        if (email != null) {
            User user = userService.getUserByEmail(email);
            List<SessionDto> sessions = sessionService.findSessionsByUser(user);
            model.addAttribute("user", user);
            model.addAttribute("sessions", sessions);
        } else {
            model.addAttribute("sessions", List.of()); // Empty list if no user is logged in
        }
        return findSessionPaginated(1, "title", "asc", model);
    }

    @GetMapping("/tutor/sessions/new")
    public String createSessionForm(Model model) {
        Session session = new Session();
        model.addAttribute("session", session);
        return "/tutor/create-session";
    }


    @PostMapping("/tutor/sessions/new")
    public String saveSession(@Valid @ModelAttribute("session") SessionDto sessionDto, BindingResult result, Model model) {

        if(result.hasErrors()) {
            model.addAttribute("session", sessionDto);
            return "/tutor/create-session";
        }
        sessionService.saveSession(sessionDto);
        return "redirect:/tutor/sessions";
    }

    @GetMapping("/tutor/sessions/{sessionId}/edit")
    public String editSessionForm(@PathVariable("sessionId") long sessionId, Model model) {
        SessionDto sessionDto = sessionService.findSessionById(sessionId);
        model.addAttribute("sessionDto", sessionDto);
        return "/tutor/edit-session";
    }

    @PostMapping("/tutor/sessions/edit/{sessionId}")
    public String updateSession(@PathVariable("sessionId") Long sessionId,
                                @Valid @ModelAttribute("sessionDto") SessionDto sessionDto,
                                BindingResult result, Model model) {
        if(result.hasErrors()) {
            model.addAttribute("sessionDto", sessionDto);
            return "tutor/edit-session";
        }
        sessionDto.setId(sessionId);
        sessionService.updateSession(sessionDto);
        return "redirect:/tutor/sessions";
    }

    @GetMapping("/tutor/sessions/{sessionId}/delete")
    public String deleteSession(@PathVariable("sessionId") Long sessionId) {
        sessionService.delete(sessionId);
        return "redirect:/tutor/sessions";
    }


    @GetMapping("/tutor/sessions/{sessionId}/recordings")
    public String listRecordings(@PathVariable("sessionId") long sessionId, Model model) {
        List<RecordingDto> recordings = recordingService.getRecordingsBySessionId(sessionId);
        model.addAttribute("recordings", recordings);
        model.addAttribute("sessionId", sessionId);
        return findRecordingPaginated(1, "id", "asc", model);
    }

    @GetMapping("/tutor/recordings/{sessionId}/new")
    public String createRecordingForm(@PathVariable("sessionId") Long sessionId,Model model){
        Recording recording = new Recording();
        SessionDto sessionDto = sessionService.findSessionById(sessionId);
        Session session = mapToSession(sessionDto);
        recording.setSession(session);
        model.addAttribute("recording",recording );
        model.addAttribute("sessionId", sessionId);
        return "/tutor/create-recording";
    }

    @PostMapping("/tutor/recordings/create/{sessionId}")
    public String createRecording(@PathVariable("sessionId") Long sessionId,
                                  @Valid @ModelAttribute("recording") RecordingDto recordingDto,
                                  BindingResult result,
                                  @RequestParam("file") MultipartFile file,
                                  Model model) {
        if (result.hasErrors()) {
            model.addAttribute("recording", recordingDto);
            return "/tutor/create-recording";
        }

        try {
            recordingService.createRecording(sessionId, recordingDto, file);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error uploading file: " + e.getMessage());
            return "/tutor/create-recording";
        }

        return "redirect:/tutor/sessions/" + sessionId + "/recordings";
    }

    @GetMapping("/tutor/recordings/{recordingId}/edit")
    public String editRecordingForm(@PathVariable("recordingId") Long recordingId, Model model) {
        RecordingDto recording = recordingService.findByRecordingId(recordingId);
        model.addAttribute("recording", recording);
        model.addAttribute("sessionId", recording.getSession().getId()); // Ensure sessionId is passed
        return "/tutor/edit-recording";
    }

    @PostMapping("/tutor/recordings/{recordingId}/edit")
    public String updateRecording(@PathVariable("recordingId") Long recordingId,
                                  @Valid @ModelAttribute("recording") RecordingDto recordingDto,
                                  BindingResult result,
                                  @RequestParam("file") MultipartFile file,
                                  Model model) {
        if (result.hasErrors()) {
            model.addAttribute("recording", recordingDto);
            return "/tutor/edit-recording";
        }

        try {
            // Fetch the existing recording
            RecordingDto existingRecording = recordingService.findByRecordingId(recordingId);
            if (existingRecording == null) {
                model.addAttribute("errorMessage", "Recording not found");
                return "/tutor/edit-recording";
            }
            // Set the session in the DTO
            recordingDto.setSession(existingRecording.getSession());

            // Update the recording
            recordingService.updateRecording(recordingId, recordingDto, file);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating recording: " + e.getMessage());
            return "/tutor/edit-recording";
        }
        return "redirect:/tutor/sessions/" + recordingDto.getSession().getId() + "/recordings";
    }


    @GetMapping("/tutor/recordings/{recordingId}/delete")
    public String deleteRecording(@PathVariable("recordingId") Long recordingId) {
        RecordingDto recording = recordingService.findByRecordingId(recordingId);
        Long sessionId = recording.getSession().getId();
        recordingService.deleteRecording(recordingId);
        return "redirect:/tutor/sessions/" + sessionId + "/recordings";
    }

    @GetMapping("/tutor/sessionPage/{pageNo}")
    public String findSessionPaginated(@PathVariable (value = "pageNo") int pageNo,
                                       @RequestParam("sortField") String sortField,
                                       @RequestParam("sortDir") String sortDir,
                                       Model model) {
        String email = SecurityUtil.getSessionUser();
        User user = userService.getUserByEmail(email);
        int pageSize = 5;

        Page<Session> page = sessionService.findPaginatedByUser(user, pageNo, pageSize, sortField, sortDir);
        List<Session> listSessions = page.getContent();

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        model.addAttribute("listSessions", listSessions);
        return "/tutor/sessions-list";
    }
    @GetMapping("/tutor/recordingPage/{pageNo}")
    public String findRecordingPaginated(@PathVariable (value = "pageNo") int pageNo,
                                       @RequestParam("sortField") String sortField,
                                       @RequestParam("sortDir") String sortDir,
                                       Model model) {
        int pageSize = 5;

        Page<User> page = userService.findPaginated(pageNo, pageSize, sortField, sortDir);
        List<User> listUsers= page.getContent();

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        model.addAttribute("listUsers", listUsers);
        return "/tutor/session-recordings";
    }
}

