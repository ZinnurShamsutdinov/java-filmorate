package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.interfaces.FilmStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Repository("FilmDbStorage")
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(Film film) throws ResponseStatusException {
        if (dbContainsFilm(film)) {
            log.warn("Такой фильм уже есть");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Такой фильм уже есть");
        }
        Integer filmId = addFilmInfo(film);
        film.setId(filmId);
        String sqlQuery = "INSERT into genre_films (film_id, genre_id) "
                + " VALUES (?, ?)";
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(sqlQuery,
                        filmId,
                        genre.getId()
                );
            }
        }
    }

    @Override
    public Film update (Film film) {
        String SQL_DELETE_FROM_GENRE_FILMS = "DELETE FROM genre_films WHERE film_id = ?";

        String sqlQuery = "UPDATE film " +
                "SET name = ?, description = ?, release_date = ?, duration = ?, mpa = ? WHERE film_id = ?";
        if (jdbcTemplate.update(sqlQuery, film.getName(), film.getDescription(), film.getReleaseDate()
                , film.getDuration(), film.getMpa().getId(), film.getId()) == 0) {
            throw new NotFoundException("Фильма с id=" + film.getId() + " нет");
        }
        if (film.getGenres().size() == 0) {
            String sqlQuery2 = SQL_DELETE_FROM_GENRE_FILMS;
            jdbcTemplate.update(sqlQuery2, film.getId());
        }
        if (film.getGenres() != null && film.getGenres().size() != 0) {
            String sqlQuery2 = SQL_DELETE_FROM_GENRE_FILMS;
            jdbcTemplate.update(sqlQuery2, film.getId());
            String sqlQuery3 = "INSERT INTO genre_films (film_id, genre_id) VALUES (?, ?)";
            film.getGenres().forEach(genre -> jdbcTemplate.update(sqlQuery3, film.getId(), genre.getId()));
        }
        Film film2 = getFilm(film.getId());
        return film2;
    }

    public List<Film> getFilmsList() {
        String sqlQuery = "SELECT film.*, mpa.mpa_name FROM film JOIN mpa ON film.mpa = mpa.mpa_id";
        return jdbcTemplate.query(sqlQuery, this::makeFilm);
    }


    @Override
    public Film getFilm(Integer id) {
        String sqlQuery = "SELECT film_id, name, description, release_date, duration, film.mpa, mpa.mpa_name " +
                "FROM film JOIN MPA ON film.mpa = mpa.mpa_id WHERE film.film_id = ?";
        Film film;
        try {
            film = jdbcTemplate.queryForObject(sqlQuery, this::makeFilm, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Фильма с id=" + id + " нет");
        }
        return film;
    }

    @Override
    public void addLike(Integer userId, Integer filmId) throws  ResponseStatusException {
        if (!dbContainsUser(userId)) {
            String message = "Ошибка запроса добавления лайка фильму." +
                    " Невозможно поставить лайк от пользователя с id= " + userId + " которого не существует.";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        if (!dbContainsFilm(filmId)) {
            String message = "Ошибка запроса добавления лайка фильму." +
                    " Невозможно поставить лайк фильму с id= " + filmId + " которого не существует.";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        String sqlQuery = "INSERT INTO likes (person_id, film_id) VALUES (?, ?)";
        try {
            jdbcTemplate.update(sqlQuery, userId, filmId);
        } catch (DuplicateKeyException e ) {
            String message = "Ошибка запроса добавления лайка фильму." +
                    " Попытка полькователем поставить лайк дважды одному фильму.";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    @Override
    public void deleteLike(Integer userId, Integer filmId) {
        if (!dbContainsUser(userId)) {
            String message = "Ошибка запроса удаления лайка" +
                    " Невозможно удалить лайк от пользователя с id= " + userId + " которого не существует.";
            throw new NotFoundException(message);
        }
        if (!dbContainsFilm(filmId)) {
            String message = "Ошибка запроса удаления лайка" +
                    " Невозможно удалить лайк с фильма с id= " + filmId + " которого не существует.";
            throw new NotFoundException(message);
        }
        String sqlQuery = "DELETE FROM likes where person_id = ? AND film_id = ?";
        if (jdbcTemplate.update(sqlQuery, userId, filmId) == 0) {
            throw new NotFoundException("Лайка от пользователя с id=" + userId + " у фильма с id=" + filmId + " нет");
        }
    }

    private Film makeFilm(ResultSet resultSet, int rowSum) throws SQLException {
        Film film = Film.builder()
                .id(resultSet.getInt("film_id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getDate("release_date").toLocalDate())
                .duration(resultSet.getInt("duration"))
                .mpa(new Mpa(resultSet.getInt("mpa"), resultSet.getString("mpa_name")))
                .build();
        String sqlQuery = "SELECT person.* FROM likes JOIN person ON likes.person_id=person.person_id WHERE likes.film_id=?";
        film.getLikes().addAll(jdbcTemplate.query(sqlQuery, this::makeUser, film.getId()));
        film.getGenres().addAll(findGenresByFilmId(film.getId()));
        return film;
    }

    private int addFilmInfo(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("film")
                .usingGeneratedKeyColumns("film_id");
        return simpleJdbcInsert.executeAndReturnKey(film.toMap()).intValue();
    }

    private User makeUser(ResultSet resultSet, int rowNum) throws SQLException {
        return User.builder()
                .id(resultSet.getInt("person_id"))
                .email(resultSet.getString("email"))
                .login(resultSet.getString("login"))
                .name(resultSet.getString("name"))
                .birthday(resultSet.getDate("birthday").toLocalDate())
                .build();
    }

    private Set<Genre> findGenresByFilmId(Integer id) {
        String sqlQuery = "SELECT g.genre_id, g.genre_name FROM film AS f JOIN genre_films AS gf ON f.film_id=gf.film_id JOIN genre AS g ON gf.genre_id=g.genre_id WHERE f.film_id = ?";
        return new TreeSet<>(jdbcTemplate.query(sqlQuery, this::makeGenre, id));
    }

    private boolean dbContainsFilm(Film film) {
        String sqlQuery = "SELECT f.*, mpa.mpa_name FROM FILM AS f JOIN mpa ON f.mpa = mpa.mpa_id " +
                "WHERE f.name = ? AND  f.description = ? AND f.release_date = ? AND f.duration = ? AND f.mpa = ?";
        try {
            jdbcTemplate.queryForObject(sqlQuery, this::makeFilm, film.getName(), film.getDescription(),
                    film.getReleaseDate(), film.getDuration(), film.getMpa().getId());
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    private boolean dbContainsUser(Integer userId) {
        String sqlQuery = "SELECT * FROM person WHERE person_id = ?";
        try {
            jdbcTemplate.queryForObject(sqlQuery, this::makeUser, userId);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    private boolean dbContainsFilm(Integer filmId) {
        String sqlQuery = "SELECT f.*, mpa.mpa_name FROM FILM AS f JOIN mpa ON f.mpa = mpa.mpa_id " +
                "WHERE f.film_id = ?";
        try {
            jdbcTemplate.queryForObject(sqlQuery, this::makeFilm, filmId);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    private Genre makeGenre(ResultSet resultSet, int rowSum) throws SQLException {
        return new Genre(resultSet.getInt("genre_id"), resultSet.getString("genre_name"));
    }
}
