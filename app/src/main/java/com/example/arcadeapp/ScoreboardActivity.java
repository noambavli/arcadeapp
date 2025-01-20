package com.example.arcadeapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

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

        // Retrieve and sort the user scores
        loadAndSortScores();

        // Set the adapter to display the scores
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, leaderboard);
        scoreboardListView.setAdapter(adapter);
    }

    private void loadAndSortScores() {
        SharedPreferences prefs = getSharedPreferences("AppData", MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();
        ArrayList<Map.Entry<String, ?>> entries = new ArrayList<>(allEntries.entrySet());

        // Sort the scores in descending order
        Collections.sort(entries, (o1, o2) -> Integer.compare((int) o2.getValue(), (int) o1.getValue()));

        // Add the sorted leaderboard to the array list
        leaderboard.clear();
        for (Map.Entry<String, ?> entry : entries) {
            if (entry.getKey().endsWith("_score")) {  // Only include score entries
                String username = entry.getKey().replace("_score", "");
                int score = (int) entry.getValue();
                leaderboard.add(username + ": " + score);
            }
        }
    }
}
