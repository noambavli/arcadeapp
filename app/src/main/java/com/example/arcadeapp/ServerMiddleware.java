package com.example.arcadeapp;

import android.util.Base64;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.*;

public class ServerMiddleware {
    private static final String SERVER_URL = "http://<server-ip>:5000";
    private static final OkHttpClient client = new OkHttpClient();
    private PublicKey serverPublicKey;
    private KeyPair clientKeyPair;

    // Initialize RSA keys
    public ServerMiddleware() {
        try {
            // Generate RSA key pair for client
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            clientKeyPair = keyGen.generateKeyPair();

            // Fetch server's public key
            fetchServerPublicKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Fetch the server's public key
    private void fetchServerPublicKey() {
        Request request = new Request.Builder()
                .url(SERVER_URL + "/get_public_key")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String publicKeyString = response.body().string();
                        byte[] publicKeyBytes = Base64.decode(publicKeyString, Base64.DEFAULT);
                        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);
                        KeyFactory keyFactory = KeyFactory.getInstance("RSA", new BouncyCastleProvider());
                        serverPublicKey = keyFactory.generatePublic(spec);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    // Generate a random AES key
    private SecretKey generateAESKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128); // AES-128
            return keyGen.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // RSA encryption
    private String encryptWithRSA(String data) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", new BouncyCastleProvider());
            cipher.init(Cipher.ENCRYPT_MODE, serverPublicKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // AES encryption
    private String encryptWithAES(String data, SecretKey aesKey) {
        try {
            byte[] iv = new byte[16];
            new Random().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);

            byte[] encryptedData = cipher.doFinal(data.getBytes());
            String encryptedBase64 = Base64.encodeToString(encryptedData, Base64.DEFAULT);
            String ivBase64 = Base64.encodeToString(iv, Base64.DEFAULT);

            return ivBase64 + ":" + encryptedBase64; // Combine IV and encrypted data
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // AES decryption
    private String decryptWithAES(String data, SecretKey aesKey) {
        try {
            String[] parts = data.split(":");
            byte[] iv = Base64.decode(parts[0], Base64.DEFAULT);
            byte[] encryptedData = Base64.decode(parts[1], Base64.DEFAULT);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);

            byte[] decryptedBytes = cipher.doFinal(encryptedData);
            return new String(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Send an encrypted request
    public void sendRequest(String endpoint, String jsonPayload, Callback callback) {
        try {
            // Generate AES key
            SecretKey aesKey = generateAESKey();

            // Encrypt payload with AES
            String encryptedPayload = encryptWithAES(jsonPayload, aesKey);

            // Encrypt AES key with RSA
            String encryptedAESKey = encryptWithRSA(Base64.encodeToString(aesKey.getEncoded(), Base64.DEFAULT));

            // Create final payload
            String finalPayload = "{\"aes_key\":\"" + encryptedAESKey + "\",\"data\":\"" + encryptedPayload + "\"}";

            RequestBody body = RequestBody.create(finalPayload, MediaType.parse("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(SERVER_URL + "/" + endpoint)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
