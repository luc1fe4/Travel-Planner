CREATE TABLE itinerary_days (
    id BIGSERIAL PRIMARY KEY,
    trip_id BIGINT NOT NULL, 
    day_date DATE NOT NULL,
    UNIQUE(trip_id, day_date)
);

CREATE TABLE itinerary_items (
    id BIGSERIAL PRIMARY KEY,
    day_id BIGINT NOT NULL REFERENCES itinerary_days(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    location_name VARCHAR(255),
    lat DOUBLE PRECISION,
    lng DOUBLE PRECISION,
    sequence_order INT NOT NULL,
    start_time TIME
);
