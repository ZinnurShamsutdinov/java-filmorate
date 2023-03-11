package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.*;

import java.util.List;

public interface GenreStorage {
    List<Genre> getGenresList();

    Genre getGenre(Integer id);
}
