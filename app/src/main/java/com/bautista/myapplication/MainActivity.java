package com.bautista.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText username, password;
    Button continuebtn;
    TextView tvSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            username = findViewById(R.id.username);
            password = findViewById(R.id.password);
            continuebtn = findViewById(R.id.continuebtn);
            tvSignUp = findViewById(R.id.tvSignUp);

            SharedPreferences userPrefs = getSharedPreferences("CalmaUser", MODE_PRIVATE);

            continuebtn.setOnClickListener(v -> {
                try {
                    String inputUser = username.getText().toString().trim();
                    String inputPass = password.getText().toString().trim();

                    if (inputUser.isEmpty() || inputPass.isEmpty()) {
                        Toast.makeText(this, "Please enter both credentials", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // --- SEARCH FOR USER-SPECIFIC DATA ---
                    // Retrieve the password tied to this specific username
                    String savedPass = userPrefs.getString("password_" + inputUser, null);

                    if (savedPass != null && inputPass.equals(savedPass)) {
                        // Mark this user as the active session user
                        userPrefs.edit().putString("username", inputUser).apply();

                        boolean isNew = userPrefs.getBoolean("isNewUser", true);

                        Intent intent = new Intent(MainActivity.this, Dashboard.class);
                        intent.putExtra("username", inputUser);
                        intent.putExtra("isNewUser", isNew);
                        startActivity(intent);
                    } else {
                        // Either username doesn't exist (savedPass is null) or password wrong
                        Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "Error during login: " + e.getMessage());
                    Toast.makeText(this, "Login error. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });

            tvSignUp.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, Register.class))
            );
        } catch (Exception e) {
            Log.e("MainActivity", "Error in onCreate: " + e.getMessage());
        }
    }
}