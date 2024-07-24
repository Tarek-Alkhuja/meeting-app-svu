package com.webappproject.meetingapp.mappers;


import com.webappproject.meetingapp.dto.UserDto;
import com.webappproject.meetingapp.models.User;

public class UserMapper {
    public static UserDto mapToUserDto(User user) {
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .password(user.getPassword())
                .build();
        return userDto;
    }
}


