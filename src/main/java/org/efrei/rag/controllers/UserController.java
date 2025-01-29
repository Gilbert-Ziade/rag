package org.efrei.rag.controllers;


import org.efrei.rag.entities.MyUser;
import org.efrei.rag.repositories.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/return-text/{text}")
    public String returnText(@PathVariable String text) {
        return "you wrote : " + text;
    }

    @PostMapping("create-user")
    public String createUser(@RequestBody MyUser user) {
        userRepository.save(user);
        return "User created";
    }

    @GetMapping("get-user/{id}")
    public MyUser getUser(@PathVariable Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @GetMapping("/users")
    public List<MyUser> getUsers() {
        return userRepository.findAll();
    }
}
