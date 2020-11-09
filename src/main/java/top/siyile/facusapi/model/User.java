package top.siyile.facusapi.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data public class User {
    @Id
    public String id;

    public String userName;
    public String password;

    public String firstName;
    public String lastName;
    public String country;
    public String timeZone;
    public String subject;
    public String studyYear;

    public String goal;

    public User() {}

    public User(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    @Override
    public String toString() {
        return String.format("User[id=%s, userName='%s', firstName='%s', lastName='%s']",
                id, userName, firstName, lastName);
    }
}
