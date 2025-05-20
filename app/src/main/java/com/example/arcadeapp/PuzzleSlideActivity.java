package com.example.arcadeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Random;

public class PuzzleSlideActivity extends AppCompatActivity {
    private GridLayout puzzleGrid;
    private TextView movesText;
    private TextView timerText;
    private TextView levelText;
    private Button newGameButton;
    private Button exitButton;
    private Button easyButton;
    private Button mediumButton;
    private Button hardButton;
    private Button backButton;
    private ArrayList<Button> tiles;
    private int emptyTileIndex;
    private int moves;
    private int seconds;
    private boolean isGameRunning;
    private Handler timerHandler;
    private Random random;
    private int difficultyLevel = 1; // 1: Easy, 2: Medium, 3: Hard

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_slide);

        puzzleGrid = findViewById(R.id.puzzleGrid);
        movesText = findViewById(R.id.movesText);
        timerText = findViewById(R.id.timerText);
        levelText = findViewById(R.id.levelText);
        newGameButton = findViewById(R.id.newGameButton);
        exitButton = findViewById(R.id.exit_button);
        backButton = findViewById(R.id.backButton);
        easyButton = findViewById(R.id.easyButton);
        mediumButton = findViewById(R.id.mediumButton);
        hardButton = findViewById(R.id.hardButton);

        tiles = new ArrayList<>();
        random = new Random();
        timerHandler = new Handler(Looper.getMainLooper());

        newGameButton.setOnClickListener(v -> startNewGame());
        exitButton.setOnClickListener(v -> finish());
        backButton.setOnClickListener(v -> finish());
        easyButton.setOnClickListener(v -> setDifficulty(1));
        mediumButton.setOnClickListener(v -> setDifficulty(2));
        hardButton.setOnClickListener(v -> setDifficulty(3));

        setDifficulty(1); // Start with easy difficulty
        startNewGame();
    }

    private void setDifficulty(int level) {
        if (difficultyLevel == level) return; // Don't restart if same level
        
        difficultyLevel = level;
        String levelName = level == 1 ? "Easy" : level == 2 ? "Medium" : "Hard";
        levelText.setText("Level: " + levelName);
        
        // Update button colors to show selected difficulty
        easyButton.setBackgroundColor(level == 1 ? Color.parseColor("#4CAF50") : Color.parseColor("#808080"));
        mediumButton.setBackgroundColor(level == 2 ? Color.parseColor("#4CAF50") : Color.parseColor("#808080"));
        hardButton.setBackgroundColor(level == 3 ? Color.parseColor("#4CAF50") : Color.parseColor("#808080"));
        
        // Automatically start a new game with the selected difficulty
        startNewGame();
    }

    private void startNewGame() {
        // Clear existing tiles
        puzzleGrid.removeAllViews();
        tiles.clear();
        moves = 0;
        seconds = 0;
        movesText.setText("Moves: 0");
        timerText.setText("Time: 0");
        isGameRunning = true;

        // Create tiles
        for (int i = 0; i < 8; i++) {
            Button tile = createTile(i + 1);
            tiles.add(tile);
            puzzleGrid.addView(tile);
        }
        // Add empty tile
        Button emptyTile = createTile(0);
        emptyTile.setVisibility(View.INVISIBLE);
        tiles.add(emptyTile);
        puzzleGrid.addView(emptyTile);
        emptyTileIndex = 8;

        // Shuffle tiles based on difficulty
        shuffleTiles();

        // Start timer
        startTimer();
    }

    private Button createTile(int number) {
        Button tile = new Button(this);
        tile.setText(number > 0 ? String.valueOf(number) : "");
        tile.setTextSize(24);
        tile.setTextColor(Color.WHITE);
        tile.setBackgroundColor(Color.parseColor("#4CAF50"));
        
        // Set tile size
        int size = getResources().getDisplayMetrics().widthPixels / 3 - 32;
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = size;
        params.height = size;
        params.setMargins(4, 4, 4, 4);
        tile.setLayoutParams(params);

        tile.setOnClickListener(v -> onTileClick(tiles.indexOf(tile)));
        return tile;
    }

    private void shuffleTiles() {
        // Number of moves based on difficulty
        int shuffleMoves = difficultyLevel == 1 ? 20 : difficultyLevel == 2 ? 50 : 100;
        
        // Perform random moves to shuffle
        for (int i = 0; i < shuffleMoves; i++) {
            ArrayList<Integer> possibleMoves = getPossibleMoves();
            if (!possibleMoves.isEmpty()) {
                int randomMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
                swapTiles(emptyTileIndex, randomMove);
                emptyTileIndex = randomMove;
            }
        }
    }

    private ArrayList<Integer> getPossibleMoves() {
        ArrayList<Integer> moves = new ArrayList<>();
        int row = emptyTileIndex / 3;
        int col = emptyTileIndex % 3;

        // Check all four directions
        if (row > 0) moves.add(emptyTileIndex - 3); // Up
        if (row < 2) moves.add(emptyTileIndex + 3); // Down
        if (col > 0) moves.add(emptyTileIndex - 1); // Left
        if (col < 2) moves.add(emptyTileIndex + 1); // Right

        return moves;
    }

    private void onTileClick(int clickedIndex) {
        if (!isGameRunning) return;

        ArrayList<Integer> possibleMoves = getPossibleMoves();
        if (possibleMoves.contains(clickedIndex)) {
            swapTiles(emptyTileIndex, clickedIndex);
            emptyTileIndex = clickedIndex;
            moves++;
            movesText.setText("Moves: " + moves);

            if (isPuzzleSolved()) {
                gameWon();
            }
        }
    }

    private void swapTiles(int index1, int index2) {
        Button tile1 = tiles.get(index1);
        Button tile2 = tiles.get(index2);

        String tempText = tile1.getText().toString();
        tile1.setText(tile2.getText().toString());
        tile2.setText(tempText);

        int tempVisibility = tile1.getVisibility();
        tile1.setVisibility(tile2.getVisibility());
        tile2.setVisibility(tempVisibility);
    }

    private boolean isPuzzleSolved() {
        for (int i = 0; i < tiles.size() - 1; i++) {
            String tileText = tiles.get(i).getText().toString();
            if (!tileText.equals(String.valueOf(i + 1))) {
                return false;
            }
        }
        return true;
    }

    private void startTimer() {
        timerHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isGameRunning) {
                    seconds++;
                    timerText.setText("Time: " + seconds);
                    timerHandler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void gameWon() {
        isGameRunning = false;
        
        // Calculate score based on moves, time, and difficulty
        int baseScore = 1000;
        int movePenalty = moves * 10;
        int timePenalty = seconds * 5;
        int difficultyMultiplier = difficultyLevel == 1 ? 1 : difficultyLevel == 2 ? 2 : 3;
        
        int finalScore = Math.max(0, (baseScore - movePenalty - timePenalty) * difficultyMultiplier);
        
        String message = "Congratulations!\nMoves: " + moves + 
                        "\nTime: " + seconds + " seconds" +
                        "\nScore: " + finalScore;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // Send score back to previous activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("score", finalScore);
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
        if (!isGameRunning && !isPuzzleSolved()) {
            isGameRunning = true;
            startTimer();
        }
    }
} 