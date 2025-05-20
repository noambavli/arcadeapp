package com.example.arcadeapp;

import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.Iterator;
import java.util.Random;

public class FlappyBirdActivity extends AppCompatActivity {
    private FrameLayout gameBoard;
    private TextView scoreText;
    private TextView highScoreText;
    private int score = 0;
    private int highScore = 0;
    private boolean isGameRunning = false;
    private boolean isGameStarted = false;
    private Handler handler = new Handler();
    private Random random = new Random();
    private Button exitButton;
    private Button flapButton;
    private Button startButton;
    private View bird;
    private ArrayList<View> pipes = new ArrayList<>();
    private int birdY = 0;
    private int birdVelocity = 0;
    private int gravity = 1;
    private int jumpForce = -15;
    private int pipeSpeed = 5;
    private int pipeGap = 400;
    private int pipeWidth = 100;
    private int spawnInterval = 3000;
    private int difficultyLevel = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flappy_bird);

        gameBoard = findViewById(R.id.gameBoard);
        scoreText = findViewById(R.id.scoreText);
        highScoreText = findViewById(R.id.highScoreText);
        exitButton = findViewById(R.id.exit_button);
        flapButton = findViewById(R.id.flapButton);
        startButton = findViewById(R.id.startButton);

        // Initialize high score to 0
        highScore = 0;
        highScoreText.setText("High Score: " + highScore);

        exitButton.setOnClickListener(v -> {
            if (isGameRunning) {
                showExitDialog();
            } else {
                finish();
            }
        });
        flapButton.setOnClickListener(v -> flap());
        startButton.setOnClickListener(v -> startGame());

        // Wait for layout to be ready
        gameBoard.post(() -> {
            createBird();
        });
    }

    private void startGame() {
        if (isGameStarted) return;
        
        isGameStarted = true;
        isGameRunning = true;
        score = 0;
        scoreText.setText("Score: 0");
        
        // Clear any existing pipes
        for (View pipe : pipes) {
            gameBoard.removeView(pipe);
        }
        pipes.clear();
        
        // Reset bird position
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) bird.getLayoutParams();
        params.topMargin = gameBoard.getHeight() / 2;
        bird.setLayoutParams(params);
        birdY = params.topMargin;
        birdVelocity = 0;
        
        // Enable flap button and disable start button
        flapButton.setEnabled(true);
        startButton.setEnabled(false);
        
        // Start game systems
        startGameLoop();
        startPipeSpawner();
        startDifficultyTimer();
    }

    private void createBird() {
        bird = new View(this);
        bird.setBackgroundColor(Color.YELLOW);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(80, 80);
        params.leftMargin = 200;
        params.topMargin = gameBoard.getHeight() / 2;
        bird.setLayoutParams(params);
        gameBoard.addView(bird);
        birdY = params.topMargin;
    }

    private void flap() {
        if (!isGameRunning) return;
        birdVelocity = jumpForce;
    }

    private void startGameLoop() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isGameRunning) {
                    updateGame();
                    handler.postDelayed(this, 16); // ~60 FPS
                }
            }
        });
    }

    private void updateGame() {
        // Update bird
        updateBird();
        
        // Update pipes
        updatePipes();
        
        // Check collisions
        checkCollisions();
    }

    private void updateBird() {
        birdVelocity += gravity;
        birdY += birdVelocity;
        
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) bird.getLayoutParams();
        params.topMargin = birdY;
        bird.setLayoutParams(params);
        
        // Check if bird hits the ground or ceiling
        if (birdY <= 0 || birdY >= gameBoard.getHeight() - params.height) {
            gameOver();
        }
    }

    private void updatePipes() {
        Iterator<View> iterator = pipes.iterator();
        while (iterator.hasNext()) {
            View pipe = iterator.next();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) pipe.getLayoutParams();
            params.leftMargin -= pipeSpeed;
            
            if (params.leftMargin < -pipeWidth) {
                gameBoard.removeView(pipe);
                iterator.remove();
                // Only increment score for top pipes (those with topMargin = 0)
                if (params.topMargin == 0) {
                    score++;
                    scoreText.setText("Score: " + score);
                }
            } else {
                pipe.setLayoutParams(params);
            }
        }
    }

    private void checkCollisions() {
        FrameLayout.LayoutParams birdParams = (FrameLayout.LayoutParams) bird.getLayoutParams();
        
        for (View pipe : pipes) {
            FrameLayout.LayoutParams pipeParams = (FrameLayout.LayoutParams) pipe.getLayoutParams();
            
            if (isColliding(birdParams, pipeParams)) {
                gameOver();
                return;
            }
        }
    }

    private boolean isColliding(FrameLayout.LayoutParams birdParams, FrameLayout.LayoutParams pipeParams) {
        return birdParams.leftMargin < pipeParams.leftMargin + pipeParams.width &&
               birdParams.leftMargin + birdParams.width > pipeParams.leftMargin &&
               birdParams.topMargin < pipeParams.topMargin + pipeParams.height &&
               birdParams.topMargin + birdParams.height > pipeParams.topMargin;
    }

    private void startPipeSpawner() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isGameRunning) {
                    spawnPipe();
                    handler.postDelayed(this, spawnInterval);
                }
            }
        });
    }

    private void spawnPipe() {
        // Create top pipe
        View topPipe = new View(this);
        topPipe.setBackgroundColor(Color.GREEN);
        int pipeHeight = random.nextInt(gameBoard.getHeight() - pipeGap - 200) + 100;
        FrameLayout.LayoutParams topParams = new FrameLayout.LayoutParams(pipeWidth, pipeHeight);
        topParams.leftMargin = gameBoard.getWidth();
        topParams.topMargin = 0;
        topPipe.setLayoutParams(topParams);
        gameBoard.addView(topPipe);
        pipes.add(topPipe);

        // Create bottom pipe
        View bottomPipe = new View(this);
        bottomPipe.setBackgroundColor(Color.GREEN);
        FrameLayout.LayoutParams bottomParams = new FrameLayout.LayoutParams(pipeWidth, gameBoard.getHeight() - pipeHeight - pipeGap);
        bottomParams.leftMargin = gameBoard.getWidth();
        bottomParams.topMargin = pipeHeight + pipeGap;
        bottomPipe.setLayoutParams(bottomParams);
        gameBoard.addView(bottomPipe);
        pipes.add(bottomPipe);
    }

    private void startDifficultyTimer() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isGameRunning) {
                    difficultyLevel++;
                    // Slower increase in pipe speed
                    pipeSpeed = 5 + (difficultyLevel / 5);
                    // Slower decrease in spawn interval
                    spawnInterval = Math.max(2000, 3000 - (difficultyLevel * 30));
                    // Increase interval between difficulty changes to 2 minutes (120000 ms)
                    handler.postDelayed(this, 120000);
                }
            }
        });
    }

    private void gameOver() {
        isGameRunning = false;
        isGameStarted = false;
        
        // Show game over dialog
        new AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage("Your score: " + score)
            .setPositiveButton("Play Again", (dialog, which) -> {
                startGame();
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
        if (isGameRunning) {
            isGameRunning = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // No need to load high score from storage anymore
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Exit Game")
            .setMessage("Are you sure you want to exit? Your progress will be lost.")
            .setPositiveButton("Yes", (dialog, which) -> {
                finish();
            })
            .setNegativeButton("No", null)
            .show();
    }
} 