package com.example.arcadeapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {

    private Button profileButton, scoreboardButton, gamesHomeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        profileButton = findViewById(R.id.profileButton);
        scoreboardButton = findViewById(R.id.scoreboardButton);
        gamesHomeButton = findViewById(R.id.gamesHomeButton);

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        scoreboardButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ScoreboardActivity.class);
            startActivity(intent);
        });

        gamesHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, GamesHomeActivity.class);
            startActivity(intent);
        });
    }
}

