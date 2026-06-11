package ru.zagrebin.server.auth;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.zagrebin.server.common.ApiModels;
import ru.zagrebin.server.common.ServerValidation;
import ru.zagrebin.server.data.DbService;
import ru.zagrebin.server.data.entity.UserEntity;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final DbService db;
    private final BCryptPasswordEncoder encoder;

    public AuthController(DbService db, BCryptPasswordEncoder encoder) {
        this.db = db;
        this.encoder = encoder;
    }

    public record RegisterRequest(String username, String email, String password) {}

    public record LoginRequest(String email, String password) {}

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest req,
                                        HttpSession session) {

        req = ServerValidation.requireBody(req);
        var username = ServerValidation.username(req.username());
        var email = ServerValidation.email(req.email());
        var password = ServerValidation.password(req.password(), "Password");

        if (db.emailExists(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        if (db.usernameExists(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already registered");
        }
        var u = new UserEntity();
        u.setUsername(username);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(password));
        u.setDisplayName(username);
        u.setBio("");

        u = db.saveUser(u);
        session.setAttribute("uid", u.getId());

        return Map.of(
                "id", u.getId(),
                "registeredAt", Instant.now().toString()
        );
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest req,
                                     HttpSession session) {

        req = ServerValidation.requireBody(req);
        var login = ServerValidation.requiredText(req.email(), "Login", 254).toLowerCase();
        var password = ServerValidation.password(req.password(), "Password");
        var user = db.findByEmailOrUsername(login);

        if (!encoder.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        session.setAttribute("uid", user.getId());

        return Map.of(
                "id", user.getId(),
                "displayName", user.getDisplayName()
        );
    }

    @PostMapping("/logout")
    public void logout(HttpSession session) {
        session.invalidate();
    }

    @GetMapping("/me")
    public ApiModels.User me(HttpSession session) {
        return db.getUser(requireUid(session));
    }

    private Long requireUid(HttpSession session) {
        var uid = (Long) session.getAttribute("uid");
        if (uid == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return uid;
    }
}
