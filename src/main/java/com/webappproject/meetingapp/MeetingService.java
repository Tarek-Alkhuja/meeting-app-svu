package com.webappproject.meetingapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MeetingService {
    @Autowired
    private RestTemplate restTemplate;

    public MeetingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private static final String MEETING_SERVICE_URL = "http://localhost:8000";
    public String getHtmlContent() {
        return restTemplate.getForObject(MEETING_SERVICE_URL, String.class);
    }
}
