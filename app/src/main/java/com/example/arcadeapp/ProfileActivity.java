package com.example.arcadeapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProfileActivity extends AppCompatActivity {

    private TextView usernameText, scoreText;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        usernameText = findViewById(R.id.usernameText);  // Correct reference
        scoreText = findViewById(R.id.scoreText);  // Correct reference

        // Get the saved JWT token from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        token = prefs.getString("jwt_token", null);

        if (token == null) {
            Toast.makeText(ProfileActivity.this, "You need to log in first.", Toast.LENGTH_SHORT).show();
            finish();  // Exit if there's no token
            return;
        }

        // Get the username from SharedPreferences
        String username = prefs.getString("username", "Guest");

        // Verify token integrity and fetch the user's profile and score from the server
        new Thread(() -> {
            try {
                // Make a request to the server to verify the token and get user profile
                URL url = new URL(ServerConfig.BASE_URL + "/profile");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);  // Pass JWT token in the Authorization header

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

                    // Parse the response JSON
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String serverUsername = jsonResponse.getString("username");
                    int score = jsonResponse.getInt("score");

                    // Update UI with the profile data
                    runOnUiThread(() -> {
                        usernameText.setText("Username: " + serverUsername);
                        scoreText.setText("Score: " + score);
                    });
                } else {
                    // Invalid token or error fetching profile
                    runOnUiThread(() -> {
                        Toast.makeText(ProfileActivity.this, "Error: Invalid token or unable to fetch profile", Toast.LENGTH_SHORT).show();
                        finish();  // Close the activity if there's an error
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(ProfileActivity.this, "Error verifying token or fetching profile", Toast.LENGTH_SHORT).show();
                    finish();  // Close the activity if there's an exception
                });
            }
        }).start();
    }
}
