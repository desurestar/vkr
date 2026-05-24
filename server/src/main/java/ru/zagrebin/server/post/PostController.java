package ru.zagrebin.server.post;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import ru.zagrebin.server.common.InMemoryStore;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class PostController {
    private final InMemoryStore store;
    public PostController(InMemoryStore store) { this.store = store; }

    @GetMapping("/feed/recipes") public List<InMemoryStore.Post> recipes(@RequestParam Optional<String> q) { return filter("RECIPE", q); }
    @GetMapping("/feed/articles") public List<InMemoryStore.Post> articles(@RequestParam Optional<String> q) { return filter("ARTICLE", q); }
    @GetMapping("/recipes/{id}") public InMemoryStore.Post recipe(@PathVariable Long id) { return store.posts.get(id); }
    @GetMapping("/articles/{id}") public InMemoryStore.Post article(@PathVariable Long id) { return store.posts.get(id); }

    @PostMapping("/posts/{id}/comments")
    public InMemoryStore.Comment addComment(@PathVariable Long id, @RequestBody Map<String, String> req, HttpSession s) {
        var c = new InMemoryStore.Comment(store.commentSeq.incrementAndGet(), requireUid(s), req.get("text"), Instant.now());
        store.posts.get(id).comments().add(c); return c;
    }

    @GetMapping("/posts/{id}/comments") public List<InMemoryStore.Comment> comments(@PathVariable Long id) { return store.posts.get(id).comments(); }

    @PostMapping("/posts/{id}/likes") public Map<String, String> like(@PathVariable Long id, HttpSession s) { requireUid(s); return Map.of("status", "liked"); }
    @DeleteMapping("/posts/{id}/likes") public Map<String, String> unlike(@PathVariable Long id, HttpSession s) { requireUid(s); return Map.of("status", "unliked"); }

    @PostMapping("/recipes/{id}/shopping-list")
    public Map<String, String> addRecipeToShopping(@PathVariable Long id, HttpSession s) {
        var u = store.users.get(requireUid(s));
        u.shoppingList().add(new InMemoryStore.ShoppingItem(store.shoppingSeq.incrementAndGet(), "From recipe #" + id, "1", false));
        return Map.of("status", "added");
    }

    private List<InMemoryStore.Post> filter(String type, Optional<String> q) {
        return store.posts.values().stream().filter(p -> p.type().equals(type))
                .filter(p -> q.map(v -> p.title().toLowerCase().contains(v.toLowerCase())).orElse(true)).toList();
    }
    private Long requireUid(HttpSession session) { var uid = (Long) session.getAttribute("uid"); if (uid == null) throw new IllegalStateException("Unauthorized"); return uid; }
}
