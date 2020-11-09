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
    public ResponseEntity<String> userLogin(@RequestParam("userName") String userName,
                                            @RequestParam("password") String password,
                                            HttpSession session) {
        User foundUser = repository.findByUserName(userName);
        if(foundUser != null) {
            if(passwordEncoder.matches(password, foundUser.password)) {
                session.setAttribute("username", userName);
            } else {
                return ResponseEntity.badRequest().body("Incorrect password");
            }
        } else {
            return ResponseEntity.badRequest().body("Incorrect user name");
        }
        return ResponseEntity.ok("Login succeeds");
    }

    @PostMapping("/register")
    public ResponseEntity<String> userRegister(@RequestParam("userName") String userName,
                                               @RequestParam("password") String password,
                                               @RequestParam("passwordConfirmation") String passwordConfirmation,
                                               HttpSession session) {
        if(!password.equals(passwordConfirmation)) {
            return ResponseEntity.badRequest().body("Password confirmation isn't correct");
        }

        User foundUser = repository.findByUserName(userName);
        if(foundUser != null) {
            return ResponseEntity.badRequest().body("User name already exists");
        } else {
            User newUser = new User(userName, passwordEncoder.encode(password));
            repository.save(newUser);
            session.setAttribute("username", userName);
        }
        return ResponseEntity.ok("Register succeeds");
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session)
    {
        session.removeAttribute("username");
        session.invalidate();
        return ResponseEntity.ok("Logout succeeds");
    }

    @GetMapping("/user")
    public ResponseEntity<UserForm> getUserForm(HttpSession session) {
        User loggedUser = getUserFromSession(session);
        if(loggedUser == null) {
            return ResponseEntity.badRequest().body(null);
        } else {
            UserForm loggedUserForm = new UserForm();
            loggedUserForm.setFirstName(loggedUser.firstName);
            loggedUserForm.setLastName(loggedUser.lastName);
            loggedUserForm.setCountry(loggedUser.country);
            loggedUserForm.setTimeZone(loggedUser.timeZone);
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
            loggedUser.country = userForm.getCountry();
            loggedUser.timeZone = userForm.getTimeZone();
            loggedUser.subject = userForm.getSubject();
            loggedUser.studyYear = userForm.getStudyYear();
            repository.save(loggedUser);
        }
        return ResponseEntity.ok("Update user info succeeds");
    }

    @GetMapping("/goal")
    public ResponseEntity<String> getGoal(HttpSession session) {
        User loggedUser = getUserFromSession(session);
        if(loggedUser == null) {
            return ResponseEntity.badRequest().body("Not logged in yet");
        } else {
            return ResponseEntity.ok(loggedUser.goal);
        }
    }

    @PostMapping("/matching")
    public ResponseEntity<String> matching(@RequestParam("goal") String goal,
                                          HttpSession session) {
        User loggedUser = getUserFromSession(session);
        if(loggedUser == null) {
            return ResponseEntity.badRequest().body("Not logged in yet");
        } else {
            loggedUser.goal = goal;
            repository.save(loggedUser);
            return ResponseEntity.ok(DEMO_URL);
        }
    }

    public User getUserFromSession(HttpSession session) {
        String loggedUserName = (String) session.getAttribute("username");
        if(loggedUserName == null) {
            return null;
        } else {
            return repository.findByUserName(loggedUserName);
        }
    }
}
