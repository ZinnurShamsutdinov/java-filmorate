package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Sql(scripts = {"file:src/main/resources/schema.sql"})
public class MpaControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getMpa() throws Exception {
        //given
        Mpa expectGenre = new Mpa(1, "G");
        //when
        mockMvc.perform(get("/mpa/1")).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(expectGenre, objectMapper.readValue(result.getResponse().getContentAsString()
                                , Mpa.class), "Жанры не совпадают")
                );
    }

    @Test
    public void getMpaUnknown() throws Exception {
        //when
        mockMvc.perform(get("/mpa/134663")).andDo(print())
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
    public void getMpaAll() throws Exception {
        //when
        mockMvc.perform(get("/mpa")).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(5, objectMapper.readValue(result.getResponse().getContentAsString()
                                , new TypeReference<ArrayList<Mpa>>() {
                                }).size(), "Фильмы не совпадают")
                );
    }
}
