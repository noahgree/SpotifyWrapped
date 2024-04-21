package com.example.spotifywrapped;

public class ScoreEntry implements Comparable<ScoreEntry> {
    private int score;
    private String key;

    public ScoreEntry(String key) {
        this.key = key;
        this.score = Integer.parseInt(key.split("_")[0]);
    }

    public int getScore() {
        return score;
    }

    public String getKey() {
        return key;
    }

    @Override
    public int compareTo(ScoreEntry other) {
        int scoreCompare = Integer.compare(other.score, this.score); // Descending order
        if (scoreCompare == 0) {
            return this.key.compareTo(other.key); // Compare UUIDs if scores are equal
        }
        return scoreCompare;
    }
}