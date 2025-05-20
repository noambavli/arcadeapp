package com.example.arcadeapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

public class ServerUtils {
    private static final String TAG = "ServerUtils";
    
    // Custom exception classes for better error handling
    public static class ServerException extends Exception {
        private final int statusCode;
        private final String errorMessage;

        public ServerException(int statusCode, String errorMessage) {
            super(errorMessage);
            this.statusCode = statusCode;
            this.errorMessage = errorMessage;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public static class NetworkException extends Exception {
        public NetworkException(String message) {
            super(message);
        }
    }

    public static class AuthenticationException extends Exception {
        public AuthenticationException(String message) {
            super(message);
        }
    }

    // Interface for callback handling
    public interface ServerCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    // Method to handle server responses
    private static JSONObject handleServerResponse(HttpURLConnection conn) throws ServerException, IOException {
        int responseCode = conn.getResponseCode();
        BufferedReader reader;
        
        if (responseCode >= 200 && responseCode < 300) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        try {
            JSONObject jsonResponse = new JSONObject(response.toString());
            if (responseCode >= 400) {
                String errorMessage = jsonResponse.optString("message", "Unknown error occurred");
                throw new ServerException(responseCode, errorMessage);
            }
            return jsonResponse;
        } catch (JSONException e) {
            throw new ServerException(responseCode, "Invalid server response format");
        }
    }

    // Method to get user score with proper error handling
    public static void getUserScore(Context context, ServerCallback<Integer> callback) {
        new AsyncTask<Void, Void, Integer>() {
            private Exception error;

            @Override
            protected Integer doInBackground(Void... voids) {
                try {
                    URL url = new URL(ServerConfig.BASE_URL + "/get_score");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Authorization", "Bearer " + getJwtToken(context));

                    JSONObject response = handleServerResponse(conn);
                    
                    if (response.getString("status").equals("success") && response.has("score")) {
                        return response.getInt("score");
                    } else {
                        throw new ServerException(500, "Invalid response format");
                    }
                } catch (UnknownHostException e) {
                    error = new NetworkException("Cannot connect to server. Please check your internet connection.");
                    return null;
                } catch (IOException e) {
                    error = new NetworkException("Network error: " + e.getMessage());
                    return null;
                } catch (ServerException e) {
                    error = e;
                    return null;
                } catch (AuthenticationException e) {
                    error = e;
                    return null;
                } catch (Exception e) {
                    error = new ServerException(500, "Unexpected error: " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Integer score) {
                if (error != null) {
                    callback.onError(error);
                } else {
                    callback.onSuccess(score);
                }
            }
        }.execute();
    }

    // Method to update user score with proper error handling
    public static void updateUserScore(Context context, int newScore, ServerCallback<Void> callback) {
        new AsyncTask<Void, Void, Void>() {
            private Exception error;

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    URL url = new URL(ServerConfig.BASE_URL + "/update_score");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Authorization", "Bearer " + getJwtToken(context));
                    conn.setDoOutput(true);

                    JSONObject requestBody = new JSONObject();
                    requestBody.put("score", newScore);
                    conn.getOutputStream().write(requestBody.toString().getBytes());

                    handleServerResponse(conn);
                    return null;
                } catch (UnknownHostException e) {
                    error = new NetworkException("Cannot connect to server. Please check your internet connection.");
                    return null;
                } catch (IOException e) {
                    error = new NetworkException("Network error: " + e.getMessage());
                    return null;
                } catch (ServerException e) {
                    error = e;
                    return null;
                } catch (AuthenticationException e) {
                    error = e;
                    return null;
                } catch (Exception e) {
                    error = new ServerException(500, "Unexpected error: " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Void result) {
                if (error != null) {
                    callback.onError(error);
                } else {
                    callback.onSuccess(null);
                }
            }
        }.execute();
    }

    // Method to get JWT token from SharedPreferences
    private static String getJwtToken(Context context) throws AuthenticationException {
        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        if (token == null) {
            throw new AuthenticationException("No authentication token found");
        }
        return token;
    }

    // Utility method to show error messages
    public static void showError(Context context, Exception e) {
        String message;
        if (e instanceof NetworkException) {
            message = e.getMessage();
        } else if (e instanceof ServerException) {
            message = ((ServerException) e).getErrorMessage();
        } else if (e instanceof AuthenticationException) {
            message = "Authentication error: " + e.getMessage();
        } else {
            message = "An unexpected error occurred";
        }
        
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> 
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            );
        }
        Log.e(TAG, "Error: " + e.getMessage(), e);
    }
}
