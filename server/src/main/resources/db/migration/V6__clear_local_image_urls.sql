UPDATE post
SET image_url = NULL
WHERE image_url ILIKE 'content://%' OR image_url ILIKE 'file://%';

UPDATE recipe_step
SET image_url = NULL
WHERE image_url ILIKE 'content://%' OR image_url ILIKE 'file://%';
