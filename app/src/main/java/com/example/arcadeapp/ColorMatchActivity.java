package com.example.arcadeapp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;
import java.util.ArrayList;
import java.util.Random;

public class ColorMatchActivity extends AppCompatActivity {
    private GridLayout colorGrid;
    private TextView scoreText;
    private TextView levelText;
    private Button startButton;
    private Button exitButton;
    private ArrayList<Integer> colorSequence;
    private ArrayList<Integer> playerSequence;
    private int currentLevel = 1;
    private int score = 0;
    private boolean isPlaying = false;
    private boolean isShowingSequence = false;
    private final Handler handler = new Handler();
    private final Random random = new Random();
    private final int[] colorResIds = {
        R.color.vibrant_red,
        R.color.vibrant_blue,
        R.color.vibrant_green,
        R.color.vibrant_yellow
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_match);

        colorGrid = findViewById(R.id.colorGrid);
        scoreText = findViewById(R.id.scoreText);
        levelText = findViewById(R.id.levelText);
        startButton = findViewById(R.id.startButton);
        exitButton = findViewById(R.id.exitButton);

        setupColorGrid();
        updateUI();

        startButton.setOnClickListener(v -> {
            if (!isPlaying) {
                startGame();
            }
        });

        exitButton.setOnClickListener(v -> {
            if (isPlaying) {
                showExitDialog();
            } else {
                finish();
            }
        });
    }

    private void setupColorGrid() {
        colorGrid.removeAllViews();
        for (int i = 0; i < 4; i++) {
            Button colorButton = new Button(this);
            colorButton.setBackgroundColor(ContextCompat.getColor(this, colorResIds[i]));
            colorButton.setTag(i);
            
            // Set button size
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = getResources().getDisplayMetrics().widthPixels / 3;
            params.height = getResources().getDisplayMetrics().widthPixels / 3;
            params.setMargins(10, 10, 10, 10);
            colorButton.setLayoutParams(params);

            colorButton.setOnClickListener(v -> {
                if (isPlaying && !isShowingSequence) {
                    int clickedColor = (int) v.getTag();
                    handleColorClick(clickedColor);
                }
            });

            colorGrid.addView(colorButton);
        }
    }

    private void startGame() {
        isPlaying = true;
        currentLevel = 1;
        score = 0;
        colorSequence = new ArrayList<>();
        playerSequence = new ArrayList<>();
        startButton.setEnabled(false);
        showNextSequence();
    }

    private void showNextSequence() {
        isShowingSequence = true;
        colorSequence.add(random.nextInt(4));
        
        for (int i = 0; i < colorSequence.size(); i++) {
            final int index = i;
            handler.postDelayed(() -> {
                Button button = (Button) colorGrid.getChildAt(colorSequence.get(index));
                animateButton(button);
            }, i * 1000);
        }

        handler.postDelayed(() -> {
            isShowingSequence = false;
            playerSequence.clear();
        }, colorSequence.size() * 1000);
    }

    private void animateButton(Button button) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.8f, 1f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(300);
        animatorSet.start();
    }

    private void handleColorClick(int colorIndex) {
        playerSequence.add(colorIndex);
        Button button = (Button) colorGrid.getChildAt(colorIndex);
        animateButton(button);

        if (playerSequence.get(playerSequence.size() - 1) != colorSequence.get(playerSequence.size() - 1)) {
            gameOver();
            return;
        }

        if (playerSequence.size() == colorSequence.size()) {
            score += currentLevel * 10;
            currentLevel++;
            updateUI();
            handler.postDelayed(this::showNextSequence, 1000);
        }
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Exit Game")
            .setMessage("Are you sure you want to exit? Your current score will be saved.")
            .setPositiveButton("Yes", (dialog, which) -> {
                saveScore();
                finish();
            })
            .setNegativeButton("No", null)
            .show();
    }

    private void saveScore() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("score", score);
        setResult(RESULT_OK, resultIntent);
    }

    private void gameOver() {
        isPlaying = false;
        saveScore();
        Toast.makeText(this, "Game Over! Final Score: " + score, Toast.LENGTH_LONG).show();
        startButton.setText("Play Again");
        startButton.setEnabled(true);
    }

    private void updateUI() {
        scoreText.setText("Score: " + score);
        levelText.setText("Level: " + currentLevel);
    }
} 