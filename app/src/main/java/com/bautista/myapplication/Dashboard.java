package com.bautista.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bautista.myapplication.database.BabyCalmaRepository;
import com.bautista.myapplication.database.DailyResetManager;

import java.util.Locale;

public class Dashboard extends AppCompatActivity {

    private int waterCount = 0;
    private static final int MAX_WATER = 8;
    private TextView tvWaterCount, tvBreathingCnt, tvFocusCount;
    private View[] waterBars;
    private int totalbrth = 0;
    private int focusMinutes = 0;
    private int releasedLanterns = 0;

    private TextView greetingText, tvDay, tvDateToday;
    private TextView tvSummaryWater, tvSummaryFocus, tvSummaryBreaths, tvSummaryRelease, tvWellnessScore;
    private TextView releasedCountText, tvFirstLetter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private final Handler timeHandler = new Handler();
    TextView headerUsername;
    Button btnLogout;

    // Database components
    private BabyCalmaRepository repository;
    private DailyResetManager resetManager;
    private String currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize database
        repository = new BabyCalmaRepository(this);
        resetManager = new DailyResetManager(this);
        currentDate = DailyResetManager.getCurrentDate();

        // Bind main UI
        timeHandler.post(updateTimeRunnable);
        tvDay = findViewById(R.id.day);
        tvDateToday = findViewById(R.id.datetoday);
        greetingText = findViewById(R.id.greetingText);
        headerUsername = findViewById(R.id.ProfileUsername);
        ImageButton btnAddBreathing = findViewById(R.id.btnAddBreathing);
        tvWaterCount = findViewById(R.id.tvWaterCount);
        ImageButton btnAddWater = findViewById(R.id.btnAddWater);
        tvBreathingCnt = findViewById(R.id.tvBreathingCount);
        CardView stressReleaseCard = findViewById(R.id.stressReleaseCard);
        ImageButton btnAddFocus = findViewById(R.id.btnAddFocus);
        btnLogout = findViewById(R.id.btnLogout);
        releasedCountText = findViewById(R.id.releasedCountText);
        tvFocusCount = findViewById(R.id.tvFocusCount);
        tvFirstLetter = findViewById(R.id.FirstLetter);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Bind Progress Summary UI
        tvSummaryWater = findViewById(R.id.tvSummaryWater);
        tvSummaryFocus = findViewById(R.id.tvSummaryFocus);
        tvSummaryBreaths = findViewById(R.id.tvSummaryBreaths);
        tvSummaryRelease = findViewById(R.id.tvSummaryRelease);
        tvWellnessScore = findViewById(R.id.tvWellnessScore);

        Intent intent = getIntent();
        String loggedInUser = intent.getStringExtra("username");
        headerUsername.setText(loggedInUser);

        if (loggedInUser != null && !loggedInUser.isEmpty()) {
            tvFirstLetter.setText(String.valueOf(loggedInUser.charAt(0)).toUpperCase());
        }

        TextView user = findViewById(R.id.greetingUn);
        if (loggedInUser != null) {
            user.setText(loggedInUser);
        }

        // Greeting Logic
        TextView welcomeGreeting = findViewById(R.id.greeting);
        SharedPreferences prefs = getSharedPreferences("CalmaUser", MODE_PRIVATE);
        boolean isNewUser = intent.getBooleanExtra("isNewUser", prefs.getBoolean("isNewUser", false));

