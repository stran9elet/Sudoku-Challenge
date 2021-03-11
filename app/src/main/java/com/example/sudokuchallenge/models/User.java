package com.example.sudokuchallenge.models;

import java.io.Serializable;

public class User implements Serializable {
    private String nickName;
//    private String email;
    private int credits;
    private String uid;

    private int dailyChallengesCompleted;
    private int dailyChallengeStreak;
    private int dailyChallengeBestStreak;

    private int easyGamesPlayed;
    private int easyGamesCompleted;
    private int easyAvgTimeTaken;
    private int easyBestTimeTaken;

    private int mediumGamesPlayed;
    private int mediumGamesCompleted;
    private int mediumAvgTimeTaken;
    private int mediumBestTimeTaken;

    private int difficultGamesPlayed;
    private int difficultGamesCompleted;
    private int difficultAvgTimeTaken;
    private int difficultBestTimeTaken;

    private int timeAttackGamesPlayed;
    private int timeAttackGamesCompleted;
    private int timeAttackAvgTimeTaken;

    public int getTimeAttackGamesPlayed() {
        return timeAttackGamesPlayed;
    }

    public void setTimeAttackGamesPlayed(int timeAttackGamesPlayed) {
        this.timeAttackGamesPlayed = timeAttackGamesPlayed;
    }

    public int getTimeAttackGamesCompleted() {
        return timeAttackGamesCompleted;
    }

    public void setTimeAttackGamesCompleted(int timeAttackGamesCompleted) {
        this.timeAttackGamesCompleted = timeAttackGamesCompleted;
    }

    public int getTimeAttackAvgTimeTaken() {
        return timeAttackAvgTimeTaken;
    }

    public void setTimeAttackAvgTimeTaken(int timeAttackAvgTimeTaken) {
        this.timeAttackAvgTimeTaken = timeAttackAvgTimeTaken;
    }

    public int getTimeAttackBestTimeTaken() {
        return timeAttackBestTimeTaken;
    }

    public void setTimeAttackBestTimeTaken(int timeAttackBestTimeTaken) {
        this.timeAttackBestTimeTaken = timeAttackBestTimeTaken;
    }

    private int timeAttackBestTimeTaken;


    private int mpTotGamesPlayed;
    private int mpTotGamesWon;
    private int mpWinStreak;

    private int mpEasyGamesPlayed;
    private int mpEasyGamesCompleted;
    private int mpEasyAvgTimeTaken;
    private int mpEasyBestTimeTaken;

    private int mpMediumGamesPlayed;
    private int mpMediumGamesCompleted;
    private int mpMediumAvgTimeTaken;
    private int mpMediumBestTimeTaken;

    private int mpDifficultGamesPlayed;
    private int mpDifficultGamesCompleted;
    private int mpDifficultAvgTimeTaken;
    private int mpDifficultBestTimeTaken;

    public User(){
    }

