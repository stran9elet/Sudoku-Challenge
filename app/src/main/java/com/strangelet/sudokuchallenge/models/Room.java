package com.strangelet.sudokuchallenge.models;

import com.strangelet.sudokuchallenge.utils.OpponentSudoku;

import java.io.Serializable;

public class Room implements Serializable {

    private String roomCode;
    private ProjectedUser creator;
    private ProjectedUser sidekick;
    private int difficulty;
    private OpponentSudoku creatorSudoku;
    private OpponentSudoku sidekickSudoku;
//    public boolean creatorHasLoaded = false;
//    public boolean sidekickHasLoaded = false;
    private long timestamp;

    public Room(){
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public ProjectedUser getCreator() {
        return creator;
    }

    public void setCreator(ProjectedUser creator) {
        this.creator = creator;
    }

    public ProjectedUser getSidekick() {
        return sidekick;
    }

    public void setSidekick(ProjectedUser sidekick) {
        this.sidekick = sidekick;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}