package com.example.arcadeapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView usernameText, scoreText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        usernameText = findViewById(R.id.usernameText);  // Correct reference
        scoreText = findViewById(R.id.scoreText);  // Correct reference

        // Get the saved username from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "Guest");

        // Get the saved score from SharedPreferences
        int score = loadScore(username);

        // Display the username and score
        usernameText.setText("Username: " + username);
        scoreText.setText("Score: " + score);
    }

    private int loadScore(String username) {
        SharedPreferences prefs = getSharedPreferences("AppData", MODE_PRIVATE);
        return prefs.getInt(username + "_score", 0); // Default to 0 if no score is saved
    }
}

