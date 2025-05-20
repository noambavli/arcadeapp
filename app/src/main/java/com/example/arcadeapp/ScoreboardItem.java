package com.example.arcadeapp;

public class ScoreboardItem {
    private String username;
    private int score;

    public ScoreboardItem(String username, int score) {
        this.username = username;
        this.score = score;
    }

    public String getUsername() {
        return username;
    }

    public int getScore() {
        return score;
    }
} 