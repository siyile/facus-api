package top.siyile.facusapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.siyile.facusapi.model.*;
import top.siyile.facusapi.repository.SessionRepository;
import top.siyile.facusapi.repository.UserRepository;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@RestController
public class SessionController {

    private static final List<String> ALL_STATUS = Arrays.asList("created", "matched", "started", "expired", "cancelled");

    @Autowired
    private SessionRepository repository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/session")
    public ResponseEntity<?> getSessions() {
        List<Session> allSessions = repository.findAll();
        return ResponseEntity.ok(allSessions);
    }

    @PostMapping("/session")
    public ResponseEntity<?> updateSession(@RequestBody SessionForm sessionForm) {
        String operation = sessionForm.getOperation();
        if(operation == null) {
            return ResponseEntity.badRequest().body("wrong operation");
        }
        switch (operation){
            case "create":
                // create session with a random generated RL.
                if(validateSession(sessionForm) > 0) {
                    return ResponseEntity.badRequest().body("time conflict");
                } else {
                    Session newSession = new Session();
                    newSession.initFromForm(sessionForm);
                    repository.save(newSession);
                    return ResponseEntity.ok(newSession);
                }
            case "update":
            case "cancel":
                String sid = sessionForm.getSid();
                Optional<Session> session = repository.findById(sid);
                if(session.isEmpty()) {
                    return ResponseEntity.badRequest().body("session does not exist");
                }
                if(validateSession(sessionForm) > 0) {
                    return ResponseEntity.badRequest().body("time conflict");
                } else {
                    session.get().updateFromForm(sessionForm);
                    repository.save(session.get());
                    return ResponseEntity.ok(session.get());
                }
            default:
                return ResponseEntity.badRequest().body("wrong operation");
        }
    }

    @GetMapping("/session/filterByStatus")
    public ResponseEntity<?> getSessionFilterByStatus(@RequestParam String[] status) {
        List<Session> sessions = new ArrayList<>();
        for(String stat : status) {
            if(!ALL_STATUS.contains(stat)) {
                return ResponseEntity.badRequest().body("wrong status");
            }
            sessions.addAll(repository.findByStatusOrderByStartTime(stat));
        }
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/session/filterByTag")
    public ResponseEntity<?> getSessionFilterByTag(@RequestParam String tag) {
        if(tag.isBlank()) {
            return ResponseEntity.ok(new ArrayList<Session>());
        }
        List<Session> sessions = repository.findByTagIgnoreCase(tag);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/session/filterByTime")
    public ResponseEntity<?> getSessionFilterByTime(@RequestParam(required = false) Long fromTime,
                                                    @RequestParam(required = false) Long toTime) {
        if(fromTime == null && toTime == null) {
            return ResponseEntity.badRequest().body("parameters cannot be null");
        }
        if(fromTime != null && toTime != null) {
            List<Session> sessions = repository.findByStartTimeGreaterThanEqualAndEndTimeLessThanEqual(fromTime, toTime);
            return ResponseEntity.ok(sessions);
        }
        if (fromTime == null && toTime != null) {
            List<Session> sessions = repository.findByEndTimeLessThanEqual(toTime);
            return ResponseEntity.ok(sessions);
        } else {
            List<Session> sessions = repository.findByStartTimeGreaterThanEqual(fromTime);
            return ResponseEntity.ok(sessions);
        }
    }

    @GetMapping("/session/filterByDuration")
    public ResponseEntity<?> getSessionFilterByDuration(@RequestParam int minValue, @RequestParam int maxValue) {
        List<Session> sessions = repository.findByDurationBetween(minValue, maxValue);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/session/user/{uid}")
    public ResponseEntity<?> getSessionFilterByUid(@PathVariable("uid") String uid) {
        List<Session> sessions = repository.findByFirstAttendantOrSecondAttendant(uid, uid);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/session/user/filterByStatus")
    public ResponseEntity<?> getSessionFilterByUidAndStatus(@RequestParam String uid, @RequestParam String[] status) {
        List<Session> sessions = new ArrayList<>();
        for(String stat : status) {
            if(!ALL_STATUS.contains(stat)) {
                return ResponseEntity.badRequest().body("wrong status");
            }
            sessions.addAll(repository.findByFirstAttendantOrSecondAttendantAndStatus(uid, uid, stat));
        }
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/session/matching")
    public ResponseEntity<?> match(@RequestBody String tag,
                                           HttpSession httpSession) {
        User loggedUser = getUserFromSession(httpSession);
        if(loggedUser == null) {
            return ResponseEntity.badRequest().body("Not logged in yet");
        } else {
            String uid = loggedUser.id;
            List<Session> candidateSessions = repository.findByStatusOrderByStartTime("created");
            if(candidateSessions.isEmpty()) {
                // cannot find unmatched sessions, generate a new session with random URL.
                Session newSession = new Session(uid, tag);
                repository.save(newSession);
                return ResponseEntity.ok(newSession);
            } else {
                for(Session session : candidateSessions) {
                    // find a session with the specified tag
                    if(session.getTag().equalsIgnoreCase(tag)) {
                        session.matching(uid);
                        repository.save(session);
                        return ResponseEntity.ok(session);
                    }
                }
                // cannot find desired session, choose the session with the nearest startTime
                Session session = candidateSessions.get(0);
                session.matching(uid);
                repository.save(session);
                return ResponseEntity.ok(session);
            }
        }
    }

    public User getUserFromSession(HttpSession session) {
        String loggedUserEmail = (String) session.getAttribute("email");
        if(loggedUserEmail == null) {
            return null;
        } else {
            return userRepository.findByEmail(loggedUserEmail);
        }
    }

    public int validateSession(SessionForm sessionForm) {
        String attendant = sessionForm.getFirstAttendant();
        Long endTime = sessionForm.startTime + sessionForm.duration * 60;
        List<Session> mySessions = repository.findByFirstAttendantOrSecondAttendant(attendant, attendant);
        for(Session session : mySessions) {
            if(session.startTime < endTime &&
                    session.endTime > sessionForm.startTime) {
                return 1;
            }
        }
        return 0;
    }
}