package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.interfaces.GenreStorage;

import java.util.List;

@Service
public class GenreService {
    private final GenreStorage genres;

    @Autowired
    public GenreService(GenreStorage genres) {
        this.genres = genres;
    }

    public Genre getGenre(Integer id) {
        return genres.getGenre(id);
    }

    public List<Genre> getGenreList() {
        return genres.getGenresList();
    }
}
