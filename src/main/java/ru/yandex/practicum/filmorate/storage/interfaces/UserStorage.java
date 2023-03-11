package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    void add(User user);

    void delete(User user);

    void update(User user);

    List<User> getUsersList();

    void addFriend(Integer userId, Integer friendId);

    void deleteFriend(Integer userId, Integer friendId);

    List<User> getCommonFriends(Integer userId, Integer friendId);

    List<User> getFriends(Integer friendId);

    User getUser(Integer userId);
}