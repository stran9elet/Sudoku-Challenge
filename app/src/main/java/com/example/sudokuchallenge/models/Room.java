package com.example.sudokuchallenge.models;

import com.example.sudokuchallenge.utils.OpponentSudoku;

import java.io.Serializable;

public class Room implements Serializable {

    private String roomCode;
    private User creator;
    private User sidekick;
    private String roomId;
    private int difficulty;
    private OpponentSudoku creatorSudoku;
    private OpponentSudoku sidekickSudoku;
    public boolean creatorHasLoaded = false;
    public boolean sidekickHasLoaded = false;

    public Room(){
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public User getSidekick() {
        return sidekick;
    }

    public void setSidekick(User sidekick) {
        this.sidekick = sidekick;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public OpponentSudoku getCreatorSudoku() {
        return creatorSudoku;
    }

    public void setCreatorSudoku(OpponentSudoku creatorSudoku) {
        this.creatorSudoku = creatorSudoku;
    }

    public OpponentSudoku getSidekickSudoku() {
        return sidekickSudoku;
    }

    public void setSidekickSudoku(OpponentSudoku sidekickSudoku) {
        this.sidekickSudoku = sidekickSudoku;
    }
}