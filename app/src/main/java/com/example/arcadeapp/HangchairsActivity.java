package com.example.arcadeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class HangchairsActivity extends AppCompatActivity {

    private ImageView hangchairImage;
    private TextView wordTextView, attemptsTextView, statusTextView, guessedLettersTextView;
    private EditText guessInput;
    private Button submitButton, quitButton;
    private Button exitButton;


    private List<String> wordsToGuess = List.of(
            "CHAIRS", "TABLE", "HANG", "HOME", "LEG",
            "WINDOW", "DOOR", "DESK", "LAMP", "COUCH",
            "RUG", "SHELF", "PILLOW", "CARPET", "CUPBOARD",
            "CABINET", "FLOOR", "WALL", "SOFA"
    );    private String wordToGuess;
    private StringBuilder currentGuess;
    private Set<Character> guessedLetters;  // To keep track of already guessed letters
    private int attemptsLeft = 6;
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hangchairs);



        // Initialize views
        hangchairImage = findViewById(R.id.hangchair_image);
        wordTextView = findViewById(R.id.word_text_view);
        attemptsTextView = findViewById(R.id.attempts_text_view);
        statusTextView = findViewById(R.id.status_text_view);
        guessedLettersTextView = findViewById(R.id.guessed_letters_text_view);  // TextView for showing guessed letters
        guessInput = findViewById(R.id.guess_input);
        submitButton = findViewById(R.id.submit_button);
        exitButton = findViewById(R.id.exit_button);


        // Pick a random word from the list
        Random random = new Random();
        wordToGuess = wordsToGuess.get(random.nextInt(wordsToGuess.size()));

        // Initialize current guess with underscores
        currentGuess = new StringBuilder();
        guessedLetters = new HashSet<>();

        for (int i = 0; i < wordToGuess.length(); i++) {
            currentGuess.append("_");
        }

        // Display the initial word with underscores
        wordTextView.setText(currentGuess.toString());

        // Submit button logic
        submitButton.setOnClickListener(v -> {
            String guess = guessInput.getText().toString().toUpperCase();

            if (guess.length() == 1) {
                processGuess(guess);
            }
        });


        // Quit button logic (exit the game)
        exitButton.setOnClickListener(v -> {
            finish();  // Exit the game activity when the player presses "Exit"
        });
    }    private void gameEnded() {
        // Show the Exit button when the game ends
        exitButton.setVisibility(View.VISIBLE);

    }
    private void processGuess(String guess) {
        if (guessedLetters.contains(guess.charAt(0))) {
            // If the letter was already guessed, ignore the guess
            return;
        }

        boolean correctGuess = false;
        for (int i = 0; i < wordToGuess.length(); i++) {
            if (wordToGuess.charAt(i) == guess.charAt(0)) {
                currentGuess.setCharAt(i, guess.charAt(0));
                correctGuess = true;
            }
        }

        // Update the displayed word
        wordTextView.setText(currentGuess.toString());

        // Check if the user guessed the word
        if (currentGuess.toString().equals(wordToGuess)) {
            winGame();
        } else {
            if (!correctGuess) {
                guessedLetters.add(guess.charAt(0));

                // Update guessed letters display
                guessedLettersTextView.setText("Guessed: " + guessedLetters.toString());
                attemptsLeft--;
                hangchairImage.setImageResource(getImageResourceForAttemptsLeft());
                attemptsTextView.setText("Attempts Left: " + attemptsLeft);
            }

            // Check if attempts are finished
            if (attemptsLeft == 0) {
                loseGame();
            }
        }

        // Clear input field
        guessInput.setText("");
    }

    private void winGame() {
        int new_score = attemptsLeft * 20;
        guessedLettersTextView.setText("Win! Score: " + new_score);
        hangchairImage.setImageResource(R.drawable.hangchairs_win);

        // Pass the score back to the parent activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("score", new_score);
        setResult(RESULT_OK, resultIntent);

    }


    private void loseGame() {
        guessedLettersTextView.setText("Game Over! the word was: " + wordToGuess);
        hangchairImage.setImageResource(R.drawable.hangchairs_lose);

    }


    private int getImageResourceForAttemptsLeft() {
        switch (attemptsLeft) {
            case 5: return R.drawable.hangchairs5;
            case 4: return R.drawable.hangchairs4;
            case 3: return R.drawable.hangchairs3;
            case 2: return R.drawable.hangchairs2;
            case 1: return R.drawable.hangchairs1;
            case 0: return R.drawable.hangchairs_lose;
            default: return R.drawable.hangchairs6;
        }
    }
}
