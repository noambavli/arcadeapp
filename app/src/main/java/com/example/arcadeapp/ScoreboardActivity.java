package com.example.arcadeapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ScoreboardActivity extends AppCompatActivity {

    private ListView scoreboardListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> leaderboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);

        scoreboardListView = findViewById(R.id.scoreboardListView);
        leaderboard = new ArrayList<>();

        // Load and fetch the scores from the server
        loadScoresFromServer();
    }

    private void loadScoresFromServer() {
        // Retrieve the token from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        if (token == null) {
            Toast.makeText(this, "No valid token found. Please log in.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                // Construct the URL for fetching the scoreboard
                URL url = new URL(ServerConfig.BASE_URL + "/scoreboard");  // Make sure to define this endpoint in your backend
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    // Read the response from the server
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Parse the response into a JSONArray
                    JSONArray jsonResponse = new JSONArray(response.toString());

                    // Clear the current leaderboard
                    leaderboard.clear();

                    // Loop through the JSON array and add usernames and scores to the leaderboard
                    for (int i = 0; i < jsonResponse.length(); i++) {
                        JSONObject user = jsonResponse.getJSONObject(i);
                        String username = user.getString("username");
                        int score = user.getInt("score");
                        leaderboard.add(username + ": " + score);
                    }

                    // Update the ListView with the new leaderboard
                    runOnUiThread(() -> {
                        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, leaderboard);
                        scoreboardListView.setAdapter(adapter);
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to load scoreboard", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error loading scoreboard", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