    public User(String nickName, int credits){
        this.nickName = nickName;
        this.credits = credits;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public int getDailyChallengesCompleted() {
        return dailyChallengesCompleted;
    }

    public void setDailyChallengesCompleted(int dailyChallengesCompleted) {
        this.dailyChallengesCompleted = dailyChallengesCompleted;
    }

    public int getDailyChallengeStreak() {
        return dailyChallengeStreak;
    }

    public void setDailyChallengeStreak(int dailyChallengeStreak) {
        this.dailyChallengeStreak = dailyChallengeStreak;
    }

    public int getEasyGamesPlayed() {
        return easyGamesPlayed;
    }

    public void setEasyGamesPlayed(int easyGamesPlayed) {
        this.easyGamesPlayed = easyGamesPlayed;
    }

    public int getEasyGamesCompleted() {
        return easyGamesCompleted;
    }

    public void setEasyGamesCompleted(int easyGamesCompleted) {
        this.easyGamesCompleted = easyGamesCompleted;
    }

    public int getEasyAvgTimeTaken() {
        return easyAvgTimeTaken;
    }

    public void setEasyAvgTimeTaken(int easyAvgTimeTaken) {
        this.easyAvgTimeTaken = easyAvgTimeTaken;
    }

    public int getEasyBestTimeTaken() {
        return easyBestTimeTaken;
    }

    public void setEasyBestTimeTaken(int easyBestTimeTaken) {
        this.easyBestTimeTaken = easyBestTimeTaken;
    }

    public int getMediumGamesPlayed() {
        return mediumGamesPlayed;
    }

    public void setMediumGamesPlayed(int mediumGamesPlayed) {
        this.mediumGamesPlayed = mediumGamesPlayed;
    }

    public int getMediumGamesCompleted() {
        return mediumGamesCompleted;
    }

    public void setMediumGamesCompleted(int mediumGamesCompleted) {
        this.mediumGamesCompleted = mediumGamesCompleted;
    }

    public int getMediumAvgTimeTaken() {
        return mediumAvgTimeTaken;
    }

    public void setMediumAvgTimeTaken(int mediumAvgTimeTaken) {
        this.mediumAvgTimeTaken = mediumAvgTimeTaken;
    }

    public int getMediumBestTimeTaken() {
        return mediumBestTimeTaken;
    }

    public void setMediumBestTimeTaken(int mediumBestTimeTaken) {
        this.mediumBestTimeTaken = mediumBestTimeTaken;
    }

    public int getDifficultGamesPlayed() {
        return difficultGamesPlayed;
    }

    public void setDifficultGamesPlayed(int difficultGamesPlayed) {
        this.difficultGamesPlayed = difficultGamesPlayed;
    }

    public int getDifficultGamesCompleted() {
        return difficultGamesCompleted;
    }

    public void setDifficultGamesCompleted(int difficultGamesCompleted) {
        this.difficultGamesCompleted = difficultGamesCompleted;
    }

    public int getDifficultAvgTimeTaken() {
        return difficultAvgTimeTaken;
    }

    public void setDifficultAvgTimeTaken(int difficultAvgTimeTaken) {
        this.difficultAvgTimeTaken = difficultAvgTimeTaken;
    }

    public int getDifficultBestTimeTaken() {
        return difficultBestTimeTaken;
    }

    public void setDifficultBestTimeTaken(int difficultBestTimeTaken) {
        this.difficultBestTimeTaken = difficultBestTimeTaken;
    }

    public int getMpTotGamesPlayed() {
        return mpTotGamesPlayed;
    }

    public void setMpTotGamesPlayed(int mpTotGamesPlayed) {
        this.mpTotGamesPlayed = mpTotGamesPlayed;
    }

    public int getMpTotGamesWon() {
        return mpTotGamesWon;
    }

    public void setMpTotGamesWon(int mpTotGamesWon) {
        this.mpTotGamesWon = mpTotGamesWon;
    }

    public int getMpWinStreak() {
        return mpWinStreak;
    }

    public void setMpWinStreak(int mpWinStreak) {
        this.mpWinStreak = mpWinStreak;
    }

    public int getMpEasyGamesPlayed() {
        return mpEasyGamesPlayed;
    }

    public void setMpEasyGamesPlayed(int mpEasyGamesPlayed) {
        this.mpEasyGamesPlayed = mpEasyGamesPlayed;
    }

    public int getMpEasyGamesCompleted() {
        return mpEasyGamesCompleted;
    }

    public void setMpEasyGamesCompleted(int mpEasyGamesCompleted) {
        this.mpEasyGamesCompleted = mpEasyGamesCompleted;
    }

    public int getMpEasyAvgTimeTaken() {
        return mpEasyAvgTimeTaken;
    }

    public void setMpEasyAvgTimeTaken(int mpEasyAvgTimeTaken) {
        this.mpEasyAvgTimeTaken = mpEasyAvgTimeTaken;
    }

    public int getMpEasyBestTimeTaken() {
        return mpEasyBestTimeTaken;
    }

    public void setMpEasyBestTimeTaken(int mpEasyBestTimeTaken) {
        this.mpEasyBestTimeTaken = mpEasyBestTimeTaken;
    }

    public int getMpMediumGamesPlayed() {
        return mpMediumGamesPlayed;
    }

    public void setMpMediumGamesPlayed(int mpMediumGamesPlayed) {
        this.mpMediumGamesPlayed = mpMediumGamesPlayed;
    }

    public int getMpMediumGamesCompleted() {
        return mpMediumGamesCompleted;
    }

    public void setMpMediumGamesCompleted(int mpMediumGamesCompleted) {
        this.mpMediumGamesCompleted = mpMediumGamesCompleted;
    }

    public int getMpMediumAvgTimeTaken() {
        return mpMediumAvgTimeTaken;
    }

    public void setMpMediumAvgTimeTaken(int mpMediumAvgTimeTaken) {
        this.mpMediumAvgTimeTaken = mpMediumAvgTimeTaken;
    }

    public int getMpMediumBestTimeTaken() {
        return mpMediumBestTimeTaken;
    }

    public void setMpMediumBestTimeTaken(int mpMediumBestTimeTaken) {
        this.mpMediumBestTimeTaken = mpMediumBestTimeTaken;
    }

    public int getMpDifficultGamesPlayed() {
        return mpDifficultGamesPlayed;
    }

    public void setMpDifficultGamesPlayed(int mpDifficultGamesPlayed) {
        this.mpDifficultGamesPlayed = mpDifficultGamesPlayed;
    }

    public int getMpDifficultGamesCompleted() {
        return mpDifficultGamesCompleted;
    }

    public void setMpDifficultGamesCompleted(int mpDifficultGamesCompleted) {
        this.mpDifficultGamesCompleted = mpDifficultGamesCompleted;
    }

    public int getMpDifficultAvgTimeTaken() {
        return mpDifficultAvgTimeTaken;
    }

    public void setMpDifficultAvgTimeTaken(int mpDifficultAvgTimeTaken) {
        this.mpDifficultAvgTimeTaken = mpDifficultAvgTimeTaken;
    }

    public int getMpDifficultBestTimeTaken() {
        return mpDifficultBestTimeTaken;
    }

    public void setMpDifficultBestTimeTaken(int mpDifficultBestTimeTaken) {
        this.mpDifficultBestTimeTaken = mpDifficultBestTimeTaken;
    }

    public String getNickName(){
        return nickName;
    }

    public int getCredits(){
        return credits;
    }

//    public void setEmail(String email){
//        this.email = email;
//    }

//    public String getEmail() {
//        return email;
//    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getDailyChallengeBestStreak() {
        return dailyChallengeBestStreak;
    }

    public void setDailyChallengeBestStreak(int dailyChallengeBestStreak) {
        this.dailyChallengeBestStreak = dailyChallengeBestStreak;
    }
}
