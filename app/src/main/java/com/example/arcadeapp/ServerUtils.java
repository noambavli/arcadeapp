package com.example.arcadeapp;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerUtils {

    public static int getUserScore(Context context) {
        int score = -1;  // Default score when there's an error

        try {
            // Use the base URL from ServerConfig
            URL url = new URL(ServerConfig.BASE_URL + "/get_score");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + getJwtToken(context));

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.has("score")) {
                    score = jsonResponse.getInt("score");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return score;  // Return the score (or -1 if error)
    }

    // Method to get JWT token from SharedPreferences
    private static String getJwtToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getString("jwt_token", null);
    }
}
