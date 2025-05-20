package com.example.arcadeapp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryMatchActivity extends AppCompatActivity {
    private GridLayout gameGrid;
    private TextView scoreText;
    private TextView timerText;
    private Button exitButton;
    private int score = 0;
    private int pairsFound = 0;
    private final int TOTAL_PAIRS = 8;
    private List<Card> cards;
    private Card firstCard = null;
    private Card secondCard = null;
    private boolean isProcessing = false;
    private Handler handler = new Handler();
    private CountDownTimer timer;
    private long timeRemaining = 120000; // 2 minutes in milliseconds
    private boolean isGameOver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_match);

        gameGrid = findViewById(R.id.game_grid);
        scoreText = findViewById(R.id.score_text);
        timerText = findViewById(R.id.timer_text);
        exitButton = findViewById(R.id.exit_button);

        initializeGame();
        setupExitButton();
        startTimer();
    }

    @Override
    public void onBackPressed() {
        if (!isGameOver) {
            showExitDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void initializeGame() {
        cards = new ArrayList<>();
        // Create pairs of cards
        for (int i = 0; i < TOTAL_PAIRS; i++) {
            cards.add(new Card(i));
            cards.add(new Card(i));
        }
        Collections.shuffle(cards);

        // Create the game grid
        gameGrid.setColumnCount(4);
        gameGrid.setRowCount(4);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int cardSize = (screenWidth - 80) / 4; // 80dp for margins and padding

        for (Card card : cards) {
            Button cardButton = new Button(this);
            cardButton.setTag(card);
            cardButton.setBackgroundColor(ContextCompat.getColor(this, R.color.ocean_blue));
            cardButton.setOnClickListener(v -> handleCardClick((Button) v));
            
            // Set card size
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = cardSize;
            params.height = cardSize;
            params.setMargins(4, 4, 4, 4);
            cardButton.setLayoutParams(params);
            
            gameGrid.addView(cardButton);
        }
    }

    private void handleCardClick(Button button) {
        if (isProcessing || isGameOver) return;

        Card card = (Card) button.getTag();
        if (card.isMatched || card.isFlipped) return;

        // Flip the card
        flipCard(button, card);

        if (firstCard == null) {
            firstCard = card;
        } else {
            secondCard = card;
            isProcessing = true;

            // Check for match after a short delay
            handler.postDelayed(() -> checkForMatch(), 500);
        }
    }

    private void flipCard(Button button, Card card) {
        card.isFlipped = true;
        button.setBackgroundColor(ContextCompat.getColor(this, getCardColor(card.value)));
    }

    private void flipCardBack(Button button, Card card) {
        card.isFlipped = false;
        button.setBackgroundColor(ContextCompat.getColor(this, R.color.ocean_blue));
    }

    private void checkForMatch() {
        if (firstCard.value == secondCard.value) {
            // Match found
            pairsFound++;
            score += 10;
            firstCard.isMatched = true;
            secondCard.isMatched = true;
            updateScore();
            
            if (pairsFound == TOTAL_PAIRS) {
                showWinDialog();
            }
        } else {
            // No match
            score = Math.max(0, score - 2);
            updateScore();
            
            // Find and flip back the unmatched cards
            for (int i = 0; i < gameGrid.getChildCount(); i++) {
                View child = gameGrid.getChildAt(i);
                if (child instanceof Button) {
                    Card childCard = (Card) child.getTag();
                    if (childCard == firstCard || childCard == secondCard) {
                        flipCardBack((Button) child, childCard);
                    }
                }
            }
        }
        
        // Reset for next turn
        firstCard = null;
        secondCard = null;
        isProcessing = false;
    }

    private int getCardColor(int value) {
        int[] colors = {
            R.color.vibrant_red,
            R.color.vibrant_blue,
            R.color.vibrant_green,
            R.color.vibrant_yellow,
            R.color.vibrant_purple,
            R.color.vibrant_orange,
            R.color.vibrant_pink,
            R.color.teal_200
        };
        return colors[value % colors.length];
    }

    private void updateScore() {
        runOnUiThread(() -> {
            scoreText.setText("Score: " + score);
        });
    }

    private void startTimer() {
        timer = new CountDownTimer(timeRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!isGameOver) {
                    timeRemaining = millisUntilFinished;
                    int seconds = (int) (millisUntilFinished / 1000);
                    runOnUiThread(() -> {
                        timerText.setText(String.format("Time: %02d:%02d", seconds / 60, seconds % 60));
                    });
                }
            }

            @Override
            public void onFinish() {
                if (!isGameOver) {
                    showTimeUpDialog();
                }
            }
        }.start();
    }

    private void setupExitButton() {
        exitButton.setOnClickListener(v -> showExitDialog());
    }

    private void showExitDialog() {
        if (isGameOver) return;
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.GameDialog);
        builder.setTitle("Exit Game")
                .setMessage("Are you sure you want to exit? Your progress will be lost.")
                .setPositiveButton("Exit", (dialog, which) -> {
                    finishGame();
                })
                .setNegativeButton("Continue", null)
                .setCancelable(false)
                .show();
    }

    private void showWinDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Congratulations!")
            .setMessage("You won! Your score: " + score)
            .setPositiveButton("Play Again", (dialog, which) -> {
                resetGame();
            })
            .setNegativeButton("Exit", (dialog, which) -> {
                // Return score to previous activity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("score", score);
                setResult(RESULT_OK, resultIntent);
                finish();
            })
            .setCancelable(false)
            .show();
    }

    private void showTimeUpDialog() {
        isGameOver = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.GameDialog);
        builder.setTitle("Time's Up!")
                .setMessage("Your final score: " + score)
                .setPositiveButton("Exit", (dialog, which) -> {
                    finishGame();
                })
                .setCancelable(false)
                .show();
    }

    private void finishGame() {
        isGameOver = true;
        if (timer != null) {
            timer.cancel();
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        Intent resultIntent = new Intent();
        resultIntent.putExtra("score", score);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isGameOver = true;
        if (timer != null) {
            timer.cancel();
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    private void resetGame() {
        // Reset game state variables
        score = 0;
        pairsFound = 0;
        firstCard = null;
        secondCard = null;
        isProcessing = false;
        isGameOver = false;
        timeRemaining = 120000; // Reset timer to 2 minutes

        // Clear the game grid
        gameGrid.removeAllViews();

        // Update UI
        scoreText.setText("Score: 0");
        timerText.setText("Time: 02:00");

        // Reinitialize the game
        initializeGame();

        // Restart the timer
        if (timer != null) {
            timer.cancel();
        }
        startTimer();
    }

    private static class Card {
        int value;
        boolean isFlipped = false;
        boolean isMatched = false;

        Card(int value) {
            this.value = value;
        }
    }
} 