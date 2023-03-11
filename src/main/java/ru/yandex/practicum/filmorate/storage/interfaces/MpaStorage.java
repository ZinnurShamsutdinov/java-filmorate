package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.*;

import java.util.List;

public interface MpaStorage {
    Mpa getMpa(Integer id);

    List<Mpa> getMpasList();
}
