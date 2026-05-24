package ru.zagrebin.server.profile;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import ru.zagrebin.server.common.InMemoryStore;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {
    private final InMemoryStore store;
    public ProfileController(InMemoryStore store) { this.store = store; }

    public record UpdateProfileRequest(String displayName, String bio, String avatarUrl) {}
    public record UpdatePasswordRequest(String oldPassword, String newPassword) {}

    @PatchMapping
    public InMemoryStore.User updateProfile(@RequestBody UpdateProfileRequest req, HttpSession session) {
        var uid = requireUid(session); var u = store.users.get(uid);
        var updated = new InMemoryStore.User(u.id(), u.username(), u.email(), u.passwordHash(), req.displayName(), req.bio(), req.avatarUrl(), u.following(), u.followers(), u.shoppingList());
        store.users.put(uid, updated); return updated;
    }

    @PatchMapping("/password")
    public Map<String, String> updatePassword(@RequestBody UpdatePasswordRequest req, HttpSession session) {
        var uid = requireUid(session); var u = store.users.get(uid);
        if (!store.encoder.matches(req.oldPassword(), u.passwordHash())) throw new IllegalArgumentException("wrong old password");
        store.users.put(uid, new InMemoryStore.User(u.id(), u.username(), u.email(), store.encoder.encode(req.newPassword()), u.displayName(), u.bio(), u.avatarUrl(), u.following(), u.followers(), u.shoppingList()));
        return Map.of("status", "ok");
    }

    @GetMapping("/{userId}") public InMemoryStore.User publicProfile(@PathVariable Long userId) { return store.users.get(userId); }
    @PostMapping("/{userId}/follow") public Map<String, String> follow(@PathVariable Long userId, HttpSession session) { var me = store.users.get(requireUid(session)); me.following().add(userId); store.users.get(userId).followers().add(me.id()); return Map.of("status", "followed"); }
    @DeleteMapping("/{userId}/follow") public Map<String, String> unfollow(@PathVariable Long userId, HttpSession session) { var me = store.users.get(requireUid(session)); me.following().remove(userId); store.users.get(userId).followers().remove(me.id()); return Map.of("status", "unfollowed"); }

    @GetMapping("/shopping-list") public List<InMemoryStore.ShoppingItem> shopping(HttpSession session) { return store.users.get(requireUid(session)).shoppingList(); }
    @PostMapping("/shopping-list") public InMemoryStore.ShoppingItem addShopping(@RequestBody Map<String, String> req, HttpSession session) { var item = new InMemoryStore.ShoppingItem(store.shoppingSeq.incrementAndGet(), req.get("name"), req.getOrDefault("amount", "1"), false); store.users.get(requireUid(session)).shoppingList().add(item); return item; }

    private Long requireUid(HttpSession session) { var uid = (Long) session.getAttribute("uid"); if (uid == null) throw new IllegalStateException("Unauthorized"); return uid; }
}
