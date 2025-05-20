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
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class SpaceShooterActivity extends AppCompatActivity {
    private FrameLayout gameBoard;
    private TextView scoreText;
    private TextView livesText;
    private int score = 0;
    private int lives = 3;
    private boolean isGameRunning = true;
    private Handler handler = new Handler();
    private Random random = new Random();
    private Button exitButton;
    private Button leftButton, rightButton, fireButton;
    private View playerShip;
    private ArrayList<View> bullets = new ArrayList<>();
    private ArrayList<View> enemies = new ArrayList<>();
    private ArrayList<View> asteroids = new ArrayList<>();
    private int playerX = 0;
    private int playerSpeed = 70;
    private int bulletSpeed = 30;
    private int enemySpeed = 6;
    private int asteroidSpeed = 15;
    private int spawnInterval = 2000; // milliseconds
    private int difficultyLevel = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_shooter);

        gameBoard = findViewById(R.id.gameBoard);
        scoreText = findViewById(R.id.scoreText);
        livesText = findViewById(R.id.livesText);
        exitButton = findViewById(R.id.exit_button);
        leftButton = findViewById(R.id.leftButton);
        rightButton = findViewById(R.id.rightButton);
        fireButton = findViewById(R.id.fireButton);

        exitButton.setOnClickListener(v -> finish());

        // Wait for layout to be ready
        gameBoard.post(() -> {
            initializeGame();
        });
    }

    private void initializeGame() {
        // Create player ship
        createPlayerShip();
        
        // Set up controls
        setupControls();
        
        // Start game loop
        startGameLoop();
        
        // Start enemy spawner
        startEnemySpawner();
        
        // Start asteroid spawner
        startAsteroidSpawner();
        
        // Start difficulty timer
        startDifficultyTimer();
    }

    private void createPlayerShip() {
        playerShip = new View(this);
        playerShip.setBackgroundColor(Color.BLUE);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100, 100);
        params.leftMargin = gameBoard.getWidth() / 2 - 50;
        params.topMargin = gameBoard.getHeight() - 150;
        playerShip.setLayoutParams(params);
        gameBoard.addView(playerShip);
        playerX = params.leftMargin;
    }

    private void setupControls() {
        leftButton.setOnClickListener(v -> movePlayer(-playerSpeed));
        rightButton.setOnClickListener(v -> movePlayer(playerSpeed));
        fireButton.setOnClickListener(v -> fireBullet());
    }

    private void movePlayer(int deltaX) {
        if (!isGameRunning) return;
        
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) playerShip.getLayoutParams();
        int newX = params.leftMargin + deltaX;
        
        // Keep player within bounds
        if (newX >= 0 && newX <= gameBoard.getWidth() - params.width) {
            params.leftMargin = newX;
            playerX = newX;
            playerShip.setLayoutParams(params);
        }
    }

    private void fireBullet() {
        if (!isGameRunning) return;
        
        View bullet = new View(this);
        bullet.setBackgroundColor(Color.YELLOW);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(20, 40);
        params.leftMargin = playerX + 40; // Center of player ship
        params.topMargin = gameBoard.getHeight() - 150;
        bullet.setLayoutParams(params);
        gameBoard.addView(bullet);
        bullets.add(bullet);
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
        // Update bullets
        updateBullets();
        
        // Update enemies
        updateEnemies();
        
        // Update asteroids
        updateAsteroids();
        
        // Check collisions
        checkCollisions();
    }

    private void updateBullets() {
        Iterator<View> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            View bullet = iterator.next();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) bullet.getLayoutParams();
            params.topMargin -= bulletSpeed;
            
            if (params.topMargin < 0) {
                gameBoard.removeView(bullet);
                iterator.remove();
            } else {
                bullet.setLayoutParams(params);
            }
        }
    }

    private void updateEnemies() {
        Iterator<View> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            View enemy = iterator.next();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) enemy.getLayoutParams();
            params.topMargin += enemySpeed;
            
            if (params.topMargin > gameBoard.getHeight()) {
                gameBoard.removeView(enemy);
                iterator.remove();
                loseLife();
            } else {
                enemy.setLayoutParams(params);
            }
        }
    }

    private void updateAsteroids() {
        Iterator<View> iterator = asteroids.iterator();
        while (iterator.hasNext()) {
            View asteroid = iterator.next();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) asteroid.getLayoutParams();
            params.topMargin += asteroidSpeed;
            
            if (params.topMargin > gameBoard.getHeight()) {
                gameBoard.removeView(asteroid);
                iterator.remove();
            } else {
                asteroid.setLayoutParams(params);
            }
        }
    }

    private void checkCollisions() {
        // Check bullet-enemy collisions
        Iterator<View> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            View bullet = bulletIterator.next();
            FrameLayout.LayoutParams bulletParams = (FrameLayout.LayoutParams) bullet.getLayoutParams();
            
            Iterator<View> enemyIterator = enemies.iterator();
            while (enemyIterator.hasNext()) {
                View enemy = enemyIterator.next();
                FrameLayout.LayoutParams enemyParams = (FrameLayout.LayoutParams) enemy.getLayoutParams();
                
                if (isColliding(bulletParams, enemyParams)) {
                    gameBoard.removeView(bullet);
                    gameBoard.removeView(enemy);
                    bulletIterator.remove();
                    enemyIterator.remove();
                    score += 100;
                    scoreText.setText("Score: " + score);
                    break;
                }
            }
        }
        
        // Check player-enemy and player-asteroid collisions
        FrameLayout.LayoutParams playerParams = (FrameLayout.LayoutParams) playerShip.getLayoutParams();
        
        // Check enemy collisions
        Iterator<View> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            View enemy = enemyIterator.next();
            if (isColliding(playerParams, (FrameLayout.LayoutParams) enemy.getLayoutParams())) {
                gameBoard.removeView(enemy);
                enemyIterator.remove();
                loseLife();
            }
        }
        
        // Check asteroid collisions
        Iterator<View> asteroidIterator = asteroids.iterator();
        while (asteroidIterator.hasNext()) {
            View asteroid = asteroidIterator.next();
            if (isColliding(playerParams, (FrameLayout.LayoutParams) asteroid.getLayoutParams())) {
                gameBoard.removeView(asteroid);
                asteroidIterator.remove();
                loseLife();
            }
        }
    }

    private boolean isColliding(FrameLayout.LayoutParams params1, FrameLayout.LayoutParams params2) {
        return params1.leftMargin < params2.leftMargin + params2.width &&
               params1.leftMargin + params1.width > params2.leftMargin &&
               params1.topMargin < params2.topMargin + params2.height &&
               params1.topMargin + params1.height > params2.topMargin;
    }

    private void loseLife() {
        lives--;
        livesText.setText("Lives: " + lives);
        
        if (lives <= 0) {
            gameOver();
        }
    }

    private void startEnemySpawner() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isGameRunning) {
                    spawnEnemy();
                    handler.postDelayed(this, spawnInterval);
                }
            }
        });
    }

    private void startAsteroidSpawner() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isGameRunning) {
                    spawnAsteroid();
                    handler.postDelayed(this, spawnInterval * 2);
                }
            }
        });
    }

    private void spawnEnemy() {
        View enemy = new View(this);
        enemy.setBackgroundColor(Color.RED);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(80, 80);
        params.leftMargin = random.nextInt(gameBoard.getWidth() - 80);
        params.topMargin = -80;
        enemy.setLayoutParams(params);
        gameBoard.addView(enemy);
        enemies.add(enemy);
    }

    private void spawnAsteroid() {
        View asteroid = new View(this);
        asteroid.setBackgroundColor(Color.GRAY);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(60, 60);
        params.leftMargin = random.nextInt(gameBoard.getWidth() - 60);
        params.topMargin = -60;
        asteroid.setLayoutParams(params);
        gameBoard.addView(asteroid);
        asteroids.add(asteroid);
    }

    private void startDifficultyTimer() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isGameRunning) {
                    difficultyLevel++;
                    spawnInterval = Math.max(500, 2000 - (difficultyLevel * 100));
                    enemySpeed = 10 + (difficultyLevel / 2);
                    asteroidSpeed = 15 + (difficultyLevel / 2);
                    handler.postDelayed(this, 10000); // Increase difficulty every 10 seconds
                }
            }
        });
    }

    private void gameOver() {
        isGameRunning = false;
        String gameOverMessage = "Game Over!\nFinal Score: " + score;
        Toast.makeText(this, gameOverMessage, Toast.LENGTH_LONG).show();
        
        // Update the score text with final score
        scoreText.setText("Final Score: " + score);
        
        // Show exit button
        exitButton.setVisibility(View.VISIBLE);
        
        // Send the score back to the previous activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("score", score);
        setResult(RESULT_OK, resultIntent);
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