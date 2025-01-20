package com.example.arcadeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ExampleGameActivity extends AppCompatActivity {

    private Button playButton;
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example_game);

        playButton = findViewById(R.id.playButton);

        playButton.setOnClickListener(v -> {
            // Simulate scoring
            score = (int) (Math.random() * 100);  // Random score between 0 and 100

            // Save the score to SharedPreferences under the current username
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String username = prefs.getString("username", "Guest");

            // Save the score for this user
            SharedPreferences.Editor editor = getSharedPreferences("AppData", MODE_PRIVATE).edit();
            editor.putInt(username + "_score", score);
            editor.apply();

            // Return the score to the calling activity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("score", score);
            setResult(RESULT_OK, resultIntent);
            finish();  // Close the game activity
        });
    }
}

