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
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Sql(scripts = {"file:src/main/resources/schema.sql"})
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private static final LocalDate brithDay = LocalDate.of(1985, 02, 15);

    @Test
    public void addAndGetUser() throws Exception {
        //given
        User user1 = User.builder().email("simple@email.ru").login("user_login").name("name").birthday(brithDay)
                .build();
        user1.setId(1);
        List<User> expectFilms = new ArrayList<>(List.of(user1));

        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isCreated(),
                        result -> assertEquals(user1, objectMapper.readValue(result.getResponse().getContentAsString()
                                , User.class), "Пользователи не совпадают")
                );
        //when
        mockMvc.perform(get("/users")).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(expectFilms, objectMapper.readValue(result.getResponse().getContentAsString()
                                , new TypeReference<ArrayList<User>>() {
                                }), "Пользователи не совпадают")
                );
    }

    @Test
    public void addDuplicateUser() throws Exception {
        //given
        User user1 = User.builder().id(1).email("simple@email.ru").login("user_login").name("name").birthday(brithDay)
                .build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
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
    public void addIncorrectLoginUser() throws Exception {
        //given
        User user1 = User.builder().id(1).email("simple@email.ru").login("").name("name").birthday(brithDay)
                .build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
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
    public void useLoginInsteadName() throws Exception {
        //given
        User user1 = User.builder().id(1).email("simple@email.ru").login("user_login").name("").birthday(brithDay)
                .build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.name").value("user_login")
                );
    }

    @Test
    public void addIncorrectBrithDay() throws Exception {
        //given
        User user1 = User.builder().id(1).email("simple@email.ru").login("user_login").name("user name")
                .birthday(brithDay.plusYears(1000)).build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
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
    public void addAndUpdateUser() throws Exception {
        //given
        User user1 = User.builder().email("simple@email.ru").login("user_login").name("name").birthday(brithDay)
                .build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isCreated(),
                        result -> assertEquals(user1, objectMapper.readValue(result.getResponse().getContentAsString()
                                , User.class), "Пользователи не совпадают")
                );
        //given
        user1.setEmail("simple2@email.ru");
        user1.setLogin("user_login2");
        user1.setId(1);
        List<User> expectUsers = new ArrayList<>(List.of(user1));
        //when
        mockMvc.perform(put("/users").content(objectMapper.writeValueAsString(user1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(user1, objectMapper.readValue(result.getResponse().getContentAsString()
                                , User.class), "Пользователи не совпадают")
                );
        //when
        mockMvc.perform(get("/users")).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(expectUsers, objectMapper.readValue(result.getResponse().getContentAsString()
                                , new TypeReference<ArrayList<User>>() {
                                }), "Пользователи не совпадают")
                );
    }

    @Test
    public void updateUnknownUser() throws Exception {
        //given
        User user1 = User.builder().id(1).email("simple@email.ru").login("user_login").name("name").birthday(brithDay)
                .build();

        //when
        mockMvc.perform(put("/users").content(objectMapper.writeValueAsString(user1))
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
    public void updateIncorrectLoginUser() throws Exception {
        //given
        User user1 = User.builder().id(1).email("simple@email.ru").login("user_login").name("name").birthday(brithDay)
                .build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        user1.setLogin("");
        mockMvc.perform(put("/users").content(objectMapper.writeValueAsString(user1))
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
    public void updateUseLoginInsteadName() throws Exception {
        //given
        User user1 = User.builder().id(1).email("simple@email.ru").login("user_login").name("name").birthday(brithDay)
                .build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        user1.setName("");
        mockMvc.perform(put("/users").content(objectMapper.writeValueAsString(user1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.name").value("user_login")
                );
    }

    @Test
    public void updateIncorrectBrithDay() throws Exception {
        //given
        User user1 = User.builder().id(1).email("simple@email.ru").login("user_login").name("user name")
                .birthday(brithDay).build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        user1.setBirthday(brithDay.plusYears(1000));
        mockMvc.perform(put("/users").content(objectMapper.writeValueAsString(user1))
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
    public void userGetCommonFriendsEmpty() throws Exception {
        //given
        User user1 = User.builder().email("simple@email.ru").login("user_login").name("user name")
                .birthday(brithDay).build();
        User user2 = User.builder().email("simple@email2.ru").login("user_login2").name("user name")
                .birthday(brithDay).build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user2))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(get("/users/1/friends/common/2")).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(0, objectMapper.readValue(result.getResponse()
                                .getContentAsString(), new TypeReference<ArrayList<User>>() {
                        }).size())
                );
    }

    @Test
    public void userAddAndFriend() throws Exception {
        //given
        User user1 = User.builder().email("simple@email.ru").login("user_login").name("user name")
                .birthday(brithDay).build();
        User user2 = User.builder().email("simple@email2.ru").login("user_login2").name("user name")
                .birthday(brithDay).build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user2))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(put("/users/1/friends/2")).andDo(print())
                //then
                .andExpectAll(
                        status().isOk()
                );
        //when
        user2.setId(2);
        List<User> exceptFriend = List.of(user2);
        mockMvc.perform(get("/users/1/friends")).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(exceptFriend, objectMapper.readValue(result.getResponse()
                                .getContentAsString(), new TypeReference<ArrayList<User>>() {
                        }))
                );

    }

    @Test
    public void userAddFriendUnknown() throws Exception {
        //given
        User user1 = User.builder().email("simple@email.ru").login("user_login").name("user name")
                .birthday(brithDay).build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(put("/users/1/friends/224324")).andDo(print())
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
    public void userGetCommonFriendEmpty() throws Exception {
        //given
        User user1 = User.builder().email("simple@email.ru").login("user_login").name("user name")
                .birthday(brithDay).build();
        User user2 = User.builder().email("simple@email2.ru").login("user_login2").name("user name")
                .birthday(brithDay).build();
        User user3 = User.builder().email("simple@email3.ru").login("user_login3").name("user name")
                .birthday(brithDay).build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user2))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user3))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(put("/users/1/friends/2")).andDo(print());
        mockMvc.perform(get("/users/3/friends/common/1")).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(0, objectMapper.readValue(result.getResponse()
                                .getContentAsString(), new TypeReference<ArrayList<User>>() {
                        }).size())
                );
    }

    @Test
    public void userGetCommonFriend() throws Exception {
        //given
        User user1 = User.builder().email("simple@email.ru").login("user_login").name("user name")
                .birthday(brithDay).build();
        User user2 = User.builder().email("simple@email2.ru").login("user_login2").name("user name")
                .birthday(brithDay).build();
        User user3 = User.builder().email("simple@email3.ru").login("user_login3").name("user name")
                .birthday(brithDay).build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user2))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user3))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(put("/users/1/friends/2")).andDo(print());
        mockMvc.perform(put("/users/3/friends/2")).andDo(print());
        user2.setId(2);
        mockMvc.perform(get("/users/3/friends/common/1")).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(List.of(user2), objectMapper.readValue(result.getResponse()
                                .getContentAsString(), new TypeReference<ArrayList<User>>() {
                        }))
                );
    }

    @Test
    public void userGetCommonFriendUnknown() throws Exception {
        //given
        User user1 = User.builder().email("simple@email.ru").login("user_login").name("user name")
                .birthday(brithDay).build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(get("/users/1/friends/common/13434")).andDo(print())
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
    public void userDeleteFriend() throws Exception {
        //given
        User user1 = User.builder().email("simple@email.ru").login("user_login").name("user name")
                .birthday(brithDay).build();
        User user2 = User.builder().email("simple@email2.ru").login("user_login2").name("user name")
                .birthday(brithDay).build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user2))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(put("/users/1/friends/2")).andDo(print());
        mockMvc.perform(delete("/users/1/friends/2")).andDo(print())
                //then
                .andExpect(status().isOk());
        //when
        mockMvc.perform(get("/users/1/friends")).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(0, objectMapper.readValue(result.getResponse()
                                .getContentAsString(), new TypeReference<ArrayList<User>>() {
                        }).size())
                );
    }
}
