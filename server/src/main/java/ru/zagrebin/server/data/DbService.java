package ru.zagrebin.server.data;

import jakarta.persistence.EntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import ru.zagrebin.server.common.ApiModels;
import ru.zagrebin.server.data.entity.*;
import ru.zagrebin.server.data.repo.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class DbService {
    private static final int MIN_VIEW_DURATION_SECONDS = 8;
    private static final Duration VIEW_REPEAT_COOLDOWN = Duration.ofMinutes(10);

    public record PostFilters(
            Integer minTime,
            Integer maxTime,
            Double minCalories,
            Double maxCalories,
            Double minProteins,
            Double maxProteins,
            Double minFats,
            Double maxFats,
            Double minCarbs,
            Double maxCarbs,
            List<String> tags
    ) {
        public static PostFilters empty() {
            return tagsOnly(List.of());
        }

        public static PostFilters tagsOnly(List<String> tags) {
            return new PostFilters(null, null, null, null, null, null, null, null, null, null, tags);
        }
    }

    private final UserRepository users;
    private final PostRepository posts;
    private final CommentRepository comments;
    private final ShoppingItemRepository shopping;
    private final ShoppingListRepository shoppingLists;
    private final TagRepository tags;
    private final JdbcTemplate jdbc;
    private final EntityManager entityManager;
    private final BCryptPasswordEncoder encoder;

    public DbService(UserRepository users,
                     PostRepository posts,
                     CommentRepository comments,
                     ShoppingItemRepository shopping,
                     ShoppingListRepository shoppingLists,
                     TagRepository tags,
                     JdbcTemplate jdbc,
                     EntityManager entityManager,
                     BCryptPasswordEncoder encoder) {

        this.users = users;
        this.posts = posts;
        this.comments = comments;
        this.shopping = shopping;
        this.shoppingLists = shoppingLists;
        this.tags = tags;
        this.jdbc = jdbc;
        this.entityManager = entityManager;
        this.encoder = encoder;
    }

    public UserEntity getUserEntity(Long id) {
        return users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public PostEntity getPostEntity(Long id) {
        return posts.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    @Transactional(readOnly = true)
    public ApiModels.Post getPost(Long id) {
        return toPost(getPostEntity(id));
    }

    @Transactional(readOnly = true)
    public ApiModels.Post getPost(Long id, Long currentUserId) {
        return toPost(getPostEntity(id), currentUserId);
    }

    @Transactional(readOnly = true)
    public ApiModels.User getUser(Long id) {
        return toUser(getUserEntity(id));
    }

    public ApiModels.User toUser(UserEntity u) {
        return new ApiModels.User(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getDisplayName(),
                u.getBio(),
                u.getAvatarUrl(),
                u.getFollowing().stream().map(UserEntity::getId).collect(Collectors.toSet()),
                u.getFollowers().stream().map(UserEntity::getId).collect(Collectors.toSet()),
                shoppingLists(u.getId())
        );
    }

    public ApiModels.Post toPost(PostEntity p) {
        return toPost(p, null);
    }

    public ApiModels.Post toPost(PostEntity p, Long currentUserId) {
        return new ApiModels.Post(
                p.getId(),
                p.getAuthor().getId(),
                p.getAuthor().getDisplayName() != null && !p.getAuthor().getDisplayName().isBlank() ? p.getAuthor().getDisplayName() : p.getAuthor().getUsername(),
                p.getAuthor().getUsername(),
                p.getAuthor().getAvatarUrl(),
                p.getType(),
                p.getTitle(),
                p.getSummary(),
                p.getContent(),
                p.getImageUrl(),
                p.getLikes(),
                p.getViews(),
                currentUserId != null && isPostLikedByUser(p.getId(), currentUserId),
                p.getCreatedAt(),
                p.getStatus(),
                p.getCookTimeMinutes(),
                p.getProteinsPer100(),
                p.getFatsPer100(),
                p.getCarbsPer100(),
                p.getKcalPer100(),
                p.getTags().stream().map(this::toTag).toList(),
                p.getIngredients().stream().map(x -> new ApiModels.Ingredient(x.getName(), x.getAmount(), x.getUnit())).toList(),
                p.getSteps().stream().map(x -> new ApiModels.RecipeStep(x.getStepNumber(), x.getDescription(), x.getImageUrl())).toList(),
                comments.findByPostIdOrderByCreatedAtAsc(p.getId()).stream().map(this::toComment).toList()
        );
    }

    private String normalizePostStatus(String status) {
        return "DRAFT".equalsIgnoreCase(status) ? "DRAFT" : "PUBLISHED";
    }

    public String cleanRemoteImageUrl(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        var trimmed = value.trim();
        if (trimmed.startsWith("/media/")) {
            return trimmed;
        }

        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            try {
                var uri = java.net.URI.create(trimmed);
                var path = uri.getPath();
                return path != null && path.startsWith("/media/") ? path : null;
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }

        return null;
    }

    public ApiModels.Comment toComment(CommentEntity c) {
        var author = c.getAuthor();
        var parent = c.getParent();
        var parentAuthor = parent != null ? parent.getAuthor() : null;
        return new ApiModels.Comment(
                c.getId(),
                author.getId(),
                author.getDisplayName() != null && !author.getDisplayName().isBlank() ? author.getDisplayName() : author.getUsername(),
                author.getUsername(),
                author.getAvatarUrl(),
                parent != null ? parent.getId() : null,
                parentAuthor != null
                        ? (parentAuthor.getDisplayName() != null && !parentAuthor.getDisplayName().isBlank() ? parentAuthor.getDisplayName() : parentAuthor.getUsername())
                        : null,
                c.getText(),
                c.getCreatedAt()
        );
    }

    public ApiModels.Post likePost(Long postId, Long userId) {
        var post = getPostEntity(postId);
        if (!isPostLikedByUser(postId, userId)) {
            jdbc.update("INSERT INTO post_like (post_id, user_id) VALUES (?, ?)", postId, userId);
            post.setLikes(post.getLikes() + 1);
            posts.save(post);
        }
        return toPost(post, userId);
    }

    public ApiModels.Post unlikePost(Long postId, Long userId) {
        var post = getPostEntity(postId);
        var deleted = jdbc.update("DELETE FROM post_like WHERE post_id = ? AND user_id = ?", postId, userId);
        if (deleted > 0 && post.getLikes() > 0) {
            post.setLikes(post.getLikes() - 1);
            posts.save(post);
        }
        return toPost(post, userId);
    }

    private boolean isPostLikedByUser(Long postId, Long userId) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM post_like WHERE post_id = ? AND user_id = ?",
                Integer.class,
                postId,
                userId
        ) > 0;
    }


    public ApiModels.Post recordPostView(Long postId, Long currentUserId, String viewerKey, Integer durationSeconds) {
        var post = getPostEntity(postId);
        if (!"PUBLISHED".equalsIgnoreCase(post.getStatus())) {
            return toPost(post, currentUserId);
        }
        if (durationSeconds == null || durationSeconds < MIN_VIEW_DURATION_SECONDS) {
            return toPost(post, currentUserId);
        }
        if (currentUserId != null && post.getAuthor().getId().equals(currentUserId)) {
            return toPost(post, currentUserId);
        }
        var normalizedViewerKey = normalizeViewerKey(currentUserId, viewerKey);
        if (normalizedViewerKey == null) {
            return toPost(post, currentUserId);
        }

        var inserted = jdbc.update(
                """
                INSERT INTO post_view (post_id, viewer_key, view_bucket, duration_seconds)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (post_id, viewer_key, view_bucket) DO NOTHING
                """,
                postId,
                normalizedViewerKey,
                currentViewBucket(),
                durationSeconds
        );
        if (inserted > 0) {
            jdbc.update("UPDATE post SET views = views + 1 WHERE id = ?", postId);
            entityManager.refresh(post);
        }
        return toPost(post, currentUserId);
    }

    private long currentViewBucket() {
        return Instant.now().getEpochSecond() / VIEW_REPEAT_COOLDOWN.toSeconds();
    }

    private String normalizeViewerKey(Long currentUserId, String viewerKey) {
        if (currentUserId != null) {
            return "user:" + currentUserId;
        }
        if (viewerKey == null || viewerKey.isBlank()) {
            return null;
        }
        return "guest:" + sha256(viewerKey.trim());
    }

    private String sha256(String value) {
        try {
            var digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            var result = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public ApiModels.ShoppingItem toShopping(ShoppingItemEntity i) {
        return new ApiModels.ShoppingItem(
                i.getId(),
                i.getName(),
                i.getAmount(),
                i.isChecked()
        );
    }

    public ApiModels.ShoppingList toShoppingList(ShoppingListEntity list) {
        return new ApiModels.ShoppingList(
                list.getId(),
                list.getName(),
                list.getItems().stream().map(this::toShopping).toList()
        );
    }

    public List<ApiModels.Post> postsByType(String type, String q) {
        return postsByType(type, q, null);
    }

    public List<ApiModels.Post> postsByType(String type, String q, Long currentUserId) {
        return postsByType(type, q, currentUserId, null, null);
    }

    public List<ApiModels.Post> postsByType(String type, String q, Long currentUserId, Integer page, Integer size) {
        return postsByType(type, q, currentUserId, page, size, PostFilters.empty());
    }

    public List<ApiModels.Post> postsByType(
            String type,
            String q,
            Long currentUserId,
            Integer page,
            Integer size,
            PostFilters filters
    ) {
        var normalizedQuery = q == null || q.isBlank() ? null : q.trim();
        var pageIndex = Math.max(page == null ? 0 : page, 0);
        var pageSize = Math.min(Math.max(size == null ? Integer.MAX_VALUE : size, 1), 50);
        var selectedTags = normalizeTags(filters == null ? List.of() : filters.tags());

        return posts.findByTypeIgnoreCaseAndStatusIgnoreCase(type, "PUBLISHED").stream()
                .filter(post -> normalizedQuery == null || post.getTitle().toLowerCase().contains(normalizedQuery.toLowerCase()))
                .filter(post -> matchesPostFilters(post, filters, selectedTags))
                .sorted(Comparator.comparing(PostEntity::getCreatedAt).reversed())
                .skip(page == null || size == null ? 0 : (long) pageIndex * pageSize)
                .limit(page == null || size == null ? Long.MAX_VALUE : pageSize)
                .map(post -> toPost(post, currentUserId))
                .toList();
    }

    private Set<String> normalizeTags(List<String> rawTags) {
        if (rawTags == null) {
            return Set.of();
        }
        return rawTags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(tag -> tag.replace("#", "").trim().toLowerCase())
                .collect(Collectors.toSet());
    }

    private boolean matchesPostFilters(PostEntity post, PostFilters filters, Set<String> selectedTags) {
        if (filters == null) {
            return selectedTags.isEmpty();
        }
        return matchesRange(post.getCookTimeMinutes(), filters.minTime(), filters.maxTime())
                && matchesRange(post.getKcalPer100(), filters.minCalories(), filters.maxCalories())
                && matchesRange(post.getProteinsPer100(), filters.minProteins(), filters.maxProteins())
                && matchesRange(post.getFatsPer100(), filters.minFats(), filters.maxFats())
                && matchesRange(post.getCarbsPer100(), filters.minCarbs(), filters.maxCarbs())
                && matchesTags(post, selectedTags);
    }

    private boolean matchesRange(Integer value, Integer min, Integer max) {
        if (min == null && max == null) {
            return true;
        }
        if (value == null) {
            return false;
        }
        return (min == null || value >= min) && (max == null || value <= max);
    }

    private boolean matchesRange(BigDecimal value, Double min, Double max) {
        if (min == null && max == null) {
            return true;
        }
        if (value == null) {
            return false;
        }
        var current = value.doubleValue();
        return (min == null || current >= min) && (max == null || current <= max);
    }

    private boolean matchesTags(PostEntity post, Set<String> selectedTags) {
        if (selectedTags.isEmpty()) {
            return true;
        }
        var postTags = post.getTags().stream()
                .flatMap(tag -> List.of(tag.getName(), tag.getLabel()).stream())
                .filter(tag -> tag != null && !tag.isBlank())
                .map(tag -> tag.replace("#", "").trim().toLowerCase())
                .collect(Collectors.toSet());
        return postTags.containsAll(selectedTags);
    }

    @Transactional(readOnly = true)
    public List<ApiModels.Post> postsByAuthor(Long authorId) {
        return postsByAuthor(authorId, null);
    }


    @Transactional(readOnly = true)
    public List<ApiModels.Post> postsByAuthor(Long authorId, Long currentUserId) {
        return postsByAuthor(authorId, currentUserId, null, null, null);
    }


    @Transactional(readOnly = true)
    public List<ApiModels.Post> postsByAuthor(Long authorId, Long currentUserId, String q, Integer page, Integer size) {
        var normalizedQuery = q == null || q.isBlank() ? null : q.trim();
        var pageable = page != null && size != null
                ? PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "createdAt"))
                : null;
        var list = normalizedQuery == null
                ? (pageable == null
                        ? posts.findByAuthorIdAndStatusIgnoreCaseOrderByCreatedAtDesc(authorId, "PUBLISHED")
                        : posts.findByAuthorIdAndStatusIgnoreCaseOrderByCreatedAtDesc(authorId, "PUBLISHED", pageable))
                : (pageable == null
                        ? posts.findByAuthorIdAndStatusIgnoreCaseAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(authorId, "PUBLISHED", normalizedQuery)
                        : posts.findByAuthorIdAndStatusIgnoreCaseAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(authorId, "PUBLISHED", normalizedQuery, pageable));

        return list.stream().map(post -> toPost(post, currentUserId)).toList();
    }

    @Transactional(readOnly = true)
    public List<ApiModels.Post> draftsByAuthor(Long authorId) {
        return posts.findByAuthorIdAndStatusIgnoreCaseOrderByCreatedAtDesc(authorId, "DRAFT").stream()
                .map(post -> toPost(post, authorId))
                .toList();
    }

    public void deleteDraft(Long postId, Long userId) {
        var post = getPostEntity(postId);
        if (!post.getAuthor().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only author can delete draft");
        }
        if (!"DRAFT".equalsIgnoreCase(post.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post is not a draft");
        }
        posts.delete(post);
    }

    public Map<String, List<?>> search(String query, String type, String tag) {
        return search(query, type, tag, null, null);
    }

    public Map<String, List<?>> search(String query, String type, String tag, Integer page, Integer size) {
        var normalizedQuery = normalizeSearchQuery(query);
        var pageable = page != null && size != null
                ? PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "createdAt"))
                : null;

        var p = posts.findByStatusIgnoreCaseAndTitleContainingIgnoreCase("PUBLISHED", normalizedQuery).stream()
                .filter(x -> type == null || x.getType().equalsIgnoreCase(type))
                .filter(x -> tag == null || x.getTags().stream().anyMatch(t -> t.getName().equalsIgnoreCase(tag)))
                .skip(pageable == null ? 0 : pageable.getOffset())
                .limit(pageable == null ? Long.MAX_VALUE : pageable.getPageSize())
                .map(this::toPost)
                .toList();

        var u = searchUsers(normalizedQuery, page, size);

        return Map.of("posts", p, "users", u);
    }

    public List<ApiModels.User> searchUsers(String query, Integer page, Integer size) {
        var normalizedQuery = normalizeSearchQuery(query);
        if (normalizedQuery.isBlank()) {
            return List.of();
        }
        var pageable = PageRequest.of(
                Math.max(page == null ? 0 : page, 0),
                Math.min(Math.max(size == null ? 10 : size, 1), 25),
                Sort.by(Sort.Direction.ASC, "username")
        );
        return users.findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(normalizedQuery, normalizedQuery, pageable).stream()
                .map(this::toUser)
                .toList();
    }

    private String normalizeSearchQuery(String query) {
        if (query == null) {
            return "";
        }
        var normalized = query.trim();
        while (normalized.startsWith("@")) {
            normalized = normalized.substring(1).trim();
        }
        return normalized;
    }


    public ApiModels.Post createRecipe(Long uid, ApiModels.CreateRecipeRequest request) {
        var post = new PostEntity();
        post.setAuthor(getUserEntity(uid));
        post.setType("RECIPE");
        post.setTitle(request.title());
        post.setSummary(request.summary());
        post.setContent(request.content());
        post.setImageUrl(cleanRemoteImageUrl(request.imageUrl()));
        post.setCookTimeMinutes(request.cookTimeMinutes());
        post.setProteinsPer100(request.proteinsPer100());
        post.setFatsPer100(request.fatsPer100());
        post.setCarbsPer100(request.carbsPer100());
        post.setKcalPer100(request.kcalPer100());
        post.setStatus(normalizePostStatus(request.status()));
        post.setCreatedAt(Instant.now());
        post.setLikes(0);
        post.setViews(0);
        if (request.tags() != null) {
            post.getTags().addAll(request.tags().stream()
                    .map(this::findOrCreateTag)
                    .toList());
        }
        if (request.ingredients() != null) {
            for (var i : request.ingredients()) {
                var e = new RecipeIngredientEntity();
                e.setPost(post);
                e.setName(i.name());
                e.setAmount(i.amount());
                e.setUnit(i.unit());
                post.getIngredients().add(e);
            }
        }
        if (request.steps() != null) {
            for (var st : request.steps()) {
                var e = new RecipeStepEntity();
                e.setPost(post);
                e.setStepNumber(st.number());
                e.setDescription(st.description());
                e.setImageUrl(cleanRemoteImageUrl(st.imageUrl()));
                post.getSteps().add(e);
            }
        }
        return toPost(posts.save(post));
    }


    public ApiModels.Post updateRecipe(Long postId, Long uid, ApiModels.CreateRecipeRequest request) {
        var post = getPostEntity(postId);
        requireAuthor(post, uid);
        if (!"RECIPE".equalsIgnoreCase(post.getType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post is not a recipe");
        }
        applyCommonPostFields(post, request.title(), request.summary(), request.content(), request.imageUrl(), request.status(), request.tags());
        post.setCookTimeMinutes(request.cookTimeMinutes());
        post.setProteinsPer100(request.proteinsPer100());
        post.setFatsPer100(request.fatsPer100());
        post.setCarbsPer100(request.carbsPer100());
        post.setKcalPer100(request.kcalPer100());

        post.getIngredients().clear();
        if (request.ingredients() != null) {
            for (var i : request.ingredients()) {
                var e = new RecipeIngredientEntity();
                e.setPost(post);
                e.setName(i.name());
                e.setAmount(i.amount());
                e.setUnit(i.unit());
                post.getIngredients().add(e);
            }
        }

        post.getSteps().clear();
        if (request.steps() != null) {
            for (var st : request.steps()) {
                var e = new RecipeStepEntity();
                e.setPost(post);
                e.setStepNumber(st.number());
                e.setDescription(st.description());
                e.setImageUrl(cleanRemoteImageUrl(st.imageUrl()));
                post.getSteps().add(e);
            }
        }
        return toPost(posts.save(post), uid);
    }

    public ApiModels.Post updateArticle(Long postId, Long uid, ApiModels.CreateArticleRequest request) {
        var post = getPostEntity(postId);
        requireAuthor(post, uid);
        if (!"ARTICLE".equalsIgnoreCase(post.getType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post is not an article");
        }
        applyCommonPostFields(post, request.title(), request.summary(), request.content(), request.imageUrl(), request.status(), request.tags());
        return toPost(posts.save(post), uid);
    }

    private void requireAuthor(PostEntity post, Long uid) {
        if (!post.getAuthor().getId().equals(uid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only author can edit post");
        }
    }

    private void applyCommonPostFields(PostEntity post, String title, String summary, String content, String imageUrl, String status, List<String> tags) {
        post.setTitle(title);
        post.setSummary(summary);
        post.setContent(content);
        post.setImageUrl(cleanRemoteImageUrl(imageUrl));
        post.setStatus(normalizePostStatus(status));
        post.getTags().clear();
        if (tags != null) {
            post.getTags().addAll(tags.stream().map(this::findOrCreateTag).toList());
        }
    }

    public ApiModels.Post createArticle(Long uid, ApiModels.CreateArticleRequest request) {
        var post = new PostEntity();
        post.setAuthor(getUserEntity(uid));
        post.setType("ARTICLE");
        post.setTitle(request.title());
        post.setSummary(request.summary());
        post.setContent(request.content());
        post.setImageUrl(cleanRemoteImageUrl(request.imageUrl()));
        post.setStatus(normalizePostStatus(request.status()));
        post.setCreatedAt(Instant.now());
        post.setLikes(0);
        post.setViews(0);
        if (request.tags() != null) {
            post.getTags().addAll(request.tags().stream()
                    .map(this::findOrCreateTag)
                    .toList());
        }
        return toPost(posts.save(post));
    }

    public CommentEntity createComment(Long postId, Long uid, String text, Long parentId) {
        var cleanedText = text == null ? "" : text.trim();
        if (cleanedText.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment text is required");
        }

        var post = getPostEntity(postId);
        var c = new CommentEntity();
        c.setAuthor(getUserEntity(uid));
        c.setPost(post);
        if (parentId != null) {
            var parent = comments.findById(parentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent comment not found"));
            if (!parent.getPost().getId().equals(postId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent comment belongs to another post");
            }
            c.setParent(parent);
        }
        c.setText(cleanedText);
        c.setCreatedAt(Instant.now());
        return comments.save(c);
    }

    public void deleteComment(Long commentId, Long uid) {
        var comment = comments.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        if (!comment.getAuthor().getId().equals(uid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only author can delete comment");
        }
        comments.delete(comment);
    }

    public ShoppingListEntity createShoppingList(Long uid, String name) {
        var list = new ShoppingListEntity();
        list.setUser(getUserEntity(uid));
        list.setName(cleanRequired(name, "List name is required"));
        return shoppingLists.save(list);
    }

    public ShoppingListEntity updateShoppingList(Long uid, Long listId, String name) {
        var list = requireShoppingList(uid, listId);
        list.setName(cleanRequired(name, "List name is required"));
        return shoppingLists.save(list);
    }

    public void deleteShoppingList(Long uid, Long listId) {
        shoppingLists.delete(requireShoppingList(uid, listId));
    }

    public ShoppingItemEntity addShopping(Long uid, Long listId, String name, String amount) {
        var list = listId == null ? getOrCreateDefaultShoppingList(uid) : requireShoppingList(uid, listId);
        var i = new ShoppingItemEntity();
        i.setUser(getUserEntity(uid));
        i.setList(list);
        i.setName(cleanRequired(name, "Item name is required"));
        i.setAmount((amount == null || amount.isBlank()) ? "1" : amount.trim());
        i.setChecked(false);
        return shopping.save(i);
    }

    public ShoppingItemEntity addShopping(Long uid, String name, String amount) {
        return addShopping(uid, null, name, amount);
    }

    public ShoppingItemEntity updateShoppingItem(Long uid, Long itemId, String name, String amount, Boolean checked) {
        var item = requireShoppingItem(uid, itemId);
        if (name != null) {
            item.setName(cleanRequired(name, "Item name is required"));
        }
        if (amount != null) {
            item.setAmount(amount.isBlank() ? "1" : amount.trim());
        }
        if (checked != null) {
            item.setChecked(checked);
        }
        return shopping.save(item);
    }

    public void deleteShoppingItem(Long uid, Long itemId) {
        shopping.delete(requireShoppingItem(uid, itemId));
    }

    public void addRecipeIngredientsToShopping(Long uid, Long recipeId) {
        var recipe = getPostEntity(recipeId);
        if (!"RECIPE".equalsIgnoreCase(recipe.getType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post is not a recipe");
        }
        var list = createShoppingList(uid, recipe.getTitle());
        for (var ingredient : recipe.getIngredients()) {
            addShopping(uid, list.getId(), ingredient.getName(), formatAmount(ingredient.getAmount(), ingredient.getUnit()));
        }
    }

    private ShoppingListEntity requireShoppingList(Long uid, Long listId) {
        var list = shoppingLists.findById(listId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shopping list not found"));
        if (!list.getUser().getId().equals(uid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Shopping list belongs to another user");
        }
        return list;
    }

    private ShoppingItemEntity requireShoppingItem(Long uid, Long itemId) {
        var item = shopping.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shopping item not found"));
        if (!item.getUser().getId().equals(uid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Shopping item belongs to another user");
        }
        return item;
    }

    private ShoppingListEntity getOrCreateDefaultShoppingList(Long uid) {
        return shoppingLists.findByUserIdOrderByIdAsc(uid).stream()
                .findFirst()
                .orElseGet(() -> createShoppingList(uid, "Мой список"));
    }

    private String cleanRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private String formatAmount(java.math.BigDecimal amount, String unit) {
        var amountText = amount == null ? "1" : amount.stripTrailingZeros().toPlainString();
        return unit == null || unit.isBlank() ? amountText : amountText + " " + unit;
    }

    public List<ApiModels.Comment> comments(Long postId) {
        return comments.findByPostIdOrderByCreatedAtAsc(postId).stream().map(this::toComment).toList();
    }

    public List<ApiModels.ShoppingList> shoppingLists(Long uid) {
        return shoppingLists.findByUserIdOrderByIdAsc(uid).stream().map(this::toShoppingList).toList();
    }

    public UserEntity saveUser(UserEntity u) {
        return users.save(u);
    }

    public boolean isFollowing(Long viewerId, Long userId) {
        return users.isFollowing(viewerId, userId);
    }

    public void followUser(Long followerId, Long targetId) {
        var follower = getUserEntity(followerId);
        var target = getUserEntity(targetId);
        if (follower.getId().equals(target.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot follow yourself");
        }

        follower.getFollowing().add(target);
        users.save(follower);
    }

    public void unfollowUser(Long followerId, Long targetId) {
        var follower = getUserEntity(followerId);
        getUserEntity(targetId);

        follower.getFollowing().removeIf(followed -> followed.getId().equals(targetId));
        users.save(follower);
    }

    public boolean emailExists(String email) {
        return users.existsByEmailIgnoreCase(email);
    }

    public boolean usernameExists(String username) {
        return users.existsByUsernameIgnoreCase(username);
    }

    public UserEntity findByEmail(String email) {
        return users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
    }

    public UserEntity findByEmailOrUsername(String loginOrEmail) {
        return users.findByEmailIgnoreCase(loginOrEmail)
                .or(() -> users.findByUsernameIgnoreCase(loginOrEmail))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
    }

    public List<ApiModels.User> allUsers() {
        return users.findAll().stream().map(this::toUser).toList();
    }


    public List<ApiModels.Tag> tags(String q) {
        var list = (q == null || q.isBlank()) ? tags.findAll() : tags.findByNameContainingIgnoreCase(q);
        return list.stream().map(this::toTag).toList();
    }

    private ApiModels.Tag toTag(TagEntity t) {
        return new ApiModels.Tag(t.getId(), t.getName(), t.getLabel(), t.getColor());
    }

    private TagEntity findOrCreateTag(String name) {
        return tags.findByNameIgnoreCase(name).orElseGet(() -> {
            var t = new TagEntity();
            t.setName(name);
            t.setLabel(name);
            t.setColor("#B57A1D");
            return tags.save(t);
        });
    }

}
