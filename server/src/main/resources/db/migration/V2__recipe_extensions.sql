CREATE TABLE tag (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    label VARCHAR(100),
    color VARCHAR(7)
);

CREATE TABLE post_tag (
    post_id BIGINT NOT NULL REFERENCES post(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, tag_id)
);

CREATE TABLE recipe_ingredient (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES post(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    amount NUMERIC,
    unit VARCHAR(50)
);

CREATE TABLE recipe_step (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES post(id) ON DELETE CASCADE,
    step_number INT NOT NULL,
    description TEXT NOT NULL,
    image_url VARCHAR(255),
    CONSTRAINT uq_recipe_step UNIQUE (post_id, step_number)
);
