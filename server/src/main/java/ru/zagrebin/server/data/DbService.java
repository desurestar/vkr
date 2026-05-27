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
    private final BCryptPasswordEncoder encoder;

    public DbService(UserRepository users,
                     PostRepository posts,
                     CommentRepository comments,
                     ShoppingItemRepository shopping,
                     BCryptPasswordEncoder encoder) {

        this.users = users;
        this.posts = posts;
        this.comments = comments;
        this.shopping = shopping;
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
                p.getType(),
                p.getTitle(),
                p.getSummary(),
                p.getContent(),
                p.getLikes(),
                p.getCreatedAt(),
                p.getCookTimeMinutes(),
                p.getTags(),
                comments.findByPostId(p.getId()).stream().map(this::toComment).toList()
        );
    }

    public ApiModels.Comment toComment(CommentEntity c) {
        return new ApiModels.Comment(
                c.getId(),
                c.getAuthor().getId(),
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
                .filter(x -> tag == null || x.getTags().contains(tag))
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
        post.setCookTimeMinutes(request.cookTimeMinutes());
        post.setCreatedAt(Instant.now());
        post.setLikes(0);
        if (request.tags() != null) {
            post.getTags().addAll(request.tags());
        }
        return toPost(posts.save(post));
    }

    public CommentEntity createComment(Long postId, Long uid, String text) {
        var c = new CommentEntity();
        c.setAuthor(getUserEntity(uid));
        c.setPost(getPostEntity(postId));
        c.setText(text);
        c.setCreatedAt(Instant.now());
        return comments.save(c);
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
        return comments.findByPostId(postId).stream().map(this::toComment).toList();
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
}
