CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    bio VARCHAR(255),
    avatar_url VARCHAR(255)
);

CREATE TABLE post (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT NOT NULL REFERENCES app_user(id),
    type VARCHAR(32) NOT NULL,
    title VARCHAR(255) NOT NULL,
    summary VARCHAR(255),
    content TEXT,
    likes INT NOT NULL DEFAULT 0,
    cook_time_minutes INT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE comment (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT NOT NULL REFERENCES app_user(id),
    post_id BIGINT NOT NULL REFERENCES post(id),
    text VARCHAR(2048) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE post_tags (
    post_id BIGINT NOT NULL REFERENCES post(id),
    tag VARCHAR(255) NOT NULL
);

CREATE TABLE user_following (
    follower_id BIGINT NOT NULL REFERENCES app_user(id),
    following_id BIGINT NOT NULL REFERENCES app_user(id),
    PRIMARY KEY (follower_id, following_id)
);

CREATE TABLE shopping_item (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id),
    name VARCHAR(255) NOT NULL,
    amount VARCHAR(64) NOT NULL,
    checked BOOLEAN NOT NULL DEFAULT FALSE
);
