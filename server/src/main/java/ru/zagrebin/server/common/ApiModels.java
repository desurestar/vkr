package ru.zagrebin.server.common;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public class ApiModels {
    public record User(Long id, String username, String email, String displayName, String bio, String avatarUrl,
                       Set<Long> following, Set<Long> followers, List<ShoppingItem> shoppingList) {}
    public record Post(Long id, Long authorId, String type, String title, String summary, String content, int likes,
                       Instant createdAt, Integer cookTimeMinutes, List<String> tags, List<Comment> comments) {}

    public record CreateRecipeRequest(String title, String summary, String content, Integer cookTimeMinutes, List<String> tags) {}
    public record Comment(Long id, Long authorId, String text, Instant createdAt) {}
    public record ShoppingItem(Long id, String name, String amount, boolean checked) {}
}
