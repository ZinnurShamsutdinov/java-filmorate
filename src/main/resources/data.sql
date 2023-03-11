INSERT INTO MPA (mpa_name) SELECT 'G'
WHERE NOT EXISTS (SELECT mpa_name FROM MPA WHERE mpa_name = 'G');

INSERT INTO MPA (mpa_name) SELECT 'PG'
WHERE NOT EXISTS (SELECT mpa_name FROM MPA WHERE mpa_name = 'PG');

INSERT INTO MPA (mpa_name) SELECT 'PG-13'
WHERE NOT EXISTS (SELECT mpa_name FROM MPA WHERE mpa_name = 'PG-13');

INSERT INTO MPA (mpa_name) SELECT 'R'
WHERE NOT EXISTS (SELECT mpa_name FROM MPA WHERE mpa_name = 'R');

INSERT INTO MPA (mpa_name) SELECT 'NC-17'
WHERE NOT EXISTS (SELECT mpa_name FROM MPA WHERE mpa_name = 'NC-17');

INSERT INTO GENRE (genre_name) SELECT 'Комедия'
WHERE NOT EXISTS (SELECT genre_name FROM GENRE WHERE genre_name = 'Комедия');

INSERT INTO GENRE (genre_name) SELECT 'Драма'
WHERE NOT EXISTS (SELECT genre_name FROM GENRE WHERE genre_name = 'Драма');

INSERT INTO GENRE (genre_name) SELECT 'Мультфильм'
WHERE NOT EXISTS (SELECT genre_name FROM GENRE WHERE genre_name = 'Мультфильм');

INSERT INTO GENRE (genre_name) SELECT 'Триллер'
WHERE NOT EXISTS (SELECT genre_name FROM GENRE WHERE genre_name = 'Триллер');

INSERT INTO GENRE (genre_name) SELECT 'Документальный'
WHERE NOT EXISTS (SELECT genre_name FROM GENRE WHERE genre_name = 'Документальный');

INSERT INTO GENRE (genre_name) SELECT 'Боевик'
WHERE NOT EXISTS (SELECT genre_name FROM GENRE WHERE genre_name = 'Боевик');
