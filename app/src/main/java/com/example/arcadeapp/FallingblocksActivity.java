package com.example.arcadeapp;

import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class FallingblocksActivity extends AppCompatActivity {
    private boolean dialogShown = false;

    private FrameLayout gameBoard;
    private TextView scoreText;
    private int score = 0;
    private boolean isGameRunning = true;
    private Handler handler = new Handler();
    private Random random = new Random();
    private Button exitButton;  // Declare here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fallingblocks);

        gameBoard = findViewById(R.id.gameBoard);
        scoreText = findViewById(R.id.scoreText);
        exitButton = findViewById(R.id.exit_button);  // Initialize here

        exitButton.setOnClickListener(v -> {
            finish();  // Exit the game activity when the player presses "Exit"
        });

        startGameLoop();
    }

    private void startGameLoop() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isGameRunning) {
                    spawnBlock();
                    handler.postDelayed(this, 1000); // Spawn a new block every second
                }
            }
        });
    }

    private void final_dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game ended")
                .setMessage("Thanks for playing! Your score: " + score)
                .setNeutralButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        // Show the dialog
        builder.create().show();
    }

    private void stopGame() {
        // Stop spawning new blocks
        isGameRunning = false;

        // Stop all ongoing block animations
        handler.removeCallbacksAndMessages(null);

        // Optionally, reset or clean up any remaining blocks on the screen
        gameBoard.removeAllViews();

        // Display game over message
        scoreText.setText("Final Score: " + score);
    }

    private void spawnBlock() {
        final View block = new View(this);
        int size = 170;

        // Create a circular background for the block
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);  // Set the shape to oval (circle)
        drawable.setColor(getRandomColor());  // Random color for the block
        block.setBackground(drawable);

        // Set the size of the block
        block.setLayoutParams(new FrameLayout.LayoutParams(size, size));

        // Position the block randomly at the top of the game board
        block.setX(random.nextInt(gameBoard.getWidth() - size)); // Random x position
        block.setY(0); // Place the block at the top

        // Add the block to the game board
        gameBoard.addView(block);

        // Animate the block falling using ValueAnimator to track its position
        final ValueAnimator fallAnimator = ValueAnimator.ofFloat(0, gameBoard.getHeight() - block.getHeight());
        fallAnimator.setDuration(2500); // Duration for the block to fall
        fallAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                block.setY(value);

                // Check if the block has reached the bottom
                if (value >= gameBoard.getHeight() - block.getHeight()) {
                    stopGame();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("score", score);  // Pass the best score
                    setResult(RESULT_OK, resultIntent);

                    // Check if the dialog has already been shown to avoid calling it again
                    if (!dialogShown) {
                        dialogShown = true;
                        //final_dialog();
                    }
                }
            }
        });

        // Start the falling animation
        fallAnimator.start();

        // Add touch listener for popping the block when pressed
        block.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Pop the block by removing it when touched
                    gameBoard.removeView(block);
                    score += 5;  // Bonus points for popping a block
                    scoreText.setText("Score: " + score);
                    fallAnimator.cancel();  // Stop the falling animation
                    return true;
                }
                return false;
            }
        });
    }

    private int getRandomColor() {
        int[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN};
        return colors[random.nextInt(colors.length)];
    }

    @Override
    protected void onPause() {
        super.onPause();
        isGameRunning = false; // Stop the game when the app is paused
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isGameRunning) {
            isGameRunning = true;
            startGameLoop(); // Restart the game loop when the app is resumed
        }
    }
}
