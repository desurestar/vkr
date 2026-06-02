CREATE TABLE shopping_list (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id),
    name VARCHAR(255) NOT NULL
);

INSERT INTO shopping_list (user_id, name)
SELECT DISTINCT user_id, 'Мой список'
FROM shopping_item;

ALTER TABLE shopping_item ADD COLUMN list_id BIGINT;

UPDATE shopping_item si
SET list_id = sl.id
FROM shopping_list sl
WHERE sl.user_id = si.user_id;

ALTER TABLE shopping_item
    ALTER COLUMN list_id SET NOT NULL,
    ADD CONSTRAINT fk_shopping_item_list FOREIGN KEY (list_id) REFERENCES shopping_list(id) ON DELETE CASCADE;
