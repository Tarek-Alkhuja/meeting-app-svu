package com.webappproject.meetingapp.services;

import com.webappproject.meetingapp.dto.SessionDto;
import com.webappproject.meetingapp.models.Session;
import com.webappproject.meetingapp.models.User;
import org.springframework.data.domain.Page;
import java.util.List;

public interface SessionService {
    List<SessionDto> findAllSessions();
    Session saveSession(SessionDto sessionDto);
    SessionDto findSessionById(Long sessionId);
    void updateSession(SessionDto sessionDto);
    void delete(Long sessionId);
    List<SessionDto> findSessionsByUser(User user);
    List<SessionDto> findSessionsByUserId(Long userId);
    Page<Session> findPaginatedByUser(User user, int pageNo, int pageSize, String sortField, String sortDir);
    Page<Session> findSessionPaginated(int pageNo, int pageSize, String sortField, String sortDir);
}




