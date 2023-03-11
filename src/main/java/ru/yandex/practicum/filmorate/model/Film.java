package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;

@Data
@Builder
public class Film {
    @EqualsAndHashCode.Exclude
    private Integer id;
    //@NotBlank(message = "Название не может быть пустым")
    private String name;
    //@Size(max = 200, message = "Максимальная длина описания 200 символов")
    private String description;
    @PastOrPresent(message = "Некорректная дата релиза")
    private LocalDate releaseDate;
    //@Positive(message = "Некорректная продолжительность фильма")
    private Integer duration;
    @EqualsAndHashCode.Exclude
    private final Set<User> likes = new HashSet<>();
    @EqualsAndHashCode.Exclude
    private final Set<Genre> genres = new TreeSet<>();
    @EqualsAndHashCode.Exclude
    @NotNull
    private Mpa mpa;

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("name", name);
        values.put("description", description);
        values.put("release_date", releaseDate);
        values.put("duration", duration);
        values.put("mpa", mpa.getId());
        return values;
    }
}
