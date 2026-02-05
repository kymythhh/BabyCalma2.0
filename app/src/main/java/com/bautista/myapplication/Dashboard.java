package com.bautista.myapplication;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.Locale;

public class Dashboard extends AppCompatActivity {

    private int waterCount = 0;
    private static final int MAX_WATER = 8;
    private TextView tvWaterCount, tvBreathingCnt;
    private ImageButton btnAddWater;
    private View[] waterBars;
    private int totalbrth = 0;
    private TextView greetingText, tvDay, tvDateToday;
    private Handler timeHandler = new Handler();
    TextView headerUsername;
    private CardView stressReleaseCard;
    ImageButton btnAddBreathing, btnAddFocus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        timeHandler.post(updateTimeRunnable);
        tvDay = findViewById(R.id.day);
        tvDateToday = findViewById(R.id.datetoday);
        greetingText = findViewById(R.id.greetingText);
        headerUsername = findViewById(R.id.ProfileUsername);
        btnAddBreathing = findViewById(R.id.btnAddBreathing);
        tvWaterCount = findViewById(R.id.tvWaterCount);
        btnAddWater = findViewById(R.id.btnAddWater);
        tvBreathingCnt = findViewById(R.id.tvBreathingCount);
        stressReleaseCard = findViewById(R.id.stressReleaseCard);
        btnAddFocus = findViewById(R.id.btnAddFocus);

        Intent intent = getIntent();
        String loggedInUser = intent.getStringExtra("username");
        headerUsername.setText(loggedInUser);

        TextView user = findViewById(R.id.greetingUn);
        if (loggedInUser != null) {
            user.setText(loggedInUser);

        }

        waterBars = new View[]{
                findViewById(R.id.bar1),
                findViewById(R.id.bar2),
                findViewById(R.id.bar3),
                findViewById(R.id.bar4),
                findViewById(R.id.bar5),
                findViewById(R.id.bar6),
                findViewById(R.id.bar7),
                findViewById(R.id.bar8)
        };

        updateUI();

        btnAddWater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (waterCount < MAX_WATER) {
                    waterCount++;
                    updateUI();
                }
            }
        });

        btnAddBreathing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Dashboard.this, Breathing.class);
                startBreathingLauncher.launch(i);
            }
        });

        stressReleaseCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Dashboard.this,
                        "Opening Stress Lantern Release...",
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Dashboard.this, Lantern.class);
                startActivity(intent);
            }
        });

        btnAddFocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, FocusTimer.class);
                startActivity(intent);
            }
        });
    }

    private void updateUI() {
        tvWaterCount.setText(waterCount + " glasses today");

        for (int i = 0; i < MAX_WATER; i++) {
            if (i < waterCount) {
                waterBars[i].setBackgroundResource(R.drawable.water_bar_active);
            } else {
                waterBars[i].setBackgroundResource(R.drawable.water_bar_inactive);
            }
        }
    }

    private final ActivityResultLauncher<Intent> startBreathingLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    int sessionCycles = result.getData().getIntExtra("total_cycles", 0);

                    totalbrth += sessionCycles;

                    tvBreathingCnt.setText(totalbrth + " cycles");
                }
            }
    );

    private final Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            Calendar calendar = Calendar.getInstance();

            SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());

            tvDay.setText(dayFormat.format(calendar.getTime()));
            tvDateToday.setText(dateFormat.format(calendar.getTime()));

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            String greeting;

            if (hour >= 0 && hour < 12) {
                greeting = "GOOD MORNING, ";
            } else if (hour >= 12 && hour < 18) {
                greeting = "GOOD AFTERNOON, ";
            } else {
                greeting = "GOOD EVENING, ";
            }

            greetingText.setText(greeting);

            timeHandler.postDelayed(this, 1000);
        }
    };
}






