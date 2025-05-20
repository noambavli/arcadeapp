package com.example.arcadeapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Random;

public class SnakeActivity extends AppCompatActivity {
    private FrameLayout gameBoard;
    private TextView scoreText;
    private int score = 0;
    private boolean isGameRunning = true;
    private Handler handler = new Handler();
    private Random random = new Random();
    private Button exitButton;
    private Button upButton, downButton, leftButton, rightButton;
    private View currentFood;
    
    // Snake properties
    private ArrayList<View> snakeParts = new ArrayList<>();
    private int snakeDirection = 0; // 0: right, 1: down, 2: left, 3: up
    private int snakeSpeed = 200; // milliseconds between moves
    private int gridSize = 50; // size of each snake part in pixels
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snake);
        
        gameBoard = findViewById(R.id.gameBoard);
        scoreText = findViewById(R.id.scoreText);
        exitButton = findViewById(R.id.exit_button);
        Button backButton = findViewById(R.id.backButton);
        
        // Initialize direction buttons
        upButton = findViewById(R.id.up_button);
        downButton = findViewById(R.id.down_button);
        leftButton = findViewById(R.id.left_button);
        rightButton = findViewById(R.id.right_button);
        
        // Set up button click listeners
        upButton.setOnClickListener(v -> changeDirection(3));
        downButton.setOnClickListener(v -> changeDirection(1));
        leftButton.setOnClickListener(v -> changeDirection(2));
        rightButton.setOnClickListener(v -> changeDirection(0));
        
        exitButton.setOnClickListener(v -> finish());
        backButton.setOnClickListener(v -> finish());
        
        // Wait for layout to be ready
        gameBoard.post(() -> {
            initializeGame();
        });
    }
    
    private void changeDirection(int newDirection) {
        if (!isGameRunning) return;
        
        // Only change direction if it's not a 180-degree turn
        if (Math.abs(newDirection - snakeDirection) != 2) {
            snakeDirection = newDirection;
        }
    }
    
    private void initializeGame() {
        // Initialize snake
        View snakeHead = new View(this);
        snakeHead.setBackgroundColor(Color.GREEN);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(gridSize, gridSize);
        params.leftMargin = 100;
        params.topMargin = 100;
        snakeHead.setLayoutParams(params);
        gameBoard.addView(snakeHead);
        snakeParts.add(snakeHead);
        
        // Spawn initial food
        spawnFood();
        
        // Start game loop
        startGameLoop();
    }
    
    private void startGameLoop() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isGameRunning) {
                    moveSnake();
                    handler.postDelayed(this, snakeSpeed);
                }
            }
        });
    }
    
    private void moveSnake() {
        if (gameBoard.getWidth() == 0 || gameBoard.getHeight() == 0) {
            return; // Skip if layout is not ready
        }
        
        View head = snakeParts.get(0);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) head.getLayoutParams();
        int oldLeftMargin = params.leftMargin;
        int oldTopMargin = params.topMargin;
        
        // Calculate new position based on direction
        switch (snakeDirection) {
            case 0: // right
                params.leftMargin += gridSize;
                break;
            case 1: // down
                params.topMargin += gridSize;
                break;
            case 2: // left
                params.leftMargin -= gridSize;
                break;
            case 3: // up
                params.topMargin -= gridSize;
                break;
        }
        
        // Check for collisions with walls
        if (params.leftMargin < 0 || params.leftMargin > gameBoard.getWidth() - gridSize ||
            params.topMargin < 0 || params.topMargin > gameBoard.getHeight() - gridSize) {
            gameOver();
            return;
        }
        
        // Check for collision with self
        for (int i = 1; i < snakeParts.size(); i++) {
            View part = snakeParts.get(i);
            FrameLayout.LayoutParams partParams = (FrameLayout.LayoutParams) part.getLayoutParams();
            if (params.leftMargin == partParams.leftMargin && params.topMargin == partParams.topMargin) {
                gameOver();
                return;
            }
        }
        
        // Move head
        head.setLayoutParams(params);
        
        // Move body parts
        for (int i = snakeParts.size() - 1; i > 0; i--) {
            View current = snakeParts.get(i);
            View previous = snakeParts.get(i - 1);
            FrameLayout.LayoutParams currentParams = (FrameLayout.LayoutParams) current.getLayoutParams();
            FrameLayout.LayoutParams previousParams = (FrameLayout.LayoutParams) previous.getLayoutParams();
            currentParams.leftMargin = previousParams.leftMargin;
            currentParams.topMargin = previousParams.topMargin;
            current.setLayoutParams(currentParams);
        }
        
        // Check for food collision
        if (currentFood != null) {
            FrameLayout.LayoutParams foodParams = (FrameLayout.LayoutParams) currentFood.getLayoutParams();
            if (params.leftMargin == foodParams.leftMargin && params.topMargin == foodParams.topMargin) {
                // Snake ate food
                gameBoard.removeView(currentFood);
                growSnake();
                score += 10;
                scoreText.setText("Score: " + score);
                spawnFood();
            }
        }
    }
    
    private void spawnFood() {
        if (gameBoard.getWidth() == 0 || gameBoard.getHeight() == 0) {
            return; // Skip if layout is not ready
        }
        
        if (currentFood != null) {
            gameBoard.removeView(currentFood);
        }
        
        currentFood = new View(this);
        currentFood.setBackgroundColor(Color.RED);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(gridSize, gridSize);
        
        // Find a valid position for food (not on snake)
        boolean validPosition = false;
        while (!validPosition) {
            params.leftMargin = random.nextInt(gameBoard.getWidth() / gridSize) * gridSize;
            params.topMargin = random.nextInt(gameBoard.getHeight() / gridSize) * gridSize;
            
            validPosition = true;
            for (View part : snakeParts) {
                FrameLayout.LayoutParams partParams = (FrameLayout.LayoutParams) part.getLayoutParams();
                if (params.leftMargin == partParams.leftMargin && params.topMargin == partParams.topMargin) {
                    validPosition = false;
                    break;
                }
            }
        }
        
        currentFood.setLayoutParams(params);
        gameBoard.addView(currentFood);
    }
    
    private void growSnake() {
        View lastPart = snakeParts.get(snakeParts.size() - 1);
        FrameLayout.LayoutParams lastParams = (FrameLayout.LayoutParams) lastPart.getLayoutParams();
        
        View newPart = new View(this);
        newPart.setBackgroundColor(Color.GREEN);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(gridSize, gridSize);
        params.leftMargin = lastParams.leftMargin;
        params.topMargin = lastParams.topMargin;
        newPart.setLayoutParams(params);
        
        gameBoard.addView(newPart);
        snakeParts.add(newPart);
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
                snakeParts.clear();
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
            startGameLoop();
        }
    }
} 