package com.example.arcadeapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.Random;

public class TicTacToeActivity extends AppCompatActivity {
    private int[] board = new int[9]; // 0 - empty, 1 - player, 2 - computer
    private int score = 0;
    private boolean gameOver = false;
        private Button exitButton;

    private TextView statusTextView, scoreTextView;
    private Button[] buttons = new Button[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tictactoe);
        exitButton = findViewById(R.id.exitButton);

        statusTextView = findViewById(R.id.statusTextView);
        scoreTextView = findViewById(R.id.scoreTextView);

        for (int i = 0; i < 9; i++) {
            String buttonID = "button" + i;
            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
            buttons[i] = findViewById(resID);
            final int finalI = i;
            buttons[i].setOnClickListener(v -> handleClick(finalI));
        }

        // Initialize the board to empty
        Arrays.fill(board, 0);
        updateUI();
    }



    private void handleClick(int position) {
        if (board[position] != 0 || gameOver) return; // If the cell is already filled or game is over, do nothing

        // Player's move
        board[position] = 1;
        buttons[position].setText("X");

        if (checkWinner()) {
            gameOver = true;
            statusTextView.setText("You Won!");
            scoreTextView.setText("score: 70");
            winGame();
            return;
        }

        // Check for a tie
        if (isBoardFull()) {
            gameOver = true;
            statusTextView.setText("It's a Tie!");
            final_dialog();
            return;
        }

        // Computer's move
        computerMove();

        if (checkWinner()) {
            gameOver = true;
            statusTextView.setText("Computer Wins!");
            final_dialog();
            return;
        }

        if (isBoardFull()) {
            gameOver = true;
            statusTextView.setText("It's a Tie!");
        }
    }
    private void winGame() {
        int new_score = 70;
        // Pass the score back to the parent activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("score", new_score);
        setResult(RESULT_OK, resultIntent);
        final_dialog();

        // Finish the current activity

    }

    private void computerMove() {
        int position = -1;

        // Try to win if possible
        position = findWinningMove(2);
        if (position == -1) {
            // Try to block the player's winning move
            position = findWinningMove(1);
        }
        if (position == -1) {
            // If no winning or blocking move, choose randomly
            Random rand = new Random();
            do {
                position = rand.nextInt(9);
            } while (board[position] != 0);
        }

        board[position] = 2;
        buttons[position].setText("O");
    }

    // Check if the current player (1 for X, 2 for O) can win in any position
    private int findWinningMove(int player) {
        for (int i = 0; i < 9; i++) {
            if (board[i] == 0) {
                board[i] = player;  // Temporarily make the move
                if (checkWin(player)) {
                    board[i] = 0;  // Undo the move
                    return i;
                }
                board[i] = 0;  // Undo the move
            }
        }
        return -1;
    }

    // Check if the current player has won
    private boolean checkWin(int player) {
        int[][] winConditions = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8},  // Rows
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8},  // Columns
                {0, 4, 8}, {2, 4, 6}               // Diagonals
        };

        for (int[] condition : winConditions) {
            if (board[condition[0]] == player && board[condition[1]] == player && board[condition[2]] == player) {
                return true;
            }
        }
        return false;
    }

    private boolean checkWinner() {
        // Check rows, columns, and diagonals
        for (int i = 0; i < 3; i++) {
            // Check rows
            if (board[i * 3] == board[i * 3 + 1] && board[i * 3 + 1] == board[i * 3 + 2] && board[i * 3] != 0) {
                return true;
            }
            // Check columns
            if (board[i] == board[i + 3] && board[i + 3] == board[i + 6] && board[i] != 0) {
                return true;
            }
        }
        // Check diagonals
        if (board[0] == board[4] && board[4] == board[8] && board[0] != 0) {
            return true;
        }
        if (board[2] == board[4] && board[4] == board[6] && board[2] != 0) {
            return true;
        }
        return false;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 9; i++) {
            if (board[i] == 0) return false;
        }
        return true;
    }

    private void updateUI() {
        for (int i = 0; i < 9; i++) {
            if (board[i] == 0) {
                buttons[i].setText("");
                buttons[i].setEnabled(true);
            } else if (board[i] == 1) {
                buttons[i].setText("X");
                buttons[i].setEnabled(false);
            } else {
                buttons[i].setText("O");
                buttons[i].setEnabled(false);
            }
        }
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
    }
    private void saveScore(int score) {
        // Retrieve the current score from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("AppData", MODE_PRIVATE);
        int currentScore = prefs.getInt("score", 0);  // Default to 0 if no score exists

        // Add the new score to the existing score
        int newScore = currentScore + score;

        // Save the updated score back to SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("score", newScore);
        editor.apply();
    }

    public void exitGame(View view) {
        finish(); // Close the activity
    }
}
