package com.example.spotifywrapped.user;

public class User {
    private String email;
    private String password;
    private String USER_ID;
    public User(String email, String password, String USER_ID, String name) {
        this.email = email;
        this.password = password;
        this.USER_ID = USER_ID;
    }
}
