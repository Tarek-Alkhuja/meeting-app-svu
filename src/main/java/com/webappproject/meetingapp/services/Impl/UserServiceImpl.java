package com.webappproject.meetingapp.services.Impl;

import com.webappproject.meetingapp.dto.UserDto;
import com.webappproject.meetingapp.mappers.UserMapper;
import com.webappproject.meetingapp.models.Role;
import com.webappproject.meetingapp.models.User;
import com.webappproject.meetingapp.repositories.RoleRepository;
import com.webappproject.meetingapp.repositories.UserRepository;
import com.webappproject.meetingapp.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void saveUser(User user) {
        if (user.getId() != null && userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("User with this ID already exists");
        }
        if (user.getId() == null) {
            for (Role role : user.getRoles()) {
                roleRepository.save(role);
            }
            user.setPassword(passwordEncoder().encode(user.getPassword()));
        }
        userRepository.save(user);
    }

    @Override
    public void updateUser(Long userId, User updatedUser) {
        User existingUser = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("User not found"));

        existingUser.setFullName(updatedUser.getFullName());
        existingUser.setEmail(updatedUser.getEmail());
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder().encode(updatedUser.getPassword()));
        }
        existingUser.getRoles().clear();
        existingUser.getRoles().addAll(updatedUser.getRoles());

        userRepository.save(existingUser);
    }

    @Override
    public void deleteUserById(Long id) {

        User user = userRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("User not found"));
        user.getRoles().forEach(role -> role.getUsers().remove(user));
        userRepository.deleteById(id);
    }

    @Override
    public User getUserById(Long id) {

        return userRepository.findById(id).get();
    }

    @Override
    public Page<User> findPaginated(int pageNo, int pageSize, String sortField, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
                Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
        return this.userRepository.findAll(pageable);
    }

    @Override
    public List<User> searchUsersByEmail(String email) {
        return userRepository.findByEmailContainingIgnoreCase(email);
    }
}

