package top.siyile.facusapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import top.siyile.facusapi.model.User;
import top.siyile.facusapi.model.UserForm;
import top.siyile.facusapi.repository.UserRepository;

import javax.servlet.http.HttpSession;

@RestController
public class UserController {

    private static final String DEMO_URL = "";

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> userLogin(@RequestParam("email") String email,
                                       @RequestParam("password") String password,
                                       HttpSession session) {
        User foundUser = repository.findByEmail(email);
        if(foundUser != null) {
            if(passwordEncoder.matches(password, foundUser.password)) {
                session.setAttribute("email", email);
            } else {
                return ResponseEntity.badRequest().body("Incorrect password");
            }
        } else {
            return ResponseEntity.badRequest().body("Incorrect email");
        }
        return ResponseEntity.ok(foundUser.userWithoutPassword());
    }

    @PostMapping("/register")
    public ResponseEntity<?> userRegister(@RequestBody UserForm userForm,
                                          HttpSession session) {
        User foundUser = repository.findByEmail(userForm.getEmail());
        User newUser;
        if(foundUser != null) {
            return ResponseEntity.badRequest().body("Email already exists");
        } else {
            newUser = new User(userForm.getEmail(), passwordEncoder.encode(userForm.getPassword()));
            newUser.setValueFromForm(userForm);
            repository.save(newUser);
            session.setAttribute("email", userForm.getEmail());
        }
        return ResponseEntity.ok(newUser.userWithoutPassword());
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session)
    {
        session.removeAttribute("email");
        session.invalidate();
        return ResponseEntity.ok("Logout succeeds");
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserForm(HttpSession session) {
        User loggedUser = getUserFromSession(session);
        if(loggedUser == null) {
            return ResponseEntity.badRequest().body("User not logged in yet");
        } else {
            UserForm loggedUserForm = new UserForm();
            loggedUserForm.setFirstName(loggedUser.firstName);
            loggedUserForm.setLastName(loggedUser.lastName);
            loggedUserForm.setSubject(loggedUser.subject);
            loggedUserForm.setStudyYear(loggedUser.studyYear);

            return ResponseEntity.ok(loggedUserForm);
        }
    }

    @PostMapping("/user")
    public ResponseEntity<String> updateUser(@RequestBody UserForm userForm,
                                             HttpSession session) {
        User loggedUser = getUserFromSession(session);
        if(loggedUser == null) {
            return ResponseEntity.badRequest().body("Not logged in yet");
        } else {
            loggedUser.firstName = userForm.getFirstName();
            loggedUser.lastName = userForm.getLastName();
            loggedUser.subject = userForm.getSubject();
            loggedUser.studyYear = userForm.getStudyYear();
            repository.save(loggedUser);
        }
        return ResponseEntity.ok("Update user info succeeds");
    }

    @PostMapping("/matching")
    public ResponseEntity<String> matching(@RequestParam("goal") String goal,
                                          HttpSession session) {
        User loggedUser = getUserFromSession(session);
        if(loggedUser == null) {
            return ResponseEntity.badRequest().body("Not logged in yet");
        } else {
            repository.save(loggedUser);
            return ResponseEntity.ok(DEMO_URL);
        }
    }

    public User getUserFromSession(HttpSession session) {
        String loggedUserEmail = (String) session.getAttribute("email");
        if(loggedUserEmail == null) {
            return null;
        } else {
            return repository.findByEmail(loggedUserEmail);
        }
    }
}
