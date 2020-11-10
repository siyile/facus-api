package top.siyile.facusapi.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserForm {
    private String email;
    private String password;

    private String firstName;
    private String lastName;
    private String subject;
    private String studyYear;
}
