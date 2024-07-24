package com.webappproject.meetingapp.repositories;

import com.webappproject.meetingapp.models.Session;
import com.webappproject.meetingapp.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByCreatedBy(User user);
    Page<Session> findByCreatedBy(User user, Pageable pageable);
    Page<Session> findAll(Pageable pageable);

}




