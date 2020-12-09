package top.siyile.facusapi.model;


import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.Instant;

@Data public class Session {
    @Id
    public String sid;

    public String url;
    public String tag;
    public String status;

    public String uid1;
    public String uid2;

    public Long createdTime;
    public Long matchedTime;
    public Long startTime;
    public Long endTime;

    public int duration; // in minutes


    public Session() {}

    public Session(String uid, String tag) {
        this.uid1 = uid;
        if (tag != null && !tag.isBlank()) {
            this.tag = tag;
        }
        this.status = "created";
        this.createdTime = Instant.now().getEpochSecond();
        this.startTime = Instant.now().getEpochSecond();
        this.duration = 60; // duration = 60min by default
        this.endTime = startTime + 60 * duration;
    }

    public SessionWithUserInfo sessionWithUserInfo(User user1, User user2) {
        SessionWithUserInfo sessionWithUserInfo = new SessionWithUserInfo();
        sessionWithUserInfo.sid = sid;
        sessionWithUserInfo.url = url;
        sessionWithUserInfo.tag = tag;
        sessionWithUserInfo.uid1 = uid1;
        sessionWithUserInfo.uid2 = uid2;
        sessionWithUserInfo.status = status;
        sessionWithUserInfo.createdTime = createdTime;
        sessionWithUserInfo.matchedTime = matchedTime;
        sessionWithUserInfo.startTime = startTime;
        sessionWithUserInfo.endTime = endTime;
        sessionWithUserInfo.duration = duration;
        sessionWithUserInfo.user1 = user1;
        sessionWithUserInfo.user2 = user2;
        return sessionWithUserInfo;
    }

    public void match(String uid, String url) {
        this.uid2 = uid;
        this.url = url;
        this.matchedTime = Instant.now().getEpochSecond();
        this.setStartTime(this.matchedTime);
        this.setEndTime(this.startTime + this.duration * 60);
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

    public void initFromForm(SessionForm sessionForm, String uid1) {
        this.uid1 = uid1;
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
        return String.format("Session[sid=%s, tag='%s', uid1='%s', " +
                        "uid2='%s', status='%s', duration=%s, " +
                        "createdTime=%d, matchedTime=%d, startTime=%d, endTime=%d]",
                sid, tag, uid1,
                uid2, status, duration,
                createdTime, matchedTime, startTime, endTime);
    }
}