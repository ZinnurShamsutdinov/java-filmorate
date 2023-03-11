package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Sql(scripts = {"file:src/main/resources/schema.sql"})
public class FilmControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private static final LocalDate date = LocalDate.of(1895, 12, 29);

    private static final LocalDate brithDay = LocalDate.of(1993, 10, 27);

    @Test
    public void addAndGetFilm() throws Exception {
        //given
        Film film1 = Film.builder().name("film1").description("description").releaseDate(date).duration(60)
                .mpa(new Mpa(1, null)).build();
        List<Film> expectFilms = new ArrayList<>(List.of(film1));

        //when
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isCreated(),
                        result -> assertEquals(film1, objectMapper.readValue(result.getResponse().getContentAsString()
                                , Film.class), "Фильмы не совпадают")
                );
        //when
        mockMvc.perform(get("/films")).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(expectFilms, objectMapper.readValue(result.getResponse().getContentAsString()
                                , new TypeReference<ArrayList<Film>>() {
                                }), "Фильмы не совпадают")
                );
    }

    @Test
    public void addDuplicateFilm() throws Exception {
        //given
        Film film1 = Film.builder().id(1).name("film1").description("description").releaseDate(date).duration(60)
                .mpa(new Mpa(1, null)).build();

        //when
        //filmController.addFilm(film1);
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest(),
                        result -> {
                            assertNotNull(result.getResponse().getContentAsString()
                                    , "Отсутствует тело сообщения");
                            assertFalse(result.getResponse().getContentAsString().isBlank()
                                    , "Тело ответа с сообщением пустое");
                        }
                );
    }

    @Test
    public void addAndUpdateFilm() throws Exception {
        //given
        Film film1 = Film.builder().name("film1").description("description").releaseDate(date).duration(60)
                .mpa(new Mpa(1, null)).build();
        //when
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isCreated(),
                        result -> assertEquals(film1, objectMapper.readValue(result.getResponse().getContentAsString()
                                , Film.class), "Фильмы не совпадают")
                );
        //given
        film1.setName("update film1");
        film1.setDescription("update description");
        film1.setReleaseDate(date.plusYears(1));
        film1.setDuration(70);
        film1.setId(1);
        List<Film> expectFilms = new ArrayList<>(List.of(film1));
        //when
        mockMvc.perform(put("/films").content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(film1, objectMapper.readValue(result.getResponse().getContentAsString()
                                , Film.class), "Фильмы не совпадают")
                );
        mockMvc.perform(get("/films")).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(expectFilms, objectMapper.readValue(result.getResponse().getContentAsString()
                                , new TypeReference<ArrayList<Film>>() {
                                }), "Фильмы не совпадают")
                );
    }

    @Test
    public void updateUnknownFilm() throws Exception {
        //given
        Film film1 = Film.builder().id(1).name("film1").description("description").releaseDate(date).duration(60)
                .mpa(new Mpa(1, null)).build();

        //when
        mockMvc.perform(put("/films").content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isNotFound(),
                        result -> {
                            assertNotNull(result.getResponse().getContentAsString()
                                    , "Отсутствует тело сообщения");
                            assertFalse(result.getResponse().getContentAsString().isBlank()
                                    , "Тело ответа с сообщением пустое");
                        }
                );
    }

    @Test
    public void filmCreateFailMpa() throws Exception {
        //given
        Film film1 = Film.builder().name("film1").description("description")
                .releaseDate(date).duration(60).build();
        //when
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest(),
                        result -> {
                            assertNotNull(result.getResponse().getContentAsString()
                                    , "Отсутствует тело сообщения");
                            assertFalse(result.getResponse().getContentAsString().isBlank()
                                    , "Тело ответа с сообщением пустое");
                        }
                );
    }

    @Test
    public void getPopularFilmNoLikes() throws Exception {
        //given
        Film film1 = Film.builder().name("film1").description("description")
                .releaseDate(date).duration(60).mpa(new Mpa(1, null)).build();
        Film film2 = Film.builder().name("film2").description("description2")
                .releaseDate(date).duration(60).mpa(new Mpa(1, null)).build();
        //when
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film2))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(get("/films/popular").content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(List.of(film1, film2), objectMapper.readValue(result.getResponse().getContentAsString()
                                , new TypeReference<ArrayList<Film>>() {
                                }), "Фильмы не совпадают")
                );
    }

    @Test
    public void filmAddLike() throws Exception {
        Film film1 = Film.builder().name("film1").description("description")
                .releaseDate(date).duration(60).mpa(new Mpa(1, null)).build();
        User user1 = User.builder().email("simple@email.ru").login("user_login").name("name").birthday(brithDay)
                .build();
        //when
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(put("/films/1/like/1")).andDo(print())
                //then
                .andExpectAll(status().isOk());

    }

    @Test
    public void filmGetPopularCount2() throws Exception {
        //given
        Film film1 = Film.builder().name("film1").description("description")
                .releaseDate(date).duration(60).mpa(new Mpa(1, null)).build();
        Film film2 = Film.builder().name("film2").description("description2")
                .releaseDate(date).duration(60).mpa(new Mpa(1, null)).build();
        Film film3 = Film.builder().name("film3").description("description3")
                .releaseDate(date).duration(60).mpa(new Mpa(1, null)).build();
        User user1 = User.builder().email("simple@email.ru").login("user_login").name("name").birthday(brithDay)
                .build();
        //when
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film2))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film3))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(put("/films/2/like/1")).andDo(print());
        film1.setId(1);
        film1.setId(2);
        film1.setId(3);
        mockMvc.perform(get("/films/popular?count=2")).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(List.of(film2, film1), objectMapper.readValue(result.getResponse().getContentAsString()
                                , new TypeReference<ArrayList<Film>>() {
                                }), "Фильмы не совпадают")
                );

    }

    @Test
    public void filmRemoveLike() throws Exception {
        //given
        Film film1 = Film.builder().name("film1").description("description")
                .releaseDate(date).duration(60).mpa(new Mpa(1, null)).build();
        Film film2 = Film.builder().name("film2").description("description2")
                .releaseDate(date).duration(60).mpa(new Mpa(1, null)).build();
        User user1 = User.builder().email("simple@email.ru").login("user_login").name("name").birthday(brithDay)
                .build();
        //when
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film2))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(put("/films/2/like/1")).andDo(print());
        film1.setId(1);
        film2.setId(2);
        mockMvc.perform(delete("/films/2/like/1")).andDo(print());
        mockMvc.perform(get("/films/popular")).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(List.of(film1, film2), objectMapper.readValue(result.getResponse().getContentAsString()
                                , new TypeReference<ArrayList<Film>>() {
                                }), "Фильмы не совпадают")
                );
    }

    @Test
    public void filmRemoveLikeNotFound() throws Exception {
        //given
        Film film1 = Film.builder().name("film1").description("description")
                .releaseDate(date).duration(60).mpa(new Mpa(1, null)).build();
        //when
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(delete("/films/2/like/-1")).andDo(print())
                //then
                .andExpectAll(
                        status().isNotFound(),
                        result -> {
                            assertNotNull(result.getResponse().getContentAsString()
                                    , "Отсутствует тело сообщения");
                            assertFalse(result.getResponse().getContentAsString().isBlank()
                                    , "Тело ответа с сообщением пустое");
                        }
                );
    }

    @Test
    public void filmGenreUpdate() throws Exception {
        //given
        Film film1 = Film.builder().name("film1").description("description")
                .releaseDate(date).duration(60).mpa(new Mpa(1, null)).build();
        //when
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        film1.setId(1);
        film1.getGenres().add(new Genre(5, null));
        Set<Genre> expectedSetGenre = new TreeSet<>();
        expectedSetGenre.add(new Genre(5, "Документальный"));
        mockMvc.perform(put("/films").content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(expectedSetGenre, objectMapper.readValue(result.getResponse().getContentAsString()
                                , Film.class).getGenres(), "Фильмы не совпадают")
                );
    }

    @Test
    public void filmUpdateRemoveGenre() throws Exception {
        //given
        Film film1 = Film.builder().name("film1").description("description")
                .releaseDate(date).duration(60).mpa(new Mpa(1, null)).build();
        film1.getGenres().add(new Genre(1, null));
        //when
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        film1.getGenres().remove(new Genre(1, null));
        film1.setId(1);
        mockMvc.perform(put("/films").content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(0, objectMapper.readValue(result.getResponse().getContentAsString()
                                , Film.class).getGenres().size(), "Фильмы не совпадают")
                );

    }

    @Test
    public void filmUpdateGenresWithDuplicate() throws Exception {
        //given
        Film film1 = Film.builder().name("New film").releaseDate(LocalDate.of(1992, 4, 30))
                .description("New film about friends").duration(120).mpa(new Mpa(3, null)).build();
        String film1Update = "{\n" +
                "  \"id\": 1,\n" +
                "  \"name\": \"New film\",\n" +
                "  \"releaseDate\": \"1999-04-30\",\n" +
                "  \"description\": \"New film about friends\",\n" +
                "  \"duration\": 120,\n" +
                "  \"mpa\": { \"id\": 3},\n" +
                "  \"genres\": [{ \"id\": 1}, { \"id\": 2}, { \"id\": 1}]\n" +
                "}\n";
        Set<Genre> expectedSetGenres = new TreeSet<>();
        expectedSetGenres.add(new Genre(1, "Комедия"));
        expectedSetGenres.add(new Genre(2, "Драмма"));
        //when
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(put("/films").content(film1Update)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(expectedSetGenres, objectMapper.readValue(result.getResponse().getContentAsString()
                                , Film.class).getGenres(), "Фильмы не совпадают")
                );
    }
}
