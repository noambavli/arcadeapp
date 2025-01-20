package com.example.arcadeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();
            String credentials = username + ":" + password;

            // Fetch public key and encrypt credentials
            new Thread(() -> {
                try {
                    URL url = new URL("http://<server-ip>:5000/get_public_key");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    InputStream is = conn.getInputStream();
                    String publicKeyString = new BufferedReader(new InputStreamReader(is)).readLine();

                    // Encrypt credentials with public key
                    PublicKey publicKey = KeyFactory.getInstance("RSA")
                            .generatePublic(new X509EncodedKeySpec(Base64.decode(publicKeyString, Base64.DEFAULT)));
                    Cipher cipher = Cipher.getInstance("RSA");
                    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                    byte[] encryptedData = cipher.doFinal(credentials.getBytes());

                    // Send encrypted data to the server
                    URL registerUrl = new URL("http://<server-ip>:5000/register");
                    HttpURLConnection registerConn = (HttpURLConnection) registerUrl.openConnection();
                    registerConn.setRequestMethod("POST");
                    registerConn.setRequestProperty("Content-Type", "application/json");
                    registerConn.setDoOutput(true);

                    String json = "{\"data\":\"" + Base64.encodeToString(encryptedData, Base64.DEFAULT) + "\"}";
                    OutputStream os = registerConn.getOutputStream();
                    os.write(json.getBytes());
                    os.flush();

                    // Handle server response
                    InputStream responseStream = registerConn.getInputStream();
                    String response = new BufferedReader(new InputStreamReader(responseStream)).readLine();
                    runOnUiThread(() -> {
                        if (response.contains("success")) {
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        } else {
                            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }
}
