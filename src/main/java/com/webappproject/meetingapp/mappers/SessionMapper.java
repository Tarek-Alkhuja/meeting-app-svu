package com.webappproject.meetingapp.mappers;

import com.webappproject.meetingapp.dto.SessionDto;
import com.webappproject.meetingapp.models.Session;
import java.util.stream.Collectors;
import static com.webappproject.meetingapp.mappers.RecordingMapper.mapToRecordingDto;

public class SessionMapper {
        public static Session mapToSession(SessionDto session) {
            Session sessionDto = Session.builder()
                    .id(session.getId())
                    .title(session.getTitle())
                    .startTime(session.getStartTime())
                    .endTime(session.getEndTime())
                    .createdBy(session.getCreatedBy())
                    .build();
            return  sessionDto;
        }

        public static SessionDto mapToSessionDto(Session session) {
            SessionDto sessionDto = SessionDto.builder()
                    .id(session.getId())
                    .title(session.getTitle())
                    .startTime(session.getStartTime())
                    .endTime(session.getEndTime())
                    .createdBy(session.getCreatedBy())
                    .recordings(session.getRecordings().stream().map((recording) ->
                            mapToRecordingDto(recording)).collect(Collectors.toList()))
                    .build();
            return sessionDto;
        }
}


