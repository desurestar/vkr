package ru.zagrebin.server.auth;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import ru.zagrebin.server.common.InMemoryStore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final InMemoryStore store;
    public AuthController(InMemoryStore store) { this.store = store; }

    public record RegisterRequest(@NotBlank String username, @Email String email, @NotBlank String password) {}
    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest req, HttpSession session) {
        var id = store.userSeq.incrementAndGet();
        var user = new InMemoryStore.User(id, req.username(), req.email(), store.encoder.encode(req.password()), req.username(), "", null,
                new HashSet<>(), new HashSet<>(), new ArrayList<>());
        store.users.put(id, user);
        session.setAttribute("uid", id);
        return Map.of("id", id, "registeredAt", Instant.now().toString());
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest req, HttpSession session) {
        var user = store.users.values().stream().filter(u -> u.email().equalsIgnoreCase(req.email())).findFirst().orElseThrow();
        if (!store.encoder.matches(req.password(), user.passwordHash())) throw new IllegalArgumentException("Invalid credentials");
        session.setAttribute("uid", user.id());
        return Map.of("id", user.id(), "displayName", user.displayName());
    }

    @PostMapping("/logout")
    public void logout(HttpSession session) { session.invalidate(); }

    @GetMapping("/me")
    public InMemoryStore.User me(HttpSession session) { return store.users.get(requireUid(session)); }

    private Long requireUid(HttpSession session) {
        var uid = (Long) session.getAttribute("uid");
        if (uid == null) throw new IllegalStateException("Unauthorized");
        return uid;
    }
}
