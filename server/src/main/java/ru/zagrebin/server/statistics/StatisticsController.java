package ru.zagrebin.server.statistics;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.zagrebin.server.common.ApiModels;
import ru.zagrebin.server.common.ServerValidation;
import ru.zagrebin.server.data.entity.StatisticsDayEntity;
import ru.zagrebin.server.data.entity.StatisticsMealEntryEntity;
import ru.zagrebin.server.data.entity.StatisticsSettingsEntity;
import ru.zagrebin.server.data.repo.StatisticsDayRepository;
import ru.zagrebin.server.data.repo.StatisticsMealEntryRepository;
import ru.zagrebin.server.data.repo.StatisticsSettingsRepository;
import ru.zagrebin.server.data.DbService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticsController {
    private final DbService db;
    private final StatisticsSettingsRepository settingsRepo;
    private final StatisticsDayRepository daysRepo;
    private final StatisticsMealEntryRepository mealsRepo;

    public StatisticsController(DbService db, StatisticsSettingsRepository settingsRepo,
                                StatisticsDayRepository daysRepo, StatisticsMealEntryRepository mealsRepo) {
        this.db = db;
        this.settingsRepo = settingsRepo;
        this.daysRepo = daysRepo;
        this.mealsRepo = mealsRepo;
    }

    @GetMapping
    public ApiModels.StatisticsResponse getStatistics(@RequestParam(required = false) String month, HttpSession session) {
        var uid = requireUid(session);
        var settings = getOrCreateSettings(uid);
        prune(uid, settings.getRetentionMonths());
        var normalizedMonth = ServerValidation.parseMonthOrThrow(month);
        var yearMonth = normalizedMonth == null ? YearMonth.now() : YearMonth.parse(normalizedMonth);
        var start = yearMonth.atDay(1);
        var end = yearMonth.atEndOfMonth();
        var waterByDate = daysRepo.findByUserIdAndDateBetween(uid, start, end).stream()
                .collect(java.util.stream.Collectors.toMap(StatisticsDayEntity::getDate, StatisticsDayEntity::getWaterConsumedMl));
        var meals = mealsRepo.findByUserIdAndDateBetweenOrderByCreatedAtAsc(uid, start, end);

        var days = start.datesUntil(end.plusDays(1)).map(date -> toDay(date, settings, waterByDate.getOrDefault(date, 0), meals)).toList();
        return new ApiModels.StatisticsResponse(toSettings(settings), days);
    }

    @PatchMapping("/settings")
    public ApiModels.StatisticsSettings updateSettings(@RequestBody ApiModels.StatisticsSettingsRequest req, HttpSession session) {
        req = ServerValidation.requireBody(req);
        var uid = requireUid(session);
        var settings = getOrCreateSettings(uid);
        if (req.retentionMonths() != null) settings.setRetentionMonths(clamp(req.retentionMonths(), 1, 24));
        if (req.goalKcal() != null) settings.setGoalKcal(clamp(req.goalKcal(), 1, 10000));
        if (req.waterGoalMl() != null) settings.setWaterGoalMl(clamp(req.waterGoalMl(), 0, 10000));
        if (req.proteinGoalGrams() != null) settings.setProteinGoalGrams(clamp(req.proteinGoalGrams(), 0, 1000));
        if (req.fatGoalGrams() != null) settings.setFatGoalGrams(clamp(req.fatGoalGrams(), 0, 1000));
        if (req.carbsGoalGrams() != null) settings.setCarbsGoalGrams(clamp(req.carbsGoalGrams(), 0, 2000));
        settings = settingsRepo.save(settings);
        prune(uid, settings.getRetentionMonths());
        return toSettings(settings);
    }

    @PostMapping("/water")
    public ApiModels.StatisticsDay addWater(@RequestBody ApiModels.AddWaterRequest req, HttpSession session) {
        req = ServerValidation.requireBody(req);
        var uid = requireUid(session);
        var date = ServerValidation.dateOrToday(req.date());
        var amount = req.amountMl() == null ? 250 : ServerValidation.nonNegativeInt(req.amountMl(), "Water amount", ServerValidation.MAX_WATER_PER_REQUEST_ML);
        var day = daysRepo.findByUserIdAndDate(uid, date).orElseGet(() -> {
            var d = new StatisticsDayEntity();
            d.setUser(db.getUserEntity(uid));
            d.setDate(date);
            return d;
        });
        day.setWaterConsumedMl(day.getWaterConsumedMl() + amount);
        daysRepo.save(day);
        var settings = getOrCreateSettings(uid);
        prune(uid, settings.getRetentionMonths());
        var meals = mealsRepo.findByUserIdAndDateBetweenOrderByCreatedAtAsc(uid, date, date);
        return toDay(date, settings, day.getWaterConsumedMl(), meals);
    }

    @PostMapping("/meals")
    public ApiModels.StatisticsMealEntry addMeal(@RequestBody ApiModels.AddMealRequest req, HttpSession session) {
        req = ServerValidation.requireBody(req);
        var uid = requireUid(session);
        var meal = new StatisticsMealEntryEntity();
        meal.setUser(db.getUserEntity(uid));
        meal.setDate(ServerValidation.dateOrToday(req.date()));
        meal.setType(cleanType(req.type()));
        var name = ServerValidation.optionalText(req.name(), ServerValidation.MAX_MEAL_LABEL_LENGTH);
        var amountLabel = ServerValidation.optionalText(req.amountLabel(), ServerValidation.MAX_MEAL_LABEL_LENGTH);
        meal.setName(name == null ? "Прием пищи" : name);
        meal.setAmountLabel(amountLabel == null ? "0гр" : amountLabel);
        meal.setTimeLabel(ServerValidation.timeLabelOrDefault(req.timeLabel(), "--:--"));
        meal.setKcal(req.kcal() == null ? 0 : ServerValidation.nonNegativeInt(req.kcal(), "Calories", ServerValidation.MAX_KCAL));
        meal.setProteins(nonNegative(req.proteins()));
        meal.setFats(nonNegative(req.fats()));
        meal.setCarbs(nonNegative(req.carbs()));
        meal.setCreatedAt(Instant.now());
        var saved = mealsRepo.save(meal);
        prune(uid, getOrCreateSettings(uid).getRetentionMonths());
        return toMeal(saved);
    }


    @PostMapping("/recipes/{recipeId}/meals")
    public ApiModels.StatisticsMealEntry addRecipeMeal(@PathVariable Long recipeId,
                                                       @RequestBody ApiModels.AddRecipeMealRequest req,
                                                       HttpSession session) {
        req = ServerValidation.requireBody(req);
        var uid = requireUid(session);
        var recipe = db.getPostEntity(recipeId);
        if (!"RECIPE".equalsIgnoreCase(recipe.getType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post is not a recipe");
        }
        if ("DRAFT".equalsIgnoreCase(recipe.getStatus()) && !recipe.getAuthor().getId().equals(uid)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found");
        }

        var portion = req.portionGrams() == null ? 100 : ServerValidation.nonNegativeInt(req.portionGrams(), "Portion", ServerValidation.MAX_PORTION_GRAMS);
        var factor = BigDecimal.valueOf(portion).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        var meal = new StatisticsMealEntryEntity();
        meal.setUser(db.getUserEntity(uid));
        meal.setDate(ServerValidation.dateOrToday(req.date()));
        meal.setType(cleanType(req.type()));
        meal.setName(recipe.getTitle() == null || recipe.getTitle().isBlank() ? "Рецепт" : recipe.getTitle().trim());
        meal.setAmountLabel(portion + (Boolean.TRUE.equals(req.liquid()) ? "мл" : "гр"));
        meal.setTimeLabel(ServerValidation.timeLabelOrDefault(req.timeLabel(), currentTimeLabel()));
        meal.setKcal(scaled(recipe.getKcalPer100(), factor).setScale(0, RoundingMode.HALF_UP).intValue());
        meal.setProteins(scaled(recipe.getProteinsPer100(), factor));
        meal.setFats(scaled(recipe.getFatsPer100(), factor));
        meal.setCarbs(scaled(recipe.getCarbsPer100(), factor));
        meal.setCreatedAt(Instant.now());
        var saved = mealsRepo.save(meal);
        prune(uid, getOrCreateSettings(uid).getRetentionMonths());
        return toMeal(saved);
    }

    private ApiModels.StatisticsDay toDay(LocalDate date, StatisticsSettingsEntity settings, int water, List<StatisticsMealEntryEntity> meals) {
        return new ApiModels.StatisticsDay(
                date,
                settings.getGoalKcal(),
                settings.getWaterGoalMl(),
                water,
                meals.stream().filter(m -> m.getDate().equals(date) && "BREAKFAST".equals(m.getType())).map(this::toMeal).toList(),
                meals.stream().filter(m -> m.getDate().equals(date) && "LUNCH".equals(m.getType())).map(this::toMeal).toList(),
                meals.stream().filter(m -> m.getDate().equals(date) && "DINNER".equals(m.getType())).map(this::toMeal).toList(),
                meals.stream().filter(m -> m.getDate().equals(date) && "SNACK".equals(m.getType())).map(this::toMeal).toList()
        );
    }

    private ApiModels.StatisticsMealEntry toMeal(StatisticsMealEntryEntity e) {
        return new ApiModels.StatisticsMealEntry(e.getId(), e.getName(), e.getAmountLabel(), e.getTimeLabel(), e.getKcal(), e.getProteins(), e.getFats(), e.getCarbs());
    }

    private ApiModels.StatisticsSettings toSettings(StatisticsSettingsEntity e) {
        return new ApiModels.StatisticsSettings(e.getRetentionMonths(), e.getGoalKcal(), e.getWaterGoalMl(), e.getProteinGoalGrams(), e.getFatGoalGrams(), e.getCarbsGoalGrams());
    }

    private StatisticsSettingsEntity getOrCreateSettings(Long uid) {
        return settingsRepo.findByUserId(uid).orElseGet(() -> {
            var settings = new StatisticsSettingsEntity();
            settings.setUser(db.getUserEntity(uid));
            return settingsRepo.save(settings);
        });
    }

    private void prune(Long uid, int retentionMonths) {
        var cutoff = LocalDate.now().minusMonths(retentionMonths).withDayOfMonth(1);
        mealsRepo.deleteByUserIdAndDateBefore(uid, cutoff);
        daysRepo.deleteByUserIdAndDateBefore(uid, cutoff);
    }

    private int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    private BigDecimal scaled(BigDecimal value, BigDecimal factor) { return (value == null ? BigDecimal.ZERO : value).multiply(factor).setScale(2, RoundingMode.HALF_UP); }
    private String currentTimeLabel() { return java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")); }
    private BigDecimal nonNegative(BigDecimal value) {
        var validated = ServerValidation.nonNegativeDecimal(value, "Nutrient", ServerValidation.MAX_NUTRIENT);
        return validated == null ? BigDecimal.ZERO : validated;
    }
    private String cleanType(String type) {
        if (type == null) return "SNACK";
        return switch (type.toUpperCase()) {
            case "BREAKFAST", "LUNCH", "DINNER", "SNACK" -> type.toUpperCase();
            default -> "SNACK";
        };
    }
    private Long requireUid(HttpSession session) {
        var uid = (Long) session.getAttribute("uid");
        if (uid == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        return uid;
    }
}
