package com.example.arcadeapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class BubblePopActivity extends AppCompatActivity {
    private FrameLayout gameBoard;
    private TextView scoreText;
    private int score = 0;
    private boolean isGameRunning = true;
    private Handler handler = new Handler();
    private Random random = new Random();
    private Button exitButton;
    private ArrayList<Bubble> bubbles = new ArrayList<>();
    private int baseSpeed = 10; // pixels per frame
    private int spawnInterval = 1000; // milliseconds
    private int gameTime = 0; // seconds
    private int difficultyLevel = 1;
    
    private class Bubble {
        View view;
        int size;
        int points;
        float speedX;
        float speedY;
        boolean isSpecial;
        
        Bubble(View view, int size, int points, boolean isSpecial) {
            this.view = view;
            this.size = size;
            this.points = points;
            this.isSpecial = isSpecial;
            this.speedX = random.nextFloat() * 4 - 2; // Random horizontal movement
            this.speedY = -baseSpeed - random.nextFloat() * 2; // Upward movement with variation
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bubble_pop);
        
        gameBoard = findViewById(R.id.gameBoard);
        scoreText = findViewById(R.id.scoreText);
        exitButton = findViewById(R.id.exit_button);
        
        exitButton.setOnClickListener(v -> finish());
        
        // Wait for layout to be ready
        gameBoard.post(() -> {
            initializeGame();
        });
    }
    
    private void initializeGame() {
        // Start spawning bubbles
        startBubbleSpawner();
        
        // Start difficulty timer
        startDifficultyTimer();
        
        // Set up touch listener for popping bubbles
        gameBoard.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                float y = event.getY();
                checkBubblePop(x, y);
            }
            return true;
        });
    }
    
    private void startDifficultyTimer() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isGameRunning) {
                    gameTime++;
                    if (gameTime % 10 == 0) { // Increase difficulty every 10 seconds
                        difficultyLevel++;
                        spawnInterval = Math.max(500, 1000 - (difficultyLevel * 50)); // Decrease spawn interval
                        baseSpeed = 3 + (difficultyLevel / 2); // Increase base speed
                    }
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }
    
    private void startBubbleSpawner() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isGameRunning) {
                    spawnBubble();
                    handler.postDelayed(this, spawnInterval);
                }
            }
        });
    }
    
    private void spawnBubble() {
        // Determine bubble type
        boolean isSpecial = random.nextFloat() < 0.2; // 20% chance for special bubble
        int size;
        int points;
        
        if (isSpecial) {
            size = 150; // Larger special bubble
            points = 50; // More points for special bubbles
        } else {
            // Random size for normal bubbles
            float sizeRand = random.nextFloat();
            if (sizeRand < 0.4) { // 40% chance
                size = 80;
                points = 10;
            } else if (sizeRand < 0.8) { // 40% chance
                size = 120;
                points = 20;
            } else { // 20% chance
                size = 160;
                points = 30;
            }
        }
        
        View bubbleView = new View(this);
        bubbleView.setBackgroundColor(isSpecial ? Color.YELLOW : getRandomColor());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
        params.leftMargin = random.nextInt(gameBoard.getWidth() - size);
        params.topMargin = gameBoard.getHeight();
        bubbleView.setLayoutParams(params);
        
        gameBoard.addView(bubbleView);
        Bubble bubble = new Bubble(bubbleView, size, points, isSpecial);
        bubbles.add(bubble);
        
        // Start moving the bubble
        moveBubble(bubble);
    }
    
    private void moveBubble(Bubble bubble) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!isGameRunning || !bubbles.contains(bubble)) {
                    return;
                }
                
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) bubble.view.getLayoutParams();
                
                // Update position with bouncing effect
                params.leftMargin += bubble.speedX;
                params.topMargin += bubble.speedY;
                
                // Bounce off walls
                if (params.leftMargin <= 0 || params.leftMargin >= gameBoard.getWidth() - bubble.size) {
                    bubble.speedX *= -1;
                    params.leftMargin = Math.max(0, Math.min(params.leftMargin, gameBoard.getWidth() - bubble.size));
                }
                
                // Check if bubble reached the top
                if (params.topMargin <= 0) {
                    gameOver();
                    return;
                }
                
                bubble.view.setLayoutParams(params);
                handler.postDelayed(this, 16); // ~60 FPS
            }
        });
    }
    
    private void checkBubblePop(float x, float y) {
        Iterator<Bubble> iterator = bubbles.iterator();
        while (iterator.hasNext()) {
            Bubble bubble = iterator.next();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) bubble.view.getLayoutParams();
            
            // Check if touch is within bubble bounds
            if (x >= params.leftMargin && x <= params.leftMargin + bubble.size &&
                y >= params.topMargin && y <= params.topMargin + bubble.size) {
                gameBoard.removeView(bubble.view);
                iterator.remove();
                score += bubble.points;
                scoreText.setText("Score: " + score);
                
                // Show points popup
                if (bubble.isSpecial) {
                    Toast.makeText(this, "+" + bubble.points + "!", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
    
    private int getRandomColor() {
        int[] colors = {
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.MAGENTA,
            Color.CYAN
        };
        return colors[random.nextInt(colors.length)];
    }
    
    private void gameOver() {
        isGameRunning = false;
        
        // Show game over dialog
        new AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage("Your score: " + score)
            .setPositiveButton("Play Again", (dialog, which) -> {
                // Reset game state
                score = 0;
                scoreText.setText("Score: 0");
                gameBoard.removeAllViews();
                bubbles.clear();
                gameTime = 0;
                difficultyLevel = 1;
                spawnInterval = 1000;
                baseSpeed = 10;
                isGameRunning = true;
                initializeGame();
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
    
    @Override
    protected void onPause() {
        super.onPause();
        isGameRunning = false;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (!isGameRunning) {
            isGameRunning = true;
            startBubbleSpawner();
            startDifficultyTimer();
        }
    }
} 