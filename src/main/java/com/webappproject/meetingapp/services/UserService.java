package com.webappproject.meetingapp.services;

import com.webappproject.meetingapp.dto.UserDto;
import com.webappproject.meetingapp.models.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {
    User getUserById(Long id);
    User getUserByEmail(String email);
    void saveUser(User user);
    void updateUser(Long userId, User updatedUser);
    void deleteUserById(Long id);
    Page<User> findPaginated(int pageNo, int pageSize, String sortField, String sortDirection);
    List<User> searchUsersByEmail(String email);

}



