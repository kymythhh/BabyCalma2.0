package com.bautista.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        continuebtn = findViewById(R.id.continuebtn);
        tvSignUp = findViewById(R.id.tvSignUp);

        SharedPreferences prefs = getSharedPreferences("CalmaUsers", MODE_PRIVATE);

        continuebtn.setOnClickListener(v -> {
            String inputUser = username.getText().toString();
            String inputPass = password.getText().toString();

            String savedUser = prefs.getString("username", null);
            String savedPass = prefs.getString("password", null);

            if (inputUser.equals(savedUser) && inputPass.equals(savedPass)) {
                Intent intent = new Intent(MainActivity.this, Dashboard.class);
                intent.putExtra("username", inputUser);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });

        tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, Register.class))
        );
    }
}

