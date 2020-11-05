package top.siyile.facusapi.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserForm {
    private String firstName;
    private String lastName;
    private String country;
    private String timeZone;
    private String subject;
    private String studyYear;
}
