package com.bautista.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Register extends AppCompatActivity {

    EditText etUsername, etPassword;
    CheckBox cbAgree;
    Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        cbAgree = findViewById(R.id.cbAgree);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnSignUp.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!cbAgree.isChecked()) {
                Toast.makeText(this, "You must agree to the Terms and Conditions", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("CalmaUser", MODE_PRIVATE);
            prefs.edit()
                    .putString("username", username)
                    .putString("password", password)
                    .putBoolean("isNewUser", true)
                    .apply();

            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
