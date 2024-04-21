package com.example.spotifywrapped.user;

import java.util.ArrayList;
import java.util.Map;

public class User {
    private String name;
    private String email;
    private String password;
    private String USER_ID;

    private ArrayList<Map<String, Object>> wraps;

    private String totalPoints;

    private String mAccessToken;

    public User(String email, String password, String USER_ID, String name, ArrayList<Map<String, Object>> wraps, String totalPoints) {
        this.email = email;
        this.password = password;
        this.USER_ID = USER_ID;
        this.name = name;
        this.wraps = wraps;
        this.totalPoints = totalPoints;
        //this.mAccessToken = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(String totalPoints) {
        this.totalPoints = totalPoints;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUSER_ID() {
        return USER_ID;
    }

    public void setUSER_ID(String USER_ID) {
        this.USER_ID = USER_ID;
    }

    public ArrayList<Map<String, Object>> getwraps() {
        return wraps;
    }

    public void addWrap(Map<String, Object> wrap) {
        this.wraps.add(wrap);
    }

    public String getmAccessToken() {
        return mAccessToken;
    }

    public void setmAccessToken(String mAccessToken) {
        this.mAccessToken = mAccessToken;
    }
}
