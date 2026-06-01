package ru.zagrebin.server.common;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public class ApiModels {
    public record User(Long id, String username, String email, String displayName, String bio, String avatarUrl,
                       Set<Long> following, Set<Long> followers, List<ShoppingItem> shoppingList) {
    }

    public record Post(Long id, Long authorId, String authorName, String authorHandle, String authorAvatarUrl,
                       String type, String title, String summary, String content, String imageUrl, int likes,
                       Instant createdAt, Integer cookTimeMinutes, BigDecimal proteinsPer100, BigDecimal fatsPer100,
                       BigDecimal carbsPer100, BigDecimal kcalPer100, List<Tag> tags, List<Ingredient> ingredients,
                       List<RecipeStep> steps, List<Comment> comments) {
    }

    public record Tag(Long id, String name, String label, String color) {
    }

    public record Ingredient(String name, BigDecimal amount, String unit) {
    }

    public record RecipeStep(Integer number, String description, String imageUrl) {
    }

    public record CreateRecipeRequest(String title, String summary, String content, String imageUrl,
                                      Integer cookTimeMinutes, BigDecimal proteinsPer100, BigDecimal fatsPer100,
                                      BigDecimal carbsPer100, BigDecimal kcalPer100, List<String> tags,
                                      List<Ingredient> ingredients, List<RecipeStep> steps) {
    }

    public record Comment(Long id, Long authorId, String text, Instant createdAt) {
    }

    public record ShoppingItem(Long id, String name, String amount, boolean checked) {
    }
}
