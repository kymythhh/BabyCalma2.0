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

import com.bautista.myapplication.database.BabyCalmaRepository;
import com.bautista.myapplication.database.DailyResetManager;

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

    // Database components
    private BabyCalmaRepository repository;
    private DailyResetManager resetManager;
    private String currentDate;

    //wakim
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize database
        repository = new BabyCalmaRepository(this);
        resetManager = new DailyResetManager(this);
        currentDate = DailyResetManager.getCurrentDate();

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

        // Check for daily reset and load data
        checkDailyResetAndLoadData();

        btnAddWater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (waterCount < MAX_WATER) {
                    waterCount++;
                    updateUI();

                    // Save to database
                    repository.saveWaterIntake(waterCount, currentDate,
                            new BabyCalmaRepository.DatabaseCallback() {
                                @Override
                                public void onSuccess() {
                                    // Successfully saved
                                }

                                @Override
                                public void onError(Exception e) {
                                    e.printStackTrace();
                                }
                            });
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
                    int durationSeconds = result.getData().getIntExtra("duration_seconds", 0);

                    totalbrth += sessionCycles;
                    tvBreathingCnt.setText(totalbrth + " cycles");

                    // Save to database
                    repository.saveBreathingSession(sessionCycles, durationSeconds, currentDate,
                            new BabyCalmaRepository.DatabaseCallback() {
                                @Override
                                public void onSuccess() {
                                    // Successfully saved
                                }

                                @Override
                                public void onError(Exception e) {
                                    e.printStackTrace();
                                }
                            });
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

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning from other activities
        loadTodayData();
    }

    /**
     * Check for daily reset and load today's data
     */
    private void checkDailyResetAndLoadData() {
        resetManager.checkAndResetIfNeeded(new DailyResetManager.ResetCallback() {
            @Override
            public void onResetComplete(boolean wasReset) {
                if (wasReset) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Dashboard.this,
                                    "Welcome to a new day! 🌅",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
                loadTodayData();
            }
        });
    }

    /**
     * Load all today's data from database
     */
    private void loadTodayData() {
        // Load water intake
        repository.getWaterIntakeForToday(currentDate,
                new BabyCalmaRepository.DataCallback<Integer>() {
                    @Override
                    public void onDataLoaded(Integer count) {
                        waterCount = (count != null && count > 0) ? count : 0;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateUI();
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });

        // Load breathing cycles
        repository.getTotalBreathingCyclesForToday(currentDate,
                new BabyCalmaRepository.DataCallback<Integer>() {
                    @Override
                    public void onDataLoaded(Integer total) {
                        totalbrth = (total != null && total > 0) ? total : 0;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvBreathingCnt.setText(totalbrth + " cycles");
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });
    }
}
