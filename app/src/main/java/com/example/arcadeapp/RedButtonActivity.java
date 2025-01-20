// RedButtonActivity.java
package com.example.arcadeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.arcadeapp.R;

public class RedButtonActivity extends AppCompatActivity {

    private int score = 0;
    private TextView scoreText;
    private Button redButton, exitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_red_button);

        scoreText = findViewById(R.id.scoreText);
        redButton = findViewById(R.id.redButton);
        exitButton = findViewById(R.id.exitButton);

        // Button click listener to increase score
        redButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                score++;
                scoreText.setText("Score: " + score);
            }
        });

        // Exit button click listener to close the activity
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("score", score);
                setResult(RESULT_OK, resultIntent);
                finish(); // Closes the current activity
            }
        });
    }
}
