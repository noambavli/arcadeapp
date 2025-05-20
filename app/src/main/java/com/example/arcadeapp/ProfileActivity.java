package com.example.arcadeapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
    private TextView usernameText, scoreText;
    private Button scoreboardButton, backButton;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        usernameText = findViewById(R.id.usernameText);
        scoreText = findViewById(R.id.scoreText);
        scoreboardButton = findViewById(R.id.scoreboardButton);
        backButton = findViewById(R.id.backButton);

        // Initialize TokenManager
        tokenManager = new TokenManager(this);

        // Set username
        String username = tokenManager.getUsername();
        usernameText.setText(username);

        // Set up button click listeners
        backButton.setOnClickListener(v -> finish());

        scoreboardButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ScoreboardActivity.class);
            startActivity(intent);
        });

        // Fetch fresh score
        fetchScore();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Fetch fresh score when activity resumes
        fetchScore();
    }

    private void fetchScore() {
        ServerUtils.getUserScore(this, new ServerUtils.ServerCallback<Integer>() {
            @Override
            public void onSuccess(Integer score) {
                runOnUiThread(() -> scoreText.setText("Score: " + score));
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> scoreText.setText("Error loading score"));
            }
        });
    }
}
