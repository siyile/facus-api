package top.siyile.facusapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.siyile.facusapi.model.*;
import top.siyile.facusapi.repository.SessionRepository;
import top.siyile.facusapi.repository.UserRepository;

import javax.servlet.http.HttpSession;
import java.time.Instant;
import java.util.*;


@RestController
public class SessionController {

    private static final List<String> ALL_STATUS = Arrays.asList("created", "matched", "started", "expired", "cancelled");
    private static final String CANDIDATE_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final String URL_PREFIX = "https://facus.us/";

    @Autowired
    private SessionRepository repository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/session")
    public ResponseEntity<?> getSessions() {
        List<Session> allSessions = repository.findAll();
        return ResponseEntity.ok(getSessionsWithUserInfo(allSessions));
    }

    @GetMapping("/session/{sid}")
    public ResponseEntity<?> getSessionBySid(@PathVariable("sid") String sid) {
        Optional<Session> session = repository.findById(sid);
        if(session.isEmpty()) {
            return ResponseEntity.badRequest().body("session not found");
        }
        return ResponseEntity.ok(getSessionWithUserInfo(session.get()));
    }

    @PostMapping("/session")
    public ResponseEntity<?> updateSession(@RequestBody SessionForm sessionForm, HttpSession httpSession) {
        User loggedUser = getUserFromSession(httpSession);
        String uid;
        if(loggedUser == null) {
            return ResponseEntity.badRequest().body("Not logged in yet");
        } else {
            uid = loggedUser.id;
        }
        String operation = sessionForm.getOperation();
        if(operation == null) {
            return ResponseEntity.badRequest().body("wrong operation");
        }
        if(operation.equalsIgnoreCase("create")) {
            // create session with a random generated RL.
            if (validateSession(sessionForm, uid) > 0) {
                return ResponseEntity.badRequest().body("time conflict");
            } else {
                Session newSession = new Session();
                newSession.initFromForm(sessionForm, uid);
                repository.save(newSession);
                return ResponseEntity.ok(getSessionWithUserInfo(newSession));
            }
        } else if(operation.equalsIgnoreCase("update") ||
                operation.equalsIgnoreCase("cancel")) {
            String sid = sessionForm.getSid();
            Optional<Session> session = repository.findById(sid);
            if (session.isEmpty()) {
                return ResponseEntity.badRequest().body("session does not exist");
            }
            if (operation.equalsIgnoreCase("update") && (validateSession(sessionForm, uid)) > 0) {
                return ResponseEntity.badRequest().body("time conflict");
            }
            session.get().updateFromForm(sessionForm);
            repository.save(session.get());
            return ResponseEntity.ok(getSessionWithUserInfo(session.get()));
        } else {
            return ResponseEntity.badRequest().body("wrong operation");
        }
    }

