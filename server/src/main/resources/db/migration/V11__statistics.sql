CREATE TABLE statistics_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES app_user(id) ON DELETE CASCADE,
    retention_months INT NOT NULL DEFAULT 3,
    goal_kcal INT NOT NULL DEFAULT 2000,
    water_goal_ml INT NOT NULL DEFAULT 1500,
    protein_goal_grams INT NOT NULL DEFAULT 90,
    fat_goal_grams INT NOT NULL DEFAULT 70,
    carbs_goal_grams INT NOT NULL DEFAULT 250
);

CREATE TABLE statistics_day (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    day_date DATE NOT NULL,
    water_consumed_ml INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_statistics_day_user_date UNIQUE (user_id, day_date)
);

CREATE TABLE statistics_meal_entry (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    day_date DATE NOT NULL,
    type VARCHAR(32) NOT NULL,
    name VARCHAR(255) NOT NULL,
    amount_label VARCHAR(64) NOT NULL,
    time_label VARCHAR(16) NOT NULL,
    kcal INT NOT NULL,
    proteins NUMERIC(10, 2) NOT NULL,
    fats NUMERIC(10, 2) NOT NULL,
    carbs NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_statistics_meal_user_date ON statistics_meal_entry(user_id, day_date);
