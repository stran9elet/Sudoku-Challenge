package com.example.sudokuchallenge.models;

public class RoomQueues {
    private String roomCode;
    private String creatorId;
    private int difficulty;

    public RoomQueues(){
    }

    public RoomQueues(String roomId, String creatorId, int difficulty) {
        roomCode = roomId;
        this.creatorId = creatorId;
        this.difficulty = difficulty;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
}
