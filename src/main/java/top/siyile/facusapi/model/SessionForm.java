package top.siyile.facusapi.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionForm {
    public String operation;
    public String sid;
    public String tag;
    public String firstAttendant;
    public Long startTime;
    public int duration;
}
