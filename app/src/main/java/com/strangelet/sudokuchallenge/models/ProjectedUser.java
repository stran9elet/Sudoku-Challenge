package com.strangelet.sudokuchallenge.models;

public class ProjectedUser {

    private String uid;
    private String nickname;

    public ProjectedUser(){
    }

    public ProjectedUser(String uid, String nickname) {
        this.uid = uid;
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
