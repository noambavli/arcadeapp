package com.example.arcadeapp;


import static android.app.ProgressDialog.show;
import android.content.DialogInterface;
import android.graphics.Color;
import android.widget.Button;
import androidx.core.content.ContextCompat;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Random;

public class guessNumberActivity extends AppCompatActivity {
    private int score = 1;
    private int bestScore = 0;
    private int secretNumber;
    private EditText guessInput;
    private TextView scoreLabel, statistics;
    private Button submitButton, exitButton;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_guessnumber);

            guessInput = findViewById(R.id.guess_input);
            scoreLabel = findViewById(R.id.score_label);
            submitButton = findViewById(R.id.submit_guess_button);
            exitButton = findViewById(R.id.exit_button);

            // Start the game
            startNewGame();

            // Submit guess action
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkGuess();
                }
            });

            // Exit game action
            exitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showExitDialog();
                }
            });
        }

        private void startNewGame() {
            Random random = new Random();
            secretNumber = random.nextInt(5) + 1; // Random number between 1 and 5
        }

        private void checkGuess() {
            String guessStr = guessInput.getText().toString();
            if (guessStr.isEmpty()) {
                Toast.makeText(this, "Please enter a guess", Toast.LENGTH_SHORT).show();
                return;
            }

            int guess = Integer.parseInt(guessStr);

            if (guess == secretNumber)
            {
                showWinDialog();
            } else {
               // Reset the score
                scoreLabel.setText("Score: " + score);
                Toast.makeText(this, "Wrong! You lost everything.", Toast.LENGTH_SHORT).show();
                startNewGame();
            }
        }


    private void showWinDialog() {
        // Update score based on the previous condition
        if (score == 1) {
            score = 20;
        } else {
            score = score * 4;
        }

        // Create a new AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("You Won!")
                .setMessage("Congratulations! Your current score is " + score)
                .setCancelable(false) // Make dialog non-dismissible by tapping outside
                .setPositiveButton("Double it!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startNewGame();  // Start a new game if the user chooses to continue
                    }
                })
                .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Update the best score if the current score is higher
                        // Pass the final score to the previous activity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("score", score);  // Pass the final score
                        setResult(RESULT_OK, resultIntent);
                        final_dialog();  // Execute final dialog actions
                    }
                });

        // Show the dialog first
        AlertDialog dialog = builder.create();

        // Use setOnShowListener to access the buttons after the dialog is shown
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                // Set custom styles for positive and negative buttons
                positiveButton.setTextColor(getResources().getColor(R.color.ocean_blue)); // Custom color for "Double it!"
                negativeButton.setTextColor(Color.RED); // Red color for "Quit"
            }
        });

        // Display the dialog
        dialog.show();
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



        private void resetGame() {
            score = 1; // Reset score
            scoreLabel.setText("Score: " + score);
            startNewGame();
        }

        private void showExitDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
            builder.setTitle("Exit Game")
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }
