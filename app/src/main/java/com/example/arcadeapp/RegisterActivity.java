package com.example.arcadeapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput, confirmPasswordInput;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        registerButton = findViewById(R.id.registerButton);
        Button backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        registerButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "All fields are required", Toast.LENGTH_LONG).show();
                return;
            }

            if (username.length() < 3) {
                Toast.makeText(RegisterActivity.this, "Username must be at least 3 characters long", Toast.LENGTH_LONG).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters long", Toast.LENGTH_LONG).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_LONG).show();
                return;
            }

            new Thread(() -> {
                HttpURLConnection conn = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL(ServerConfig.BASE_URL + "/register");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(5000); // 5 second timeout
                    conn.setReadTimeout(5000);

                    JSONObject json = new JSONObject();
                    json.put("username", username);
                    json.put("password", password);

                    OutputStream os = conn.getOutputStream();
                    os.write(json.toString().getBytes());
                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    reader = new BufferedReader(new InputStreamReader(
                            responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    final String message = jsonResponse.optString("message", "");

                    runOnUiThread(() -> {
                        switch (responseCode) {
                            case 201:
                                Toast.makeText(RegisterActivity.this, 
                                    "Registration successful! Please login.", 
                                    Toast.LENGTH_LONG).show();
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                finish();
                                break;
                            case 400:
                                Toast.makeText(RegisterActivity.this, 
                                    message.isEmpty() ? "Invalid registration data" : message, 
                                    Toast.LENGTH_LONG).show();
                                break;
                            case 409:
                                Toast.makeText(RegisterActivity.this, 
                                    "Username already exists", 
                                    Toast.LENGTH_LONG).show();
                                break;
                            case 500:
                                Toast.makeText(RegisterActivity.this, 
                                    "Server error. Please try again later.", 
                                    Toast.LENGTH_LONG).show();
                                break;
                            default:
                                Toast.makeText(RegisterActivity.this, 
                                    "Unexpected error. Please try again.", 
                                    Toast.LENGTH_LONG).show();
                                break;
                        }
                    });
                } catch (java.net.SocketTimeoutException e) {
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, 
                        "Connection timed out. Please check your internet connection.", 
                        Toast.LENGTH_LONG).show());
                } catch (java.net.UnknownHostException e) {
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, 
                        "Cannot connect to server. Please check your internet connection.", 
                        Toast.LENGTH_LONG).show());
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, 
                        "An error occurred. Please try again later.", 
                        Toast.LENGTH_LONG).show());
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }).start();
        });
    }
}
