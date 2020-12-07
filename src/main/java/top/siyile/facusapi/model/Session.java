package top.siyile.facusapi.model;


import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.Random;

@Data public class Session {
    @Id
    public String id;

    public String url;
    public String tag;
    public String status;

    public String firstAttendant;
    public String secondAttendant;

    public Long createdTime;
    public Long matchedTime;
    public Long startTime;
    public Long endTime;

    public int duration; // in minutes
    private static final String CANDIDATE_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";

    public Session() {}

    public Session(String uid, String tag) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 12; i++) {
            sb.append(CANDIDATE_CHARS.charAt(random.nextInt(CANDIDATE_CHARS
                    .length())));
        }
        this.url = sb.toString();

        this.firstAttendant = uid;
        if (tag != null && !tag.isBlank()) {
            this.tag = tag;
        }
        this.status = "created";
        this.createdTime = Instant.now().getEpochSecond();
        this.startTime = Instant.now().getEpochSecond();
        this.duration = 60; // duration = 60min by default
        this.endTime = startTime + 60 * duration;
    }

    public void matching(String uid) {
        this.secondAttendant = uid;
        this.matchedTime = Instant.now().getEpochSecond();
        this.status = "matched";
    }

    public void updateFromForm(SessionForm sessionForm) {
        if(sessionForm.operation.equals("cancel")) {
            this.status = "cancelled";
            return;
        }

        if (!sessionForm.getTag().isBlank()) {
            this.tag = sessionForm.getTag();
        }

        if (sessionForm.getStartTime() > 0) {
            this.startTime = sessionForm.getStartTime();
        }

        if (sessionForm.getDuration() > 0) {
            this.duration = sessionForm.getDuration();
        }

        this.endTime = startTime + 60 * duration;
    }

    public void initFromForm(SessionForm sessionForm) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 12; i++) {
            sb.append(CANDIDATE_CHARS.charAt(random.nextInt(CANDIDATE_CHARS
                    .length())));
        }
        this.url = sb.toString();
        this.firstAttendant = sessionForm.getFirstAttendant();
        if (!sessionForm.getTag().isBlank()) {
            this.tag = sessionForm.getTag();
        }
        this.startTime = sessionForm.getStartTime();
        this.duration = sessionForm.getDuration();
        this.status = "created";
        this.createdTime = Instant.now().getEpochSecond();

        this.endTime = startTime + 60 * duration;
    }

    @Override
    public String toString() {
        return String.format("Session[id=%s, tag='%s', firstAttendant='%s', " +
                        "secondAttendant='%s', status='%s', duration=%s, " +
                        "createdTime=%d, matchedTime=%d, startTime=%d, endTime=%d]",
                id, tag, firstAttendant,
                secondAttendant, status, duration,
                createdTime, matchedTime, startTime, endTime);
    }
}