        if (isNewUser) {
            welcomeGreeting.setText(R.string.welcome);
            prefs.edit().putBoolean("isNewUser", false).apply();
        } else {
            welcomeGreeting.setText(R.string.welcome_back);
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

        // Swipe to Refresh logic
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Re-check for daily reset and reload everything
            checkDailyResetAndLoadData();
        });

        checkDailyResetAndLoadData();

        btnLogout.setOnClickListener(v -> {
            Intent logoutIntent = new Intent(Dashboard.this, MainActivity.class);
            logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(logoutIntent);
            finish();
        });

        btnAddWater.setOnClickListener(v -> {
            if (waterCount < MAX_WATER) {
                waterCount++;
                updateWaterUI();
                repository.saveWaterIntake(waterCount, currentDate, null);
            }
        });

        btnAddBreathing.setOnClickListener(v -> {
            Intent i = new Intent(Dashboard.this, Breathing.class);
            startBreathingLauncher.launch(i);
        });

        stressReleaseCard.setOnClickListener(v -> {
            Intent i = new Intent(Dashboard.this, Lantern.class);
            startActivity(i);
        });

        btnAddFocus.setOnClickListener(v -> {
            Intent i = new Intent(Dashboard.this, FocusTimer.class);
            startActivity(i);
        });
    }

    private void updateWaterUI() {
        tvWaterCount.setText(getString(R.string.glasses_today, waterCount));
        tvSummaryWater.setText(getString(R.string.water_summary, waterCount, MAX_WATER));

        for (int i = 0; i < MAX_WATER; i++) {
            if (i < waterCount) {
                waterBars[i].setBackgroundResource(R.drawable.water_bar_active);
            } else {
                waterBars[i].setBackgroundResource(R.drawable.water_bar_inactive);
            }
        }
        calculateWellnessScore();
    }

    private void calculateWellnessScore() {
        float waterScore = Math.min(waterCount / (float) MAX_WATER, 1.0f) * 25;
        float focusScore = Math.min(focusMinutes / 30.0f, 1.0f) * 25; // Goal 30m
        float breathScore = Math.min(totalbrth / 10.0f, 1.0f) * 25; // Goal 10 cycles
        float lanternScore = Math.min(releasedLanterns, 1.0f) * 25; // Goal 1 release

        int totalScore = Math.round(waterScore + focusScore + breathScore + lanternScore);

        tvWellnessScore.setText(getString(R.string.percent_format, totalScore));
    }

    private final ActivityResultLauncher<Intent> startBreathingLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    int sessionCycles = result.getData().getIntExtra("total_cycles", 0);
                    int durationSeconds = result.getData().getIntExtra("duration_seconds", 0);
                    totalbrth += sessionCycles;
                    tvBreathingCnt.setText(getString(R.string.cycles_format, totalbrth));
                    tvSummaryBreaths.setText(String.valueOf(totalbrth));
                    repository.saveBreathingSession(sessionCycles, durationSeconds, currentDate, null);
                    calculateWellnessScore();
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
            String greeting = (hour < 12) ? "GOOD MORNING, " : (hour < 18) ? "GOOD AFTERNOON, " : "GOOD EVENING, ";
            greetingText.setText(greeting);
            timeHandler.postDelayed(this, 60000); // Update every minute
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        loadTodayData();
    }

    private void checkDailyResetAndLoadData() {
        resetManager.checkAndResetIfNeeded(wasReset -> {
            if (wasReset) {
                runOnUiThread(() -> Toast.makeText(Dashboard.this, "Welcome to a new day! 🌅", Toast.LENGTH_LONG).show());
            }
            loadTodayData();

            // Stop the refreshing animation
            runOnUiThread(() -> swipeRefreshLayout.setRefreshing(false));
        });
    }

    private void loadTodayData() {
        // Update current date in case it changed
        currentDate = DailyResetManager.getCurrentDate();

        // Load water
        repository.getWaterIntakeForToday(currentDate, new BabyCalmaRepository.DataCallback<Integer>() {
            @Override
            public void onDataLoaded(Integer data) {
                waterCount = (data != null) ? data : 0;
                runOnUiThread(Dashboard.this::updateWaterUI);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });

        // Load breathing
        repository.getTotalBreathingCyclesForToday(currentDate, new BabyCalmaRepository.DataCallback<Integer>() {
            @Override
            public void onDataLoaded(Integer data) {
                totalbrth = (data != null) ? data : 0;
                runOnUiThread(() -> {
                    tvBreathingCnt.setText(getString(R.string.cycles_format, totalbrth));
                    tvSummaryBreaths.setText(String.valueOf(totalbrth));
                    calculateWellnessScore();
                });
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });

        // Load focus
        repository.getTotalFocusMinutesForToday(currentDate, new BabyCalmaRepository.DataCallback<Integer>() {
            @Override
            public void onDataLoaded(Integer data) {
                focusMinutes = (data != null) ? data : 0;
                runOnUiThread(() -> {
                    tvFocusCount.setText(getString(R.string.minutes_today, focusMinutes));
                    tvSummaryFocus.setText(getString(R.string.minutes_format, focusMinutes));
                    calculateWellnessScore();
                });
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });

        // Load lantern stats
        repository.getLanternStats(currentDate, new BabyCalmaRepository.DataCallback<BabyCalmaRepository.LanternStats>() {
            @Override
            public void onDataLoaded(BabyCalmaRepository.LanternStats data) {
                releasedLanterns = (data != null) ? data.released : 0;
                runOnUiThread(() -> {
                    releasedCountText.setText(getString(R.string.released_format, releasedLanterns));
                    tvSummaryRelease.setText(String.valueOf(releasedLanterns));
                    calculateWellnessScore();
                });
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }
}
