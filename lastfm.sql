--
-- Файл сгенерирован с помощью SQLiteStudio v3.0.3 в Чт Бер 26 20:40:28 2015
--
-- Использованная кодировка текста: UTF-8
--
PRAGMA foreign_keys = off;
BEGIN TRANSACTION;

-- Таблица: artist
DROP TABLE IF EXISTS artist;
CREATE TABLE artist (mbid STRING PRIMARY KEY NOT NULL UNIQUE, name STRING NOT NULL, image STRING, bio STRING, yearformed INT, placeformed STRING)

-- Таблица: top_tracks
DROP TABLE IF EXISTS top_tracks;
CREATE TABLE top_tracks (mbid STRING PRIMARY KEY NOT NULL UNIQUE, name STRING NOT NULL, playcount INT, listeners INT, image STRING)

-- Таблица: similar
DROP TABLE IF EXISTS similar;
CREATE TABLE similar (name STRING PRIMARY KEY UNIQUE NOT NULL, image STRING)

-- Таблица: member
DROP TABLE IF EXISTS member;
CREATE TABLE member (name STRING PRIMARY KEY UNIQUE NOT NULL, yearfrom INT)

COMMIT TRANSACTION;
