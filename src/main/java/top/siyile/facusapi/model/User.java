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

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public void setValueFromForm(UserForm userForm) {
        this.firstName = userForm.getFirstName();
        this.lastName = userForm.getLastName();
        this.subject = userForm.getSubject();
        this.studyYear = userForm.getStudyYear();
    }

    public User userWithoutPassword() {
        User userWithoutPassword = new User(email, null);
        userWithoutPassword.firstName = firstName;
        userWithoutPassword.lastName = lastName;
        userWithoutPassword.subject = subject;
        userWithoutPassword.studyYear = studyYear;
        return userWithoutPassword;
    }

    @Override
    public String toString() {
        return String.format("User[id=%s, userName='%s', firstName='%s', lastName='%s']",
                id, email, firstName, lastName);
    }
}
