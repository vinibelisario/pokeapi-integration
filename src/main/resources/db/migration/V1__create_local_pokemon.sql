CREATE TABLE local_pokemon (
    id             INTEGER      PRIMARY KEY,
    name           VARCHAR(100) NOT NULL UNIQUE,
    image_url      VARCHAR(500),
    description    VARCHAR(1000),
    localized_name VARCHAR(100),
    region         VARCHAR(100),
    internal_tags  VARCHAR(500)
);