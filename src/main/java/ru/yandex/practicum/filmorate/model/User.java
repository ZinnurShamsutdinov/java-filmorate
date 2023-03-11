package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class User {
    @EqualsAndHashCode.Exclude
    private Integer id;
    //@Email(message = "Адрес электронной почты не может быть пустым и должен содержать символ @.")
    private String email;
    //@NotBlank //(message = "Логин не может быть пустым или null")
    private String login;
    private String name;
   // @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
    private final Set<User> friends = new HashSet<>();

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("person_id", id);
        values.put("email", email);
        values.put("login", login);
        values.put("name", name);
        values.put("birthday", birthday);
        return values;
    }

}

