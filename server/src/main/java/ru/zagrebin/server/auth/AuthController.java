package ru.zagrebin.server.auth;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.zagrebin.server.common.ApiModels;
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

    public record RegisterRequest(@NotBlank String username,
                                  @Email String email,
                                  @NotBlank String password) {}

    public record LoginRequest(@NotBlank String email,
                               @NotBlank String password) {}

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest req,
                                        HttpSession session) {

        if (db.emailExists(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        if (db.usernameExists(req.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already registered");
        }
        if (req.password().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
        }

        var u = new UserEntity();
        u.setUsername(req.username());
        u.setEmail(req.email());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setDisplayName(req.username());
        u.setBio("");

        u = db.saveUser(u);
        session.setAttribute("uid", u.getId());

        return Map.of(
                "id", u.getId(),
                "registeredAt", Instant.now().toString()
        );
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest req,
                                     HttpSession session) {

        var user = db.findByEmailOrUsername(req.email());

        if (!encoder.matches(req.password(), user.getPasswordHash())) {
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
