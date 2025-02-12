package com.example.arcadeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GamesHomeActivity extends AppCompatActivity {


    private GridLayout gamesGridLayout;
    private ActivityResultLauncher<Intent> startForResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games_home);

        gamesGridLayout = findViewById(R.id.gamesGridLayout);

        // Initialize the ActivityResultLauncher
        startForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                int score = result.getData().getIntExtra("score", 0);
                int current_score = ServerUtils.getUserScore(GamesHomeActivity.this);
                int calculated_score = current_score+score;
                updateScore(calculated_score);  // Handle the score after returning from the game
            }
        });
        Typeface coolFont = ResourcesCompat.getFont(this, R.font.cool_font2);

        // Add a "Hangchairs" button dynamically
        Button hangchairsButton = new Button(this);
        hangchairsButton.setText("Hangchairs");
        hangchairsButton.setBackgroundColor(ContextCompat.getColor(gamesGridLayout.getContext(), R.color.vibrant_purple));
        hangchairsButton.setTypeface(coolFont);
        hangchairsButton.setOnClickListener(v -> {
            Intent intent = new Intent(GamesHomeActivity.this, HangchairsActivity.class);
            startForResult.launch(intent);  // Use the new way to start the activity
        });

        // Add a "Tic Tac Toe" button dynamically
        Button tictactoeButton = new Button(this);
        tictactoeButton.setText("Tic Tac Toe");
        tictactoeButton.setBackgroundColor(ContextCompat.getColor(gamesGridLayout.getContext(), R.color.vibrant_orange));
        tictactoeButton.setTypeface(coolFont);
        tictactoeButton.setOnClickListener(v -> {
            Intent intent = new Intent(GamesHomeActivity.this, TicTacToeActivity.class);
            startForResult.launch(intent);  // Use the new way to start the activity
        });

        Button guessnumberButton = new Button(this);
        guessnumberButton.setText("Guess the number");
        guessnumberButton.setBackgroundColor(ContextCompat.getColor(gamesGridLayout.getContext(), R.color.vibrant_red));
        guessnumberButton.setTypeface(coolFont);
        guessnumberButton.setOnClickListener(v -> {
            Intent intent = new Intent(GamesHomeActivity.this, guessNumberActivity.class);
            startForResult.launch(intent);  // Use the new way to start the activity
        });

        Button fallingBlocksButton = new Button(this);
        fallingBlocksButton.setText("Catch the block");
        fallingBlocksButton.setTypeface(coolFont);
        fallingBlocksButton.setBackgroundColor(ContextCompat.getColor(gamesGridLayout.getContext(), R.color.vibrant_yellow));

        fallingBlocksButton.setOnClickListener(v -> {
            Intent intent = new Intent(GamesHomeActivity.this, com.example.arcadeapp.FallingblocksActivity.class);
            startForResult.launch(intent);  // Use the new way to start the activity
        });

        Button redButtonButton = new Button(this);
        redButtonButton.setText("Red button ");
        redButtonButton.setBackgroundColor(ContextCompat.getColor(gamesGridLayout.getContext(), R.color.vibrant_green));
        redButtonButton.setTypeface(coolFont);
        redButtonButton.setOnClickListener(v -> {
            Intent intent = new Intent(GamesHomeActivity.this, com.example.arcadeapp.RedButtonActivity.class);
            startForResult.launch(intent);  // Use the new way to start the activity
        });

        Button triviaButton = new Button(this);
        triviaButton.setText("Trivia ");
        triviaButton.setTypeface(coolFont);
        triviaButton.setBackgroundColor(ContextCompat.getColor(gamesGridLayout.getContext(), R.color.vibrant_blue));

        triviaButton.setOnClickListener(v -> {
            Intent intent = new Intent(GamesHomeActivity.this, com.example.arcadeapp.triviaActivity.class);
            startForResult.launch(intent);  // Use the new way to start the activity
        });



        // Add buttons to the grid layout
// Add the buttons to the GridLayout
        gamesGridLayout.addView(guessnumberButton);
        gamesGridLayout.addView(triviaButton);
        gamesGridLayout.addView(redButtonButton);
        gamesGridLayout.addView(fallingBlocksButton);
        gamesGridLayout.addView(hangchairsButton);
        gamesGridLayout.addView(tictactoeButton);
    }

    private void updateScore(int newScore) {
        // Assuming you already have the token saved in SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        if (!token.isEmpty()) {
            new Thread(() -> {
                try {
                    URL url = new URL(ServerConfig.BASE_URL + "/update_score");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                    conn.setDoOutput(true);

                    JSONObject json = new JSONObject();
                    json.put("score", newScore);

                    OutputStream os = conn.getOutputStream();
                    os.write(json.toString().getBytes());
                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        // Update score in SharedPreferences
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("score", newScore);  // Update the score
                        editor.apply();
                        runOnUiThread(() -> Toast.makeText(GamesHomeActivity.this, "Score updated", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(GamesHomeActivity.this, "Failed to update score", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(GamesHomeActivity.this, "Error updating score", Toast.LENGTH_SHORT).show());
                }
            }).start();
        }
    }

}
