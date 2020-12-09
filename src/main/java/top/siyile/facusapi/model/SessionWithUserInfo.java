package top.siyile.facusapi.model;

public class SessionWithUserInfo {
    public String sid;

    public String url;
    public String tag;
    public String status;

    public String uid1;
    public String uid2;
    public User user1;
    public User user2;

    public Long createdTime;
    public Long matchedTime;
    public Long startTime;
    public Long endTime;

    public int duration; // in minutes
}