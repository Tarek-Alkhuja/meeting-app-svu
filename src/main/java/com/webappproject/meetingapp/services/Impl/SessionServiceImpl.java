package com.webappproject.meetingapp.services.Impl;

import com.webappproject.meetingapp.dto.SessionDto;
import com.webappproject.meetingapp.models.Session;
import com.webappproject.meetingapp.models.User;
import com.webappproject.meetingapp.repositories.SessionRepository;
import com.webappproject.meetingapp.repositories.UserRepository;
import com.webappproject.meetingapp.security.SecurityUtil;
import com.webappproject.meetingapp.services.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.webappproject.meetingapp.mappers.SessionMapper.mapToSession;
import static com.webappproject.meetingapp.mappers.SessionMapper.mapToSessionDto;

@Service
public class SessionServiceImpl implements SessionService {

    private SessionRepository sessionRepository;
    private UserRepository userRepository;

    @Autowired
    public SessionServiceImpl(SessionRepository sessionRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<SessionDto> findAllSessions() {
        List<Session> sessions = sessionRepository.findAll();
        return sessions.stream().map((session) -> mapToSessionDto(session)).collect(Collectors.toList());
    }

    @Override
    public Session saveSession(SessionDto sessionDto) {
        String email = SecurityUtil.getSessionUser();
        User user = userRepository.findByEmail(email);
        Session session = mapToSession(sessionDto);
        session.setCreatedBy(user);
        return sessionRepository.save(session);
    }

    @Override
    public SessionDto findSessionById(Long sessionId) {
        Session session = sessionRepository.findById(sessionId).get();
        return mapToSessionDto(session);
    }

    @Override
    public void updateSession(SessionDto sessionDto) {
        String email = SecurityUtil.getSessionUser();
        User user = userRepository.findByEmail(email);
        Session session = mapToSession(sessionDto);
        session.setCreatedBy(user);
        sessionRepository.save(session);
    }

    @Override
    public void delete(Long sessionId) {
        sessionRepository.deleteById(sessionId);
    }


    @Override
    public List<SessionDto> findSessionsByUser(User user) {
        List<Session> sessions = sessionRepository.findByCreatedBy(user);
        return sessions.stream().map(session -> mapToSessionDto(session)).collect(Collectors.toList());
    }

    @Override
    public List<SessionDto> findSessionsByUserId(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            List<Session> sessions = sessionRepository.findByCreatedBy(user);
            return sessions.stream().map((session) -> mapToSessionDto(session)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public Page<Session> findPaginatedByUser(User user, int pageNo, int pageSize, String sortField, String sortDir) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sortDir.equals("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending());
        return sessionRepository.findByCreatedBy(user, pageable);
    }

    @Override
    public Page<Session> findSessionPaginated(int pageNo, int pageSize, String sortField, String sortDir) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sortDir.equals("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending());
        return sessionRepository.findAll(pageable);
    }
}


