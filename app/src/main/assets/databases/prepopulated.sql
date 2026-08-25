CREATE TABLE IF NOT EXISTS businesses (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT,
    type TEXT,
    description TEXT,
    longitude REAL NOT NULL,
    latitude REAL NOT NULL,
    hasChangingTable TEXT,
    changingTableLocation TEXT,
    hasDiaperPail INTEGER NOT NULL,
    isClean INTEGER NOT NULL,
    rating INTEGER NOT NULL,
    area TEXT,
    photoUrls TEXT NOT NULL,
    savedOnDevice INTEGER
);

INSERT INTO businesses (name, type, description, longitude, latitude, hasChangingTable, changingTableLocation, hasDiaperPail, isClean, rating, area, photoUrls, savedOnDevice)
VALUES ('The Green Beanery', 'cafe', NULL, -123.1727, 49.2595, 'yes', 'accessible', 1, 1, 4, 'Victoria', '[]', NULL);

INSERT INTO businesses (name, type, description, longitude, latitude, hasChangingTable, changingTableLocation, hasDiaperPail, isClean, rating, area, photoUrls, savedOnDevice)
VALUES ('Loaf & Ale', 'restaurant', NULL, -123.1735, 49.2588, 'yes', 'unisex', 1, 1, 5, 'Victoria', '[]', NULL);

INSERT INTO businesses (name, type, description, longitude, latitude, hasChangingTable, changingTableLocation, hasDiaperPail, isClean, rating, area, photoUrls, savedOnDevice)
VALUES ('Brew & Bloom', 'cafe', NULL, -123.1719, 49.2602, 'yes', 'female', 0, 1, 3, 'Victoria', '[]', NULL);


-- Generate a .db file from this file:
-- sqlite3 app/src/main/assets/databases/prepopulated.db < prepopulated.sql