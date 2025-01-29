package org.efrei.rag.controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/return-text/{text}")
    public String returnText(@PathVariable String text) {
        return "you wrote : " + text;
    }
}
