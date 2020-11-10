package top.siyile.facusapi.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data public class User {
    @Id
    public String id;

    public String email;
    public String password;

    public String firstName;
    public String lastName;

    public String subject;
    public String studyYear;
    public String country;

    public User() {}

    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.subject = "Computer Science";
        this.studyYear = "Junior";
        this.country = "United States";
    }

    public void setValueFromForm(UserForm userForm) {
        this.firstName = userForm.getFirstName();
        this.lastName = userForm.getLastName();
        if (!userForm.getSubject().isBlank()) {
            this.subject = userForm.getSubject();
        }
        if (!userForm.getStudyYear().isBlank()) {
            this.studyYear = userForm.getStudyYear();
        }
        if (!userForm.getCountry().isBlank()) {
            this.country = userForm.getCountry();
        }
    }

    public User userWithoutPassword() {
        User userWithoutPassword = new User(email, null);
        userWithoutPassword.id = id;
        userWithoutPassword.firstName = firstName;
        userWithoutPassword.lastName = lastName;
        userWithoutPassword.subject = subject;
        userWithoutPassword.studyYear = studyYear;
        userWithoutPassword.country = country;
        return userWithoutPassword;
    }

    @Override
    public String toString() {
        return String.format("User[id=%s, userName='%s', firstName='%s', lastName='%s']",
                id, email, firstName, lastName);
    }
}
