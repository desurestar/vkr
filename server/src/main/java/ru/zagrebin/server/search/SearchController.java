package ru.zagrebin.server.search;

import org.springframework.web.bind.annotation.*;
import ru.zagrebin.server.common.InMemoryStore;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {
    private final InMemoryStore store;
    public SearchController(InMemoryStore store) { this.store = store; }

    @GetMapping
    public Map<String, List<?>> search(@RequestParam String query, @RequestParam(required = false) String type, @RequestParam(required = false) String tag) {
        var posts = store.posts.values().stream()
                .filter(p -> p.title().toLowerCase().contains(query.toLowerCase()))
                .filter(p -> type == null || p.type().equalsIgnoreCase(type))
                .filter(p -> tag == null || p.tags().contains(tag))
                .toList();
        var users = store.users.values().stream().filter(u -> u.username().toLowerCase().contains(query.toLowerCase())).toList();
        return Map.of("posts", posts, "users", users);
    }
}
