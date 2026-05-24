package ru.zagrebin.server.common;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryStore {
    public record User(Long id, String username, String email, String passwordHash, String displayName, String bio, String avatarUrl,
                       Set<Long> following, Set<Long> followers, List<ShoppingItem> shoppingList) {}
    public record Post(Long id, Long authorId, String type, String title, String summary, String content, int likes,
                       Instant createdAt, List<String> tags, List<Comment> comments) {}
    public record Comment(Long id, Long authorId, String text, Instant createdAt) {}
    public record ShoppingItem(Long id, String name, String amount, boolean checked) {}

    public final Map<Long, User> users = new ConcurrentHashMap<>();
    public final Map<Long, Post> posts = new ConcurrentHashMap<>();
    public final AtomicLong userSeq = new AtomicLong(0);
    public final AtomicLong postSeq = new AtomicLong(0);
    public final AtomicLong commentSeq = new AtomicLong(0);
    public final AtomicLong shoppingSeq = new AtomicLong(0);
    public final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public InMemoryStore() {
        var uid = userSeq.incrementAndGet();
        users.put(uid, new User(uid, "demo", "demo@example.com", encoder.encode("demo12345"), "Demo User", "Bio", null,
                new HashSet<>(), new HashSet<>(), new ArrayList<>()));
        var pid = postSeq.incrementAndGet();
        posts.put(pid, new Post(pid, uid, "RECIPE", "Паста с песто", "Быстрый ужин", "Полный рецепт", 0, Instant.now(), List.of("ужин"), new ArrayList<>()));
    }
}
