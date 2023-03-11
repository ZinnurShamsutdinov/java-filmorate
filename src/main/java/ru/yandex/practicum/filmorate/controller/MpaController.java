package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MpaController {
    private final MpaService mpaService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Mpa> getMpasList() {
        return mpaService.getMpasList();
    }

    @GetMapping("{id}")
    public Mpa getMpa(@PathVariable("id") Integer id) {
        return mpaService.getMpa(id);
    }
}
