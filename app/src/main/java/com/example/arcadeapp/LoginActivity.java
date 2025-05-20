package com.example.arcadeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText usernameInput, passwordInput;
    private Button loginButton, registerButton;
    private CheckBox rememberMeCheckbox;
    private TokenManager tokenManager;
    private Handler tokenRefreshHandler;
    private static final long TOKEN_REFRESH_INTERVAL = 45 * 1000; // 45 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "LoginActivity onCreate started");
        
        // Initialize views first
        setContentView(R.layout.activity_login);
        
        // Initialize views
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);
        
        tokenRefreshHandler = new Handler(Looper.getMainLooper());
        
        // Check if user is already logged in
        tokenManager = new TokenManager(this);
        if (tokenManager.isLoggedIn()) {
            Log.d(TAG, "User is logged in, proceeding to MainMenuActivity");
            // Start token refresh if remember me is enabled
            if (tokenManager.isRememberMeEnabled()) {
                Log.d(TAG, "Remember me is enabled, starting token refresh");
                startTokenRefresh();
            }
            // Navigate to MainMenuActivity
            Intent intent = new Intent(this, MainMenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }

        Log.d(TAG, "User is not logged in, showing login screen");

        // Check if we have saved credentials
        if (tokenManager.isRememberMeEnabled()) {
            Log.d(TAG, "Remember me is enabled, pre-filling credentials");
            usernameInput.setText(tokenManager.getUsername());
            passwordInput.setText(tokenManager.getSavedPassword());
            rememberMeCheckbox.setChecked(true);
        }

        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();
            boolean rememberMe = rememberMeCheckbox.isChecked();

            Log.d(TAG, "Login attempt for user: " + username);

            new Thread(() -> {
                try {
                    Log.d(TAG, "Starting login request to: " + ServerConfig.BASE_URL + "/login");
                    URL url = new URL(ServerConfig.BASE_URL + "/login");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(30000);
                    conn.setReadTimeout(30000);
                    conn.setInstanceFollowRedirects(true);

                    JSONObject json = new JSONObject();
                    json.put("username", username);
                    json.put("password", password);
                    json.put("remember_me", rememberMe);

                    String requestBody = json.toString();
                    Log.d(TAG, "Request body: " + requestBody);

                    OutputStream os = conn.getOutputStream();
                    os.write(requestBody.getBytes());
                    os.flush();
                    os.close();

                    Log.d(TAG, "Request sent, waiting for response...");
                    int responseCode = conn.getResponseCode();
                    Log.d(TAG, "Login response code: " + responseCode);

                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(
                                responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        Log.d(TAG, "Response body: " + response.toString());

                        JSONObject jsonResponse = new JSONObject(response.toString());
                        if (responseCode == 200) {
                            if (jsonResponse.getString("status").equals("success")) {
                                String token = jsonResponse.getString("token");
                                String username_response = jsonResponse.getString("username");
                                int score = jsonResponse.optInt("score", 0);
                                String message = jsonResponse.optString("message", "Login Successful");

                                Log.d(TAG, "Login successful for user: " + username_response);

                                // Save login state based on remember me preference
                                SharedPreferences.Editor editor = tokenManager.getPrefs().edit();
                                editor.putString("jwt_token", token);
                                editor.putString("username", username_response);
                                editor.putInt("score", score);
                                
                                if (rememberMe) {
                                    Log.d(TAG, "Saving credentials for remember me");
                                    editor.putBoolean("remember_me", true);
                                    editor.putString("saved_username", username);
                                    editor.putString("saved_password", password);
                                } else {
                                    Log.d(TAG, "Clearing saved credentials");
                                    editor.putBoolean("remember_me", false);
                                    editor.remove("saved_username");
                                    editor.remove("saved_password");
                                }
                                editor.apply();

                                // Start token refresh mechanism
                                startTokenRefresh();

                                runOnUiThread(() -> {
                                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this, MainMenuActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                });
                            } else {
                                String errorMessage = jsonResponse.optString("message", "Login failed. Please try again.");
                                Log.e(TAG, "Login failed: " + errorMessage);
                                runOnUiThread(() -> Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show());
                            }
                        } else if (responseCode == 401) {
                            Log.e(TAG, "Invalid credentials");
                            runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_LONG).show());
                        } else if (responseCode == 400) {
                            Log.e(TAG, "Missing credentials");
                            runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Please enter both username and password", Toast.LENGTH_LONG).show());
                        } else {
                            Log.e(TAG, "Server error: " + responseCode);
                            runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Unable to connect to server. Please try again later.", Toast.LENGTH_LONG).show());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading response", e);
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Error reading response. Please try again later.", Toast.LENGTH_LONG).show());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Network error", e);
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Network error. Please check your internet connection.", Toast.LENGTH_LONG).show());
                }
            }).start();
        });

        registerButton.setOnClickListener(v -> {
            Log.d(TAG, "Navigating to RegisterActivity");
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void startTokenRefresh() {
        Log.d(TAG, "Starting token refresh mechanism");
        tokenRefreshHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshToken();
                tokenRefreshHandler.postDelayed(this, TOKEN_REFRESH_INTERVAL);
            }
        }, TOKEN_REFRESH_INTERVAL);
    }

    private void refreshToken() {
        Log.d(TAG, "Refreshing token");
        String username = tokenManager.getUsername();
        String password = tokenManager.getSavedPassword();
        boolean rememberMe = tokenManager.isRememberMeEnabled();

        if (username == null || password == null) {
            Log.e(TAG, "Cannot refresh token: missing credentials");
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL(ServerConfig.BASE_URL + "/login");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);
                conn.setInstanceFollowRedirects(true);

                JSONObject json = new JSONObject();
                json.put("username", username);
                json.put("password", password);
                json.put("remember_me", rememberMe);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Token refresh response code: " + responseCode);

                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    if (jsonResponse.getString("status").equals("success")) {
                        String token = jsonResponse.getString("token");
                        Log.d(TAG, "Token refreshed successfully");
                        SharedPreferences.Editor editor = tokenManager.getPrefs().edit();
                        editor.putString("jwt_token", token);
                        editor.apply();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error refreshing token", e);
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "LoginActivity onDestroy");
        tokenRefreshHandler.removeCallbacksAndMessages(null);
    }
}
