package org.efrei.rag.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class MyUser {

    @Id
    @GeneratedValue(generator = "increment")
    private Long id;

    private String name;

    private String email;

    private String password;

    private String content;

    public MyUser() {
    }

    public MyUser(Long id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getContent() {
        return content;
    }
}