    @PostMapping("/session/join")
    public ResponseEntity<?> joinSession(@RequestParam String sid, HttpSession httpSession) {
        Optional<Session> session = repository.findById(sid);
        if(session.isEmpty()) {
            return ResponseEntity.badRequest().body("session not found");
        }
        User loggedUser = getUserFromSession(httpSession);
        if(loggedUser == null) {
            return ResponseEntity.badRequest().body("Not logged in yet");
        } else {
            if(!session.get().getStatus().equalsIgnoreCase("created")) {
                return ResponseEntity.badRequest().body("Cannot join this session");
            }
            session.get().setUid2(loggedUser.id);
            session.get().setMatchedTime(Instant.now().getEpochSecond());
            session.get().setStatus("matched");
            String url = generateUrl();
            session.get().setUrl(url);
            repository.save(session.get());
            return ResponseEntity.ok(getSessionWithUserInfo(session.get()));
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

        return ResponseEntity.ok(getSessionsWithUserInfo(sessions));
    }

    @GetMapping("/session/filterByTags")
    public ResponseEntity<?> getSessionFilterByTags(@RequestParam String[] tags) {
        List<Session> sessions = new ArrayList<>();
        for(String tag : tags) {
            sessions.addAll(repository.findByTagIgnoreCase(tag));
        }
        return ResponseEntity.ok(getSessionsWithUserInfo(sessions));
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
        List<Session> sessions = repository.findByUid1OrUid2(uid, uid);
        return ResponseEntity.ok(getSessionsWithUserInfo(sessions));
    }

    @GetMapping("/session/user")
    public ResponseEntity<?> getSessionFilterWithCookie(HttpSession httpSession) {
        User loggedUser = getUserFromSession(httpSession);
        if(loggedUser == null) {
            return ResponseEntity.badRequest().body("Not logged in yet");
        } else {
            String uid = loggedUser.id;
            List<Session> sessions = repository.findByUid1OrUid2(uid, uid);
            return ResponseEntity.ok(getSessionsWithUserInfo(sessions));
        }
    }

    @GetMapping("/session/user/filterByStatus")
    public ResponseEntity<?> getSessionFilterByUidAndStatus( @RequestParam String[] status, HttpSession httpSession) {
        User loggedUser = getUserFromSession(httpSession);
        if(loggedUser == null) {
            return ResponseEntity.badRequest().body("Not logged in yet");
        } else {
            String uid = loggedUser.id;
            List<Session> sessions = new ArrayList<>();
            for (String stat : status) {
                if (!ALL_STATUS.contains(stat)) {
                    return ResponseEntity.badRequest().body("wrong status");
                }
                sessions.addAll(repository.findByUid1OrUid2AndStatus(uid, uid, stat));
            }
            return ResponseEntity.ok(getSessionsWithUserInfo(sessions));
        }
    }

    @PostMapping("/session/match")
    public ResponseEntity<?> match(@RequestBody MatchForm tag,
                                           HttpSession httpSession) {
        User loggedUser = getUserFromSession(httpSession);
        if(loggedUser == null) {
            return ResponseEntity.badRequest().body("Not logged in yet");
        } else {
            String uid = loggedUser.id;
            List<Session> candidateSessions = repository.findByStatusOrderByStartTime("created");
            if(candidateSessions.isEmpty()) {
                // cannot find unmatched sessions, generate a new session with random URL.
                Session newSession = new Session(uid, tag.getTag());
                repository.save(newSession);
                return ResponseEntity.ok(newSession);
            } else {
                // generate a randomized url for the new session
                String url = generateUrl();
                if (tag != null) {
                    for (Session session : candidateSessions) {
                        // find a session with the specified tag
                        if (session.getTag().equalsIgnoreCase(tag.getTag())) {
                            session.match(uid, url);
                            repository.save(session);
                            return ResponseEntity.ok(getSessionWithUserInfo(session));
                        }
                    }
                }
                // cannot find desired session, choose the session with the nearest startTime
                Session session = candidateSessions.get(0);
                session.match(uid, url);
                repository.save(session);
                return ResponseEntity.ok(getSessionWithUserInfo(session));
            }
        }
    }

    @PostMapping("/session/matchWithUid")
    public ResponseEntity<?> matchWithUid(@RequestBody MatchForm tag,
                                    @RequestParam String uid) {
        List<Session> candidateSessions = repository.findByStatusOrderByStartTime("created");
        if(candidateSessions.isEmpty()) {
            // cannot find unmatched sessions, generate a new session with random URL.
            Session newSession = new Session(uid, tag.getTag());
            repository.save(newSession);
            return ResponseEntity.ok(newSession);
        } else {
            // generate a randomized url for the new session
            String url = generateUrl();
            if(tag != null) {
                for(Session session : candidateSessions) {
                    // find a session with the specified tag
                    if(session.getTag().equalsIgnoreCase(tag.getTag())) {
                        session.match(uid, url);
                        repository.save(session);
                        return ResponseEntity.ok(session);
                    }
                }
            }
            // cannot find desired session, choose the session with the nearest startTime
            Session session = candidateSessions.get(0);
            session.match(uid, url);
            repository.save(session);
            return ResponseEntity.ok(session);
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

    public String generateUrl() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 12; i++) {
            sb.append(CANDIDATE_CHARS.charAt(random.nextInt(CANDIDATE_CHARS
                    .length())));
        }
        String url = URL_PREFIX + sb.toString();
        return url;
    }

    public List<Session> getSessionsWithUserInfo(List<Session> sessions) {
        List<Session> sessionsWithUserInfo = new ArrayList<>();
        for(Session session : sessions) {
            sessionsWithUserInfo.add(getSessionWithUserInfo(session));
        }
        return sessionsWithUserInfo;
    }

    public Session getSessionWithUserInfo(Session session) {
        String uid1 = session.getUid1();
        User user1, user2;
        Optional<User> user1_ = userRepository.findById(uid1);
        if(!user1_.isEmpty()) {
            user1 = user1_.get().userWithoutPassword();
        } else {
            user1 = null;
        }

        String uid2 = session.getUid2();
        Optional<User> user2_ = userRepository.findById(uid2);
        if(!user2_.isEmpty()) {
            user2 = user2_.get().userWithoutPassword();
        } else {
            user2 = null;
        }

        return session.sessionWithUserInfo(user1, user2);
    }

    public int validateSession(SessionForm sessionForm, String uid) {
        String attendant = uid;
        Long endTime = sessionForm.startTime + sessionForm.duration * 60;
        List<Session> mySessions = repository.findByUid1OrUid2(attendant, attendant);
        for(Session session : mySessions) {
            if(session.startTime < endTime &&
                    session.endTime > sessionForm.startTime) {
                return 1;
            }
        }
        return 0;
    }
}