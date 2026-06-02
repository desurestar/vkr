package ru.zagrebin.server.profile;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
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
    public ApiModels.User updateProfile(
            @RequestBody UpdateProfileRequest req,
            HttpSession session) {

        var u = db.getUserEntity(requireUid(session));

        u.setDisplayName(req.displayName());
        u.setBio(req.bio());
        u.setAvatarUrl(req.avatarUrl());

        db.saveUser(u);

        return db.getUser(u.getId());
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
    public ApiModels.PublicProfile publicProfile(@PathVariable Long userId, HttpSession session) {
        var user = db.getUser(userId);
        var viewerId = (Long) session.getAttribute("uid");
        var isFollowing = viewerId != null && db.isFollowing(viewerId, userId);

        return new ApiModels.PublicProfile(user, isFollowing, db.postsByAuthor(userId));
    }

    @PostMapping("/{userId}/follow")
    public ApiModels.PublicProfile follow(@PathVariable Long userId,
                                           HttpSession session) {

        db.followUser(requireUid(session), userId);

        return publicProfile(userId, session);
    }

    @DeleteMapping("/{userId}/follow")
    public ApiModels.PublicProfile unfollow(@PathVariable Long userId,
                                             HttpSession session) {

        db.unfollowUser(requireUid(session), userId);

        return publicProfile(userId, session);
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
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return uid;
    }
}
