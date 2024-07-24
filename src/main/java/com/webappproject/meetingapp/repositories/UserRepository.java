package com.webappproject.meetingapp.repositories;

import com.webappproject.meetingapp.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface UserRepository  extends JpaRepository<User, Long> {
    User findByEmail(String email);

    List<User> findByEmailContainingIgnoreCase(String email);
}




