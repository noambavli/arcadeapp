package com.example.arcadeapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Date;

public class TokenManager {
    private static final String TAG = "TokenManager";
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_SCORE = "score";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_SAVED_PASSWORD = "saved_password";

    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        String token = prefs.getString(KEY_TOKEN, null);
        Log.d(TAG, "Checking login status. Token exists: " + (token != null));
        
        if (token == null) {
            return false;
        }

        try {
            // Decode the JWT token
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                Log.e(TAG, "Invalid token format");
                return false;
            }

            // Decode the payload
            String payload = new String(android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT));
            org.json.JSONObject json = new org.json.JSONObject(payload);

            // Check expiration
            long exp = json.getLong("exp") * 1000; // Convert to milliseconds
            long currentTime = System.currentTimeMillis();
            boolean isValid = exp > currentTime;
            
            Log.d(TAG, String.format("Token validation - Expiration: %d, Current: %d, Valid: %b", 
                exp, currentTime, isValid));
            
            return isValid;
        } catch (Exception e) {
            Log.e(TAG, "Error checking token: " + e.getMessage());
            return false;
        }
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    public int getScore() {
        return prefs.getInt(KEY_SCORE, 0);
    }

    public boolean isRememberMeEnabled() {
        return prefs.getBoolean(KEY_REMEMBER_ME, false);
    }

    public String getSavedPassword() {
        return prefs.getString(KEY_SAVED_PASSWORD, null);
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }

    public void clearSession() {
        Log.d(TAG, "Clearing session data");
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_SCORE);
        editor.remove(KEY_REMEMBER_ME);
        editor.remove(KEY_SAVED_PASSWORD);
        editor.apply();
    }
} 