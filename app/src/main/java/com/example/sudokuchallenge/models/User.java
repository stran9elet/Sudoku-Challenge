package com.example.sudokuchallenge.models;

import java.io.Serializable;

public class User implements Serializable {
    private String nickName;
    private String email;
    private int credits;
    private String uid;

    private static User currentUser;

    public User(){
    }

    public User(String nickName, int credits){
        this.nickName = nickName;
        this.credits = credits;
    }

    public String getNickName(){
        return nickName;
    }

    public int getCredits(){
        return credits;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public static void setCurrentUser(User user){
        currentUser = user;
    }

    public static User getCurrentUser(){
        return currentUser;
    }
}
