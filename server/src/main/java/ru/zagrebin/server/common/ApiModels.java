package ru.zagrebin.server.common;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class ApiModels {
    public record User(Long id, String username, String email, String displayName, String bio, String avatarUrl,
                       Set<Long> following, Set<Long> followers, List<ShoppingList> shoppingList) {
    }

    public record PublicProfile(User user, boolean following, List<Post> posts) {
    }

    public record Post(Long id, Long authorId, String authorName, String authorHandle, String authorAvatarUrl,
                       String type, String title, String summary, String content, String imageUrl, int likes,
                       boolean likedByMe, Instant createdAt, String status, Integer cookTimeMinutes, BigDecimal proteinsPer100, BigDecimal fatsPer100,
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
                                      BigDecimal carbsPer100, BigDecimal kcalPer100, String status, List<String> tags,
                                      List<Ingredient> ingredients, List<RecipeStep> steps) {
    }

    public record CreateArticleRequest(String title, String summary, String content, String imageUrl,
                                       String status, List<String> tags) {
    }

    public record Comment(Long id, Long authorId, String authorName, String authorHandle, String authorAvatarUrl,
                          Long parentId, String parentAuthorName, String text, Instant createdAt) {
    }

    public record CommentRequest(String text, Long parentId) {
    }

    public record ShoppingList(Long id, String name, List<ShoppingItem> items) {
    }

    public record ShoppingItemRequest(String name, String amount, Boolean checked) {
    }

    public record ShoppingItem(Long id, String name, String amount, boolean checked) {
    }

    public record StatisticsSettings(int retentionMonths, int goalKcal, int waterGoalMl,
                                     int proteinGoalGrams, int fatGoalGrams, int carbsGoalGrams) {
    }

    public record StatisticsMealEntry(Long id, String name, String amountLabel, String timeLabel, int kcal,
                                      BigDecimal proteins, BigDecimal fats, BigDecimal carbs) {
    }

    public record StatisticsDay(LocalDate date, int goalKcal, int waterGoalMl, int waterConsumedMl,
                                List<StatisticsMealEntry> breakfast, List<StatisticsMealEntry> lunch,
                                List<StatisticsMealEntry> dinner, List<StatisticsMealEntry> snack) {
    }

    public record StatisticsResponse(StatisticsSettings settings, List<StatisticsDay> days) {
    }

    public record StatisticsSettingsRequest(Integer retentionMonths, Integer goalKcal, Integer waterGoalMl,
                                            Integer proteinGoalGrams, Integer fatGoalGrams, Integer carbsGoalGrams) {
    }

    public record AddWaterRequest(LocalDate date, Integer amountMl) {
    }

    public record AddMealRequest(LocalDate date, String type, String name, String amountLabel, String timeLabel,
                                 Integer kcal, BigDecimal proteins, BigDecimal fats, BigDecimal carbs) {
    }

}
