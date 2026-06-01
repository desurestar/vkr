package ru.zagrebin.server.post;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.zagrebin.server.common.ApiModels;
import ru.zagrebin.server.data.DbService;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class PostController {
    private final DbService db;

    public PostController(DbService db) {
        this.db = db;
    }

    @GetMapping("/feed/recipes")
    public List<ApiModels.Post> recipes(@RequestParam Optional<String> q) {
        return db.postsByType("RECIPE", q.orElse(null));
    }

    @GetMapping("/feed/articles")
    public List<ApiModels.Post> articles(@RequestParam Optional<String> q) {
        return db.postsByType("ARTICLE", q.orElse(null));
    }

    @GetMapping("/tags")
    public List<ApiModels.Tag> tags(@RequestParam Optional<String> q) {
        return db.tags(q.orElse(null));
    }

    @GetMapping("/recipes/{id}")
    public ApiModels.Post recipe(@PathVariable Long id) {
        return db.getPost(id);
    }

    @PostMapping("/recipes")
    public ApiModels.Post createRecipe(@RequestBody ApiModels.CreateRecipeRequest req, HttpSession s) {
        return db.createRecipe(requireUid(s), req);
    }

    @PostMapping("/articles")
    public ApiModels.Post createArticle(@RequestBody ApiModels.CreateArticleRequest req, HttpSession s) {
        return db.createArticle(requireUid(s), req);
    }

    @GetMapping("/articles/{id}")
    public ApiModels.Post article(@PathVariable Long id) {
        return db.getPost(id);
    }

    @PostMapping("/posts/{id}/comments")
    public ApiModels.Comment addComment(@PathVariable Long id, @RequestBody ApiModels.CommentRequest req, HttpSession s) {
        return db.toComment(db.createComment(id, requireUid(s), req.text(), req.parentId()));
    }

    @GetMapping("/posts/{id}/comments")
    public List<ApiModels.Comment> comments(@PathVariable Long id) {
        return db.comments(id);
    }

    @DeleteMapping("/comments/{id}")
    public Map<String, String> deleteComment(@PathVariable Long id, HttpSession s) {
        db.deleteComment(id, requireUid(s));
        return Map.of("status", "deleted");
    }

    @PostMapping("/posts/{id}/likes")
    public Map<String, String> like(@PathVariable Long id, HttpSession s) {
        requireUid(s);
        return Map.of("status", "liked");
    }

    @DeleteMapping("/posts/{id}/likes")
    public Map<String, String> unlike(@PathVariable Long id, HttpSession s) {
        requireUid(s);
        return Map.of("status", "unliked");
    }

    @PostMapping("/recipes/{id}/shopping-list")
    public Map<String, String> addRecipeToShopping(@PathVariable Long id, HttpSession s) {
        db.addShopping(requireUid(s), "From recipe #" + id, "1");
        return Map.of("status", "added");
    }

    private Long requireUid(HttpSession session) {
        var uid = (Long) session.getAttribute("uid");
        if (uid == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return uid;
    }
}
