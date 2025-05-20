package com.example.arcadeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

public class GameTourActivity extends AppCompatActivity {
    private int currentGameIndex = 0;
    private final String[] gameNames = {
        "Tic Tac Toe",
        "Memory Match",
        "Trivia",
        "Guess Number",
        "Red Button",
        "Hangchairs",
        "Falling Blocks",
        "Snake"
    };
    
    private final String[] gameDescriptions = {
        "Classic two-player game where you try to get three in a row!",
        "Test your memory by matching pairs of cards.",
        "Challenge yourself with fun trivia questions!",
        "Try to guess the secret number!",
        "Quick reaction game - don't press the red button!",
        "Help the chairs hang on!",
        "Dodge the falling blocks!",
        "Classic snake game - eat food and grow longer!"
    };

    private final Class<?>[] gameActivities = {
        TicTacToeActivity.class,
        MemoryMatchActivity.class,
        triviaActivity.class,
        guessNumberActivity.class,
        RedButtonActivity.class,
        HangchairsActivity.class,
        FallingblocksActivity.class,
        SnakeActivity.class
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_tour);

        updateGameInfo();
        setupNavigationButtons();
    }

    private void updateGameInfo() {
        TextView gameNameText = findViewById(R.id.gameNameText);
        TextView gameDescriptionText = findViewById(R.id.gameDescriptionText);
        CardView gamePreviewCard = findViewById(R.id.gamePreviewCard);

        gameNameText.setText(gameNames[currentGameIndex]);
        gameDescriptionText.setText(gameDescriptions[currentGameIndex]);
        
        // Update preview image based on current game
        // This would need corresponding drawable resources
        int previewImageId = getResources().getIdentifier(
            "preview_" + gameNames[currentGameIndex].toLowerCase().replace(" ", "_"),
            "drawable",
            getPackageName()
        );
        if (previewImageId != 0) {
            gamePreviewCard.setBackgroundResource(previewImageId);
        }
    }

    private void setupNavigationButtons() {
        Button prevButton = findViewById(R.id.prevButton);
        Button nextButton = findViewById(R.id.nextButton);
        Button playButton = findViewById(R.id.playButton);

        prevButton.setOnClickListener(v -> {
            if (currentGameIndex > 0) {
                currentGameIndex--;
                updateGameInfo();
            }
        });

        nextButton.setOnClickListener(v -> {
            if (currentGameIndex < gameNames.length - 1) {
                currentGameIndex++;
                updateGameInfo();
            }
        });

        playButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, gameActivities[currentGameIndex]);
            startActivity(intent);
        });
    }
} 