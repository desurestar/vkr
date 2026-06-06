package ru.zagrebin.server.post;

import jakarta.servlet.http.HttpServletRequest;
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
    public List<ApiModels.Post> recipes(
            @RequestParam Optional<String> q,
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size,
            @RequestParam Optional<Integer> minTime,
            @RequestParam Optional<Integer> maxTime,
            @RequestParam Optional<Double> minCalories,
            @RequestParam Optional<Double> maxCalories,
            @RequestParam Optional<Double> minProteins,
            @RequestParam Optional<Double> maxProteins,
            @RequestParam Optional<Double> minFats,
            @RequestParam Optional<Double> maxFats,
            @RequestParam Optional<Double> minCarbs,
            @RequestParam Optional<Double> maxCarbs,
            @RequestParam Optional<List<String>> tags,
            HttpSession s
    ) {
        return db.postsByType(
                "RECIPE",
                q.orElse(null),
                currentUid(s),
                page.orElse(null),
                size.orElse(null),
                new DbService.PostFilters(
                        minTime.orElse(null),
                        maxTime.orElse(null),
                        minCalories.orElse(null),
                        maxCalories.orElse(null),
                        minProteins.orElse(null),
                        maxProteins.orElse(null),
                        minFats.orElse(null),
                        maxFats.orElse(null),
                        minCarbs.orElse(null),
                        maxCarbs.orElse(null),
                        tags.orElse(List.of())
                )
        );
    }

    @GetMapping("/feed/articles")
    public List<ApiModels.Post> articles(
            @RequestParam Optional<String> q,
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size,
            @RequestParam Optional<List<String>> tags,
            HttpSession s
    ) {
        return db.postsByType(
                "ARTICLE",
                q.orElse(null),
                currentUid(s),
                page.orElse(null),
                size.orElse(null),
                DbService.PostFilters.tagsOnly(tags.orElse(List.of()))
        );
    }

    @GetMapping("/tags")
    public List<ApiModels.Tag> tags(@RequestParam Optional<String> q) {
        return db.tags(q.orElse(null));
    }

    @GetMapping("/recipes/{id}")
    public ApiModels.Post recipe(@PathVariable Long id, HttpSession s) {
        return requireVisiblePost(db.getPost(id, currentUid(s)), currentUid(s));
    }

    @PostMapping("/recipes")
    public ApiModels.Post createRecipe(@RequestBody ApiModels.CreateRecipeRequest req, HttpSession s) {
        return db.createRecipe(requireUid(s), req);
    }

    @PatchMapping("/recipes/{id}")
    public ApiModels.Post updateRecipe(@PathVariable Long id, @RequestBody ApiModels.CreateRecipeRequest req, HttpSession s) {
        return db.updateRecipe(id, requireUid(s), req);
    }

    @PostMapping("/articles")
    public ApiModels.Post createArticle(@RequestBody ApiModels.CreateArticleRequest req, HttpSession s) {
        return db.createArticle(requireUid(s), req);
    }

    @PatchMapping("/articles/{id}")
    public ApiModels.Post updateArticle(@PathVariable Long id, @RequestBody ApiModels.CreateArticleRequest req, HttpSession s) {
        return db.updateArticle(id, requireUid(s), req);
    }

    @GetMapping("/articles/{id}")
    public ApiModels.Post article(@PathVariable Long id, HttpSession s) {
        return requireVisiblePost(db.getPost(id, currentUid(s)), currentUid(s));
    }

    @GetMapping("/drafts")
    public List<ApiModels.Post> drafts(HttpSession s) {
        return db.draftsByAuthor(requireUid(s));
    }

    @DeleteMapping("/drafts/{id}")
    public Map<String, String> deleteDraft(@PathVariable Long id, HttpSession s) {
        db.deleteDraft(id, requireUid(s));
        return Map.of("status", "deleted");
    }


    @PostMapping("/posts/{id}/views")
    public ApiModels.Post recordView(
            @PathVariable Long id,
            @RequestBody ApiModels.PostViewRequest req,
            HttpSession s,
            HttpServletRequest request
    ) {
        var currentUserId = currentUid(s);
        requireVisiblePost(db.getPost(id, currentUserId), currentUserId);
        return db.recordPostView(id, currentUserId, viewerKey(s, request), req.durationSeconds());
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
    public ApiModels.Post like(@PathVariable Long id, HttpSession s) {
        var uid = requireUid(s);
        return db.likePost(id, uid);
    }

    @DeleteMapping("/posts/{id}/likes")
    public ApiModels.Post unlike(@PathVariable Long id, HttpSession s) {
        var uid = requireUid(s);
        return db.unlikePost(id, uid);
    }

    @PostMapping("/recipes/{id}/shopping-list")
    public Map<String, String> addRecipeToShopping(@PathVariable Long id, HttpSession s) {
        db.addRecipeIngredientsToShopping(requireUid(s), id);
        return Map.of("status", "added");
    }

    private ApiModels.Post requireVisiblePost(ApiModels.Post post, Long currentUserId) {
        if ("DRAFT".equalsIgnoreCase(post.status()) && !post.authorId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
        return post;
    }


    private String viewerKey(HttpSession session, HttpServletRequest request) {
        if (currentUid(session) != null) {
            return null;
        }
        return clientIp(request) + "|" + Optional.ofNullable(request.getHeader("User-Agent")).orElse("");
    }

    private String clientIp(HttpServletRequest request) {
        var forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        var realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return Optional.ofNullable(request.getRemoteAddr()).orElse("");
    }

    private Long currentUid(HttpSession session) {
        return (Long) session.getAttribute("uid");
    }

    private Long requireUid(HttpSession session) {
        var uid = (Long) session.getAttribute("uid");
        if (uid == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return uid;
    }
}
