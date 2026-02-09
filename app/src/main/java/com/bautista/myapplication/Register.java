package com.bautista.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashSet;
import java.util.Set;

public class Register extends AppCompatActivity {

    EditText etUsername, etPassword;
    CheckBox cbAgree;
    Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        try {
            etUsername = findViewById(R.id.etUsername);
            etPassword = findViewById(R.id.etPassword);
            cbAgree = findViewById(R.id.cbAgree);
            btnSignUp = findViewById(R.id.btnSignUp);

            btnSignUp.setOnClickListener(v -> {
                try {
                    String username = etUsername.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();

                    // Basic Validation
                    if (username.isEmpty() || password.isEmpty()) {
                        Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!cbAgree.isChecked()) {
                        Toast.makeText(this, "You must agree to the Terms and Conditions", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    SharedPreferences prefs = getSharedPreferences("CalmaUser", MODE_PRIVATE);

                    // --- ERROR HANDLING: DUPLICATE CHECK ---
                    // Retrieve existing usernames. SharedPreferences returns a copy of the set.
                    Set<String> registeredUsers = prefs.getStringSet("all_usernames", new HashSet<>());

                    if (registeredUsers.contains(username)) {
                        Toast.makeText(this, "Username '" + username + "' already exists. Try another.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // --- SAVE DATA LOCALLY ---
                    SharedPreferences.Editor editor = prefs.edit();

                    // Save specific password for THIS username
                    editor.putString("password_" + username, password);

                    // Add new username to the list of all accounts
                    Set<String> updatedUsers = new HashSet<>(registeredUsers);
                    updatedUsers.add(username);
                    editor.putStringSet("all_usernames", updatedUsers);

                    // Set current session details
                    editor.putString("username", username);
                    editor.putBoolean("isNewUser", true);

                    if (editor.commit()) {
                        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Storage error. Please try again.", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    Log.e("Register", "Error during sign up: " + e.getMessage());
                    Toast.makeText(this, "An unexpected error occurred", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("Register", "Initialization error: " + e.getMessage());
        }
    }
}