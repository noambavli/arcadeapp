package com.example.arcadeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GamesHomeActivity extends AppCompatActivity {

    private GridLayout gamesGridLayout;
    private ActivityResultLauncher<Intent> startForResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games_home);

        gamesGridLayout = findViewById(R.id.gamesGridLayout);
        Button backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        // Initialize the ActivityResultLauncher
        startForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                int score = result.getData().getIntExtra("score", 0);
                ServerUtils.getUserScore(GamesHomeActivity.this, new ServerUtils.ServerCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer currentScore) {
                        int calculated_score = currentScore + score;
                        updateScore(calculated_score);
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> 
                            Toast.makeText(GamesHomeActivity.this, "Error getting current score: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }
                });
            }
        });
        Typeface coolFont = ResourcesCompat.getFont(this, R.font.cool_font2);

        // Add Color Match button
        Button colorMatchButton = new Button(this);
        colorMatchButton.setText("Color Match");
        colorMatchButton.setBackgroundColor(ContextCompat.getColor(gamesGridLayout.getContext(), R.color.vibrant_purple));
        colorMatchButton.setTypeface(coolFont);
        colorMatchButton.setOnClickListener(v -> {
            Intent intent = new Intent(GamesHomeActivity.this, ColorMatchActivity.class);
            startForResult.launch(intent);
        });
        gamesGridLayout.addView(colorMatchButton);

        // Add a "Hangchairs" button
        Button hangchairsButton = new Button(this);
        hangchairsButton.setText("Hangchairs");
        hangchairsButton.setBackgroundColor(ContextCompat.getColor(gamesGridLayout.getContext(), R.color.vibrant_purple));
        hangchairsButton.setTypeface(coolFont);
        hangchairsButton.setOnClickListener(v -> {
            Intent intent = new Intent(GamesHomeActivity.this, HangchairsActivity.class);
            startForResult.launch(intent);
        });
        gamesGridLayout.addView(hangchairsButton);

        // Add a "Tic Tac Toe" button
        Button ticTacToeButton = new Button(this);
        ticTacToeButton.setText("Tic Tac Toe");
        ticTacToeButton.setTypeface(coolFont);
        ticTacToeButton.setBackgroundColor(ContextCompat.getColor(gamesGridLayout.getContext(), R.color.teal_700));
        ticTacToeButton.setOnClickListener(v -> {
            Intent intent = new Intent(GamesHomeActivity.this, TicTacToeActivity.class);
            startForResult.launch(intent);
        });
        gamesGridLayout.addView(ticTacToeButton);
        
        // Add Memory Match button
        Button memoryMatchButton = new Button(this);
        memoryMatchButton.setText("Memory Match");
        memoryMatchButton.setTypeface(coolFont);
        memoryMatchButton.setBackgroundColor(ContextCompat.getColor(gamesGridLayout.getContext(), R.color.vibrant_pink));
        memoryMatchButton.setOnClickListener(v -> {
            Intent intent = new Intent(GamesHomeActivity.this, MemoryMatchActivity.class);
            startForResult.launch(intent);
        });
        gamesGridLayout.addView(memoryMatchButton);

        // Add Guess Number button
        Button guessnumberButton = new Button(this);
        guessnumberButton.setText("Guess the number");
        guessnumberButton.setBackgroundColor(ContextCompat.getColor(gamesGridLayout.getContext(), R.color.vibrant_red));
        guessnumberButton.setTypeface(coolFont);
        guessnumberButton.setOnClickListener(v -> {
            Intent intent = new Intent(GamesHomeActivity.this, guessNumberActivity.class);
            startForResult.launch(intent);
        });
        gamesGridLayout.addView(guessnumberButton);

        // Add Falling Blocks button
        Button fallingBlocksButton = new Button(this);
        fallingBlocksButton.setText("Falling Blocks");
        fallingBlocksButton.setBackgroundColor(ContextCompat.getColor(gamesGridLayout.getContext(), R.color.vibrant_purple));
        fallingBlocksButton.setTypeface(coolFont);
        fallingBlocksButton.setOnClickListener(v -> {
            Intent intent = new Intent(GamesHomeActivity.this, FallingblocksActivity.class);
            startForResult.launch(intent);
        });
        gamesGridLayout.addView(fallingBlocksButton);

        // Add Snake game button
        Button snakeButton = new Button(this);
        snakeButton.setText("Snake");
        snakeButton.setBackgroundColor(ContextCompat.getColor(gamesGridLayout.getContext(), R.color.vibrant_green));
        snakeButton.setTypeface(coolFont);
        snakeButton.setOnClickListener(v -> {
            Intent intent = new Intent(GamesHomeActivity.this, SnakeActivity.class);
            startForResult.launch(intent);
        });
        gamesGridLayout.addView(snakeButton);

        // Add Red Button game
        Button redButtonButton = new Button(this);
        redButtonButton.setText("Red button");
        redButtonButton.setBackgroundColor(ContextCompat.getColor(gamesGridLayout.getContext(), R.color.vibrant_green));
        redButtonButton.setTypeface(coolFont);
        redButtonButton.setOnClickListener(v -> {
            Intent intent = new Intent(GamesHomeActivity.this, RedButtonActivity.class);
            startForResult.launch(intent);
        });
        gamesGridLayout.addView(redButtonButton);

        // Add Trivia button
        Button triviaButton = new Button(this);
        triviaButton.setText("Trivia");
        triviaButton.setTypeface(coolFont);
        triviaButton.setBackgroundColor(ContextCompat.getColor(gamesGridLayout.getContext(), R.color.vibrant_blue));
        triviaButton.setOnClickListener(v -> {
            Intent intent = new Intent(GamesHomeActivity.this, triviaActivity.class);
            startForResult.launch(intent);
        });
        gamesGridLayout.addView(triviaButton);

        // Add Space Shooter button
        Button spaceShooterButton = new Button(this);
        spaceShooterButton.setText("Space Shooter");
        spaceShooterButton.setTypeface(coolFont);
        spaceShooterButton.setBackgroundColor(ContextCompat.getColor(gamesGridLayout.getContext(), R.color.vibrant_purple));
        spaceShooterButton.setOnClickListener(v -> {
            Intent intent = new Intent(GamesHomeActivity.this, SpaceShooterActivity.class);
            startForResult.launch(intent);
        });
        gamesGridLayout.addView(spaceShooterButton);

        // Add Bubble Pop button
        Button bubblePopButton = new Button(this);
        bubblePopButton.setText("Bubble Pop");
        bubblePopButton.setTypeface(coolFont);
        bubblePopButton.setBackgroundColor(ContextCompat.getColor(gamesGridLayout.getContext(), R.color.vibrant_blue));
        bubblePopButton.setOnClickListener(v -> {
            Intent intent = new Intent(GamesHomeActivity.this, BubblePopActivity.class);
            startForResult.launch(intent);
        });
        gamesGridLayout.addView(bubblePopButton);

        // Add Flappy Bird button
        Button flappyBirdButton = new Button(this);
        flappyBirdButton.setText("Flappy Bird");
        flappyBirdButton.setTypeface(coolFont);
        flappyBirdButton.setBackgroundColor(ContextCompat.getColor(gamesGridLayout.getContext(), R.color.vibrant_green));
        flappyBirdButton.setOnClickListener(v -> {
            Intent intent = new Intent(GamesHomeActivity.this, FlappyBirdActivity.class);
            startForResult.launch(intent);
        });
        gamesGridLayout.addView(flappyBirdButton);

        // Add Puzzle Slide button
        Button puzzleSlideButton = new Button(this);
        puzzleSlideButton.setText("Puzzle Slide");
        puzzleSlideButton.setTypeface(coolFont);
        puzzleSlideButton.setBackgroundColor(ContextCompat.getColor(gamesGridLayout.getContext(), R.color.vibrant_green));
        puzzleSlideButton.setOnClickListener(v -> {
            Intent intent = new Intent(GamesHomeActivity.this, PuzzleSlideActivity.class);
            startForResult.launch(intent);
        });
        gamesGridLayout.addView(puzzleSlideButton);
    }

    private void updateScore(int newScore) {
        ServerUtils.updateUserScore(this, newScore, new ServerUtils.ServerCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> Toast.makeText(GamesHomeActivity.this, "Score updated", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> Toast.makeText(GamesHomeActivity.this, "Error updating score: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
}
