package com.webappproject.meetingapp.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class PasswordGenerator {
	public static void main(String[] args) {

		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

		System.out.println(encoder.encode("password123"));
	}
}
