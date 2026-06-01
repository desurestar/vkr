package ru.zagrebin.server.data;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import ru.zagrebin.server.common.ApiModels;
import ru.zagrebin.server.data.entity.*;
import ru.zagrebin.server.data.repo.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class DbService {

    private final UserRepository users;
    private final PostRepository posts;
    private final CommentRepository comments;
    private final ShoppingItemRepository shopping;
    private final TagRepository tags;
    private final BCryptPasswordEncoder encoder;

    public DbService(UserRepository users,
                     PostRepository posts,
                     CommentRepository comments,
                     ShoppingItemRepository shopping,
                     TagRepository tags,
                     BCryptPasswordEncoder encoder) {

        this.users = users;
        this.posts = posts;
        this.comments = comments;
        this.shopping = shopping;
        this.tags = tags;
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
                shopping.findByUserId(u.getId()).stream().map(this::toShopping).toList()
        );
    }

    public ApiModels.Post toPost(PostEntity p) {
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
                p.getCreatedAt(),
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

    private String cleanRemoteImageUrl(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        var trimmed = value.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("/media/")) {
            return trimmed;
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

    public ApiModels.ShoppingItem toShopping(ShoppingItemEntity i) {
        return new ApiModels.ShoppingItem(
                i.getId(),
                i.getName(),
                i.getAmount(),
                i.isChecked()
        );
    }

    public List<ApiModels.Post> postsByType(String type, String q) {
        var list = (q == null)
                ? posts.findByTypeIgnoreCase(type)
                : posts.findByTypeIgnoreCaseAndTitleContainingIgnoreCase(type, q);

        return list.stream().map(this::toPost).toList();
    }

    public Map<String, List<?>> search(String query, String type, String tag) {

        var p = posts.findByTitleContainingIgnoreCase(query).stream()
                .filter(x -> type == null || x.getType().equalsIgnoreCase(type))
                .filter(x -> tag == null || x.getTags().stream().anyMatch(t -> t.getName().equalsIgnoreCase(tag)))
                .map(this::toPost)
                .toList();

        var u = users.findAll().stream()
                .filter(x -> x.getUsername().toLowerCase().contains(query.toLowerCase()))
                .map(this::toUser)
                .toList();

        return Map.of("posts", p, "users", u);
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
        post.setCreatedAt(Instant.now());
        post.setLikes(0);
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

    public ApiModels.Post createArticle(Long uid, ApiModels.CreateArticleRequest request) {
        var post = new PostEntity();
        post.setAuthor(getUserEntity(uid));
        post.setType("ARTICLE");
        post.setTitle(request.title());
        post.setSummary(request.summary());
        post.setContent(request.content());
        post.setImageUrl(cleanRemoteImageUrl(request.imageUrl()));
        post.setCreatedAt(Instant.now());
        post.setLikes(0);
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

    public ShoppingItemEntity addShopping(Long uid, String name, String amount) {
        var i = new ShoppingItemEntity();
        i.setUser(getUserEntity(uid));
        i.setName(name);
        i.setAmount(amount);
        i.setChecked(false);
        return shopping.save(i);
    }

    public List<ApiModels.Comment> comments(Long postId) {
        return comments.findByPostIdOrderByCreatedAtAsc(postId).stream().map(this::toComment).toList();
    }

    public List<ApiModels.ShoppingItem> shopping(Long uid) {
        return shopping.findByUserId(uid).stream().map(this::toShopping).toList();
    }

    public UserEntity saveUser(UserEntity u) {
        return users.save(u);
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
