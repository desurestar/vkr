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
    public ApiModels.PublicProfile publicProfile(
            @PathVariable Long userId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            HttpSession session
    ) {
        var user = db.getUser(userId);
        var viewerId = (Long) session.getAttribute("uid");
        var isFollowing = viewerId != null && db.isFollowing(viewerId, userId);

        return new ApiModels.PublicProfile(user, isFollowing, db.postsByAuthor(userId, viewerId, q, page, size));
    }

    @PostMapping("/{userId}/follow")
    public ApiModels.PublicProfile follow(@PathVariable Long userId,
                                           HttpSession session) {

        db.followUser(requireUid(session), userId);

        return publicProfile(userId, null, null, null, session);
    }

    @DeleteMapping("/{userId}/follow")
    public ApiModels.PublicProfile unfollow(@PathVariable Long userId,
                                             HttpSession session) {

        db.unfollowUser(requireUid(session), userId);

        return publicProfile(userId, null, null, null, session);
    }

    @GetMapping("/shopping-list")
    public List<ApiModels.ShoppingList> shopping(HttpSession session) {
        return db.shoppingLists(requireUid(session));
    }

    @PostMapping("/shopping-list")
    public ApiModels.ShoppingList createShoppingList(@RequestBody Map<String, String> req,
                                                     HttpSession session) {
        return db.toShoppingList(db.createShoppingList(requireUid(session), req.get("name")));
    }

    @PatchMapping("/shopping-list/{listId}")
    public ApiModels.ShoppingList updateShoppingList(@PathVariable Long listId,
                                                     @RequestBody Map<String, String> req,
                                                     HttpSession session) {
        return db.toShoppingList(db.updateShoppingList(requireUid(session), listId, req.get("name")));
    }

    @DeleteMapping("/shopping-list/{listId}")
    public Map<String, String> deleteShoppingList(@PathVariable Long listId,
                                                  HttpSession session) {
        db.deleteShoppingList(requireUid(session), listId);
        return Map.of("status", "deleted");
    }

    @PostMapping("/shopping-list/{listId}/items")
    public ApiModels.ShoppingItem addShoppingItem(@PathVariable Long listId,
                                                  @RequestBody ApiModels.ShoppingItemRequest req,
                                                  HttpSession session) {
        return db.toShopping(db.addShopping(requireUid(session), listId, req.name(), req.amount()));
    }

    @PatchMapping("/shopping-list/items/{itemId}")
    public ApiModels.ShoppingItem updateShoppingItem(@PathVariable Long itemId,
                                                     @RequestBody ApiModels.ShoppingItemRequest req,
                                                     HttpSession session) {
        return db.toShopping(db.updateShoppingItem(requireUid(session), itemId, req.name(), req.amount(), req.checked()));
    }

    @DeleteMapping("/shopping-list/items/{itemId}")
    public Map<String, String> deleteShoppingItem(@PathVariable Long itemId,
                                                  HttpSession session) {
        db.deleteShoppingItem(requireUid(session), itemId);
        return Map.of("status", "deleted");
    }

    private Long requireUid(HttpSession session) {
        var uid = (Long) session.getAttribute("uid");
        if (uid == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return uid;
    }
}
