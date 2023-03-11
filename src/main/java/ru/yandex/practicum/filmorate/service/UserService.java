package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.interfaces.UserStorage;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserStorage users;

    @Autowired
    public UserService(@Qualifier("UserDbStorage") UserStorage users) {
        this.users = users;
    }

    public User addUser(User user) {
        validate(user);
        users.add(user);
        log.info("Пользователь {} сохранен", user);
        return user;
    }

    public User updateUser(User user) {
        validate(user);
        users.update(user);
        log.info("Пользователь {} сохранен", user);
        return user;
    }


    private void validate(User user) {
        //   if ((user.getEmail() == null || user.getEmail().isBlank()) || !user.getEmail().contains("@")) {
       //     log.warn("Валидация не пройдена: email либо пустой, либо не содержит @.");
        //    throw new ValidationException("Адрес электронной почты не может быть пустым и должен содержать символ @.");
       // }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Валидация не пройдена: логин либо пустой, либо содержит пробелы.");
            throw new ValidationException("Логин не должен быть пустым и не должен содержать пробелы.");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Валидация не пройдена: дата рождения в будущем.");
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
    }


    public List<User> getUsers() {
        log.info("Текущее кол-во пользователей: " + users.getUsersList().size());
        return users.getUsersList();
    }

    public void addFriend(Integer userId, Integer friendId) throws ResponseStatusException {
        if (userId <=0 || friendId <= 0) {
            throw new NotFoundException( "id и friendId не могут быть отрицательныи либо равены 0");
        }
        users.addFriend(userId, friendId);
        log.info("Пользователь с id=" + userId + " добавил в друзья пользователя с id= " + friendId);
    }

    public void deleteFriend(Integer userId, Integer friendId) throws ResponseStatusException {
        if (userId <=0 || friendId <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "id и friendId не могут быть отрицательныи либо равены 0");
        }
        if (userId.equals(friendId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Невозможно удалить из друзей самого себя");
        }
        users.deleteFriend(userId, friendId);
        log.info("Пользователь с id=" + userId + " удалил пользователя с id=" + friendId);
    }

    public List<User> getCommonFriends(Integer userId, Integer friendId) throws ResponseStatusException {
        if (userId <=0 || friendId <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "id и friendId не могут быть отрицательныи либо равены 0");
        }
        if (userId.equals(friendId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Невозможно запросить общих друзей самого себя");
        }
        return  users.getCommonFriends(userId, friendId);
    }

    public List<User> getFriends(Integer friendId) throws ResponseStatusException {

        if (friendId <=0 ) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "id не может быть отрицательным либо равен 0");
        }

        return users.getFriends(friendId);
    }

    public User getUser(Integer userId) {
        if (userId <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "id не может быть отрицательным либо равен 0");
        }
        return users.getUser(userId);
    }
}