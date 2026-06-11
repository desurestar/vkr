package ru.zagrebin.server.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class ServerValidation {
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 50;
    public static final int MAX_TITLE_LENGTH = 120;
    public static final int MAX_SUMMARY_LENGTH = 512;
    public static final int MAX_CONTENT_LENGTH = 20_000;
    public static final int MAX_TAGS = 12;
    public static final int MAX_TAG_LENGTH = 40;
    public static final int MAX_INGREDIENTS = 100;
    public static final int MAX_STEPS = 50;
    public static final int MAX_COMMENT_LENGTH = 1_000;
    public static final int MAX_SHOPPING_NAME_LENGTH = 120;
    public static final int MAX_AMOUNT_LENGTH = 60;
    public static final int MAX_PROFILE_NAME_LENGTH = 80;
    public static final int MAX_BIO_LENGTH = 1_000;
    public static final int MAX_MEAL_LABEL_LENGTH = 80;
    public static final int MAX_TIME_LABEL_LENGTH = 16;
    public static final int MAX_WATER_PER_REQUEST_ML = 5_000;
    public static final int MAX_PORTION_GRAMS = 5_000;
    public static final int MAX_KCAL = 10_000;
    public static final BigDecimal MAX_NUTRIENT = BigDecimal.valueOf(2_000);

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_]{3,32}$");
    private static final Pattern TIME_LABEL = Pattern.compile("^(?:[01]\\d|2[0-3]):[0-5]\\d$");

    private ServerValidation() {
    }

    public record Page(int index, int size, boolean requested) {}

    public static <T> T requireBody(T body) {
        if (body == null) {
            throw badRequest("Request body is required");
        }
        return body;
    }

    public static String requiredText(String value, String fieldName, int maxLength) {
        var cleaned = optionalText(value, maxLength);
        if (cleaned == null) {
            throw badRequest(fieldName + " is required");
        }
        return cleaned;
    }

    public static String optionalText(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        var cleaned = value.trim().replaceAll("\\p{Cntrl}", "");
        if (cleaned.isBlank()) {
            return null;
        }
        if (cleaned.length() > maxLength) {
            throw badRequest("Value is too long. Maximum length is " + maxLength);
        }
        return cleaned;
    }

    public static String username(String value) {
        var cleaned = requiredText(value, "Username", 32).toLowerCase();
        if (!USERNAME.matcher(cleaned).matches()) {
            throw badRequest("Username must be 3-32 characters and contain only latin letters, digits and underscore");
        }
        return cleaned;
    }

    public static String email(String value) {
        var cleaned = requiredText(value, "Email", 254).toLowerCase();
        if (!cleaned.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw badRequest("Email is invalid");
        }
        return cleaned;
    }

    public static String password(String value, String fieldName) {
        if (value == null) {
            throw badRequest(fieldName + " is required");
        }
        if (value.length() < 6 || value.length() > 128) {
            throw badRequest(fieldName + " length must be 6-128 characters");
        }
        return value;
    }

    public static Page page(Integer page, Integer size) {
        if (page == null && size == null) {
            return new Page(0, DEFAULT_PAGE_SIZE, false);
        }
        var index = page == null ? 0 : page;
        if (index < 0) {
            throw badRequest("Page must be greater than or equal to 0");
        }
        var pageSize = size == null ? DEFAULT_PAGE_SIZE : size;
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw badRequest("Size must be between 1 and " + MAX_PAGE_SIZE);
        }
        return new Page(index, pageSize, true);
    }

    public static String searchQuery(String query) {
        return optionalText(query, 120);
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static Integer nonNegativeInt(Integer value, String fieldName, int max) {
        if (value == null) {
            return null;
        }
        if (value < 0 || value > max) {
            throw badRequest(fieldName + " must be between 0 and " + max);
        }
        return value;
    }

    public static BigDecimal nonNegativeDecimal(BigDecimal value, String fieldName, BigDecimal max) {
        if (value == null) {
            return null;
        }
        if (value.signum() < 0 || value.compareTo(max) > 0) {
            throw badRequest(fieldName + " must be between 0 and " + max.stripTrailingZeros().toPlainString());
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static List<String> tags(List<String> rawTags) {
        if (rawTags == null || rawTags.isEmpty()) {
            return List.of();
        }
        var result = new ArrayList<String>();
        for (var tag : rawTags) {
            var cleaned = optionalText(tag == null ? null : tag.replace("#", ""), MAX_TAG_LENGTH);
            if (cleaned != null && result.stream().noneMatch(existing -> existing.equalsIgnoreCase(cleaned))) {
                result.add(cleaned);
            }
        }
        if (result.size() > MAX_TAGS) {
            throw badRequest("Too many tags. Maximum is " + MAX_TAGS);
        }
        return List.copyOf(result);
    }

    public static LocalDate dateOrToday(LocalDate date) {
        return date == null ? LocalDate.now() : date;
    }

    public static String timeLabelOrDefault(String value, String defaultValue) {
        var cleaned = optionalText(value, MAX_TIME_LABEL_LENGTH);
        if (cleaned == null) {
            return defaultValue;
        }
        if (!TIME_LABEL.matcher(cleaned).matches()) {
            throw badRequest("Time must be in HH:mm format");
        }
        return cleaned;
    }

    public static String parseMonthOrThrow(String month) {
        var cleaned = optionalText(month, 7);
        if (cleaned == null) {
            return null;
        }
        try {
            java.time.YearMonth.parse(cleaned);
            return cleaned;
        } catch (DateTimeParseException e) {
            throw badRequest("Month must be in YYYY-MM format");
        }
    }

    public static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
