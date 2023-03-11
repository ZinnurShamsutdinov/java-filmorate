package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User addUser(@Valid @RequestBody User user) {
        return userService.addUser(user);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public User updateUser(@Valid @RequestBody User user) {
        return userService.updateUser(user);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("{id}")
    public User getUser(@PathVariable("id") Integer userId) {
        return userService.getUser(userId);
    }

    @PutMapping("{id}/friends/{friendId}")
    public void addFriend(@PathVariable("id") Integer userId, @PathVariable Integer friendId) {
        userService.addFriend(userId, friendId);
    }

    @DeleteMapping("{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable("id") Integer userId, @PathVariable Integer friendId) {
        userService.deleteFriend(userId, friendId);
    }

    @GetMapping("{id}/friends")
    public List<User> getUserFriends(@PathVariable("id") Integer userId) {

        return userService.getFriends(userId);
    }

    @GetMapping("{id}/friends/common/{friendId}")
    public List<User> getCommonFriends(@PathVariable("id") Integer userId, @PathVariable Integer friendId) {
        return userService.getCommonFriends(userId, friendId);
    }
}