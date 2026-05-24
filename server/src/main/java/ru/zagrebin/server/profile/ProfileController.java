package ru.zagrebin.server.profile;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.zagrebin.server.common.ApiModels;
import ru.zagrebin.server.data.DbService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final DbService db;
    private final BCryptPasswordEncoder encoder;

    public ProfileController(DbService db, BCryptPasswordEncoder encoder) {
        this.db = db;
        this.encoder = encoder;
    }

    public record UpdateProfileRequest(String displayName,
                                       String bio,
                                       String avatarUrl) {}

    public record UpdatePasswordRequest(String oldPassword,
                                        String newPassword) {}

    @PatchMapping
    public ApiModels.User updateProfile(@RequestBody UpdateProfileRequest req,
                                        HttpSession session) {

        var u = db.getUserEntity(requireUid(session));

        u.setDisplayName(req.displayName());
        u.setBio(req.bio());
        u.setAvatarUrl(req.avatarUrl());

        return db.toUser(db.saveUser(u));
    }

    @PatchMapping("/password")
    public Map<String, String> updatePassword(@RequestBody UpdatePasswordRequest req,
                                              HttpSession session) {

        var u = db.getUserEntity(requireUid(session));

        if (!encoder.matches(req.oldPassword(), u.getPasswordHash())) {
            throw new IllegalArgumentException("wrong old password");
        }

        u.setPasswordHash(encoder.encode(req.newPassword()));
        db.saveUser(u);

        return Map.of("status", "ok");
    }

    @GetMapping("/{userId}")
    public ApiModels.User publicProfile(@PathVariable Long userId) {
        return db.getUser(userId);
    }

    @PostMapping("/{userId}/follow")
    public Map<String, String> follow(@PathVariable Long userId,
                                      HttpSession session) {

        var me = db.getUserEntity(requireUid(session));
        var target = db.getUserEntity(userId);

        me.getFollowing().add(target);
        db.saveUser(me);

        return Map.of("status", "followed");
    }

    @DeleteMapping("/{userId}/follow")
    public Map<String, String> unfollow(@PathVariable Long userId,
                                        HttpSession session) {

        var me = db.getUserEntity(requireUid(session));

        me.getFollowing().removeIf(u -> u.getId().equals(userId));
        db.saveUser(me);

        return Map.of("status", "unfollowed");
    }

    @GetMapping("/shopping-list")
    public List<ApiModels.ShoppingItem> shopping(HttpSession session) {
        return db.shopping(requireUid(session));
    }

    @PostMapping("/shopping-list")
    public ApiModels.ShoppingItem addShopping(@RequestBody Map<String, String> req,
                                              HttpSession session) {

        return db.toShopping(
                db.addShopping(
                        requireUid(session),
                        req.get("name"),
                        req.getOrDefault("amount", "1")
                )
        );
    }

    private Long requireUid(HttpSession session) {
        var uid = (Long) session.getAttribute("uid");
        if (uid == null) {
            throw new IllegalStateException("Unauthorized");
        }
        return uid;
    }
}
