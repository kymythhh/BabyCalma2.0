package com.bautista.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
    private TextView releasedCountText, tvFirstLetter, tvDailyAffirmation;
    private SwipeRefreshLayout swipeRefreshLayout;

    private final Handler timeHandler = new Handler();
    TextView headerUsername;
    Button btnLogout;

    private BabyCalmaRepository repository;
    private DailyResetManager resetManager;
    private String currentDate;
    private String loggedInUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        try {
            repository = new BabyCalmaRepository(this);
            resetManager = new DailyResetManager(this);
            currentDate = DailyResetManager.getCurrentDate();

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

            tvSummaryWater = findViewById(R.id.tvSummaryWater);
            tvSummaryFocus = findViewById(R.id.tvSummaryFocus);
            tvSummaryBreaths = findViewById(R.id.tvSummaryBreaths);
            tvSummaryRelease = findViewById(R.id.tvSummaryRelease);
            tvWellnessScore = findViewById(R.id.tvWellnessScore);
            tvDailyAffirmation = findViewById(R.id.tvDailyAffirmation);

            Intent intent = getIntent();
            loggedInUsername = intent.getStringExtra("username");
            if (headerUsername != null) headerUsername.setText(loggedInUsername);

            if (loggedInUsername != null && !loggedInUsername.isEmpty() && tvFirstLetter != null) {
                tvFirstLetter.setText(String.valueOf(loggedInUsername.charAt(0)).toUpperCase());
            }

            TextView user = findViewById(R.id.greetingUn);
            if (loggedInUsername != null && user != null) {
                user.setText(loggedInUsername);
            }

            SharedPreferences prefs = getSharedPreferences("CalmaUser", MODE_PRIVATE);
            boolean isNewUser = intent.getBooleanExtra("isNewUser", prefs.getBoolean("isNewUser", false));
            TextView welcomeGreeting = findViewById(R.id.greeting);
            if (welcomeGreeting != null) {
                if (isNewUser) {
                    welcomeGreeting.setText(R.string.welcome);
                    prefs.edit().putBoolean("isNewUser", false).apply();
                } else {
                    welcomeGreeting.setText(R.string.welcome_back);
                }
            }

            waterBars = new View[]{
                    findViewById(R.id.bar1), findViewById(R.id.bar2), findViewById(R.id.bar3),
                    findViewById(R.id.bar4), findViewById(R.id.bar5), findViewById(R.id.bar6),
                    findViewById(R.id.bar7), findViewById(R.id.bar8)
            };

            swipeRefreshLayout.setOnRefreshListener(this::checkDailyResetAndLoadData);
            checkDailyResetAndLoadData();

            // Initialize affirmations in database if not already done
            // Force new affirmation on login
            repository.initializeAffirmations(new BabyCalmaRepository.DatabaseCallback() {
                @Override
                public void onSuccess() {
                    loadDailyAffirmation(true); // Force new on login
                }
                @Override
                public void onError(Exception e) {
                    Log.e("Dashboard", "Error initializing affirmations: " + e.getMessage());
                }
            });

            btnLogout.setOnClickListener(v -> {
                // Clear affirmation cache on logout to get new affirmation on next login
                repository.clearAffirmationCache();

                Intent logoutIntent = new Intent(Dashboard.this, MainActivity.class);
                logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(logoutIntent);
                finish();
            });

            btnAddWater.setOnClickListener(v -> {
                if (waterCount < MAX_WATER) {
                    waterCount++;
                    updateWaterUI();
                    repository.saveWaterIntake(loggedInUsername, waterCount, currentDate, null);
                }
            });

            btnAddBreathing.setOnClickListener(v -> {
                Intent i = new Intent(Dashboard.this, Breathing.class);
                startBreathingLauncher.launch(i);
            });

            stressReleaseCard.setOnClickListener(v -> {
                Intent lanternIntent = new Intent(Dashboard.this, Lantern.class);
                lanternIntent.putExtra("username", loggedInUsername);
                startActivity(lanternIntent);
            });
            btnAddFocus.setOnClickListener(v -> {
                Intent focusIntent = new Intent(Dashboard.this, FocusTimer.class);
                focusIntent.putExtra("username", loggedInUsername);
                startActivity(focusIntent);
            });

        } catch (Exception e) {
            Log.e("Dashboard", "Error in onCreate: " + e.getMessage());
        }
    }

    private void updateWaterUI() {
        try {
            if (tvWaterCount != null) tvWaterCount.setText(getString(R.string.glasses_today, waterCount));
            if (tvSummaryWater != null) tvSummaryWater.setText(getString(R.string.water_summary, waterCount, MAX_WATER));

            for (int i = 0; i < MAX_WATER; i++) {
                if (waterBars[i] != null) {
                    if (i < waterCount) waterBars[i].setBackgroundResource(R.drawable.water_bar_active);
                    else waterBars[i].setBackgroundResource(R.drawable.water_bar_inactive);
                }
            }
            calculateWellnessScore();
        } catch (Exception e) {
            Log.e("Dashboard", "Error updating water UI: " + e.getMessage());
        }
    }

    private void calculateWellnessScore() {
        try {
            float waterScore = Math.min(waterCount / (float) MAX_WATER, 1.0f) * 25;
            float focusScore = Math.min(focusMinutes / 30.0f, 1.0f) * 25;
            float breathScore = Math.min(totalbrth / 10.0f, 1.0f) * 25;
            float lanternScore = Math.min(releasedLanterns, 1.0f) * 25;

            int totalScore = Math.round(waterScore + focusScore + breathScore + lanternScore);
            if (tvWellnessScore != null) tvWellnessScore.setText(getString(R.string.percent_format, totalScore));
        } catch (Exception e) {
            Log.e("Dashboard", "Score calculation error: " + e.getMessage());
        }
    }

    private final ActivityResultLauncher<Intent> startBreathingLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                try {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        int sessionCycles = result.getData().getIntExtra("total_cycles", 0);
                        int durationSeconds = result.getData().getIntExtra("duration_seconds", 0);
                        totalbrth += sessionCycles;
                        if (tvBreathingCnt != null) tvBreathingCnt.setText(getString(R.string.cycles_format, totalbrth));
                        if (tvSummaryBreaths != null) tvSummaryBreaths.setText(String.valueOf(totalbrth));
                        repository.saveBreathingSession(loggedInUsername, sessionCycles, durationSeconds, currentDate, null);
                        calculateWellnessScore();
                    }
                } catch (Exception e) {
                    Log.e("Dashboard", "Breathing result error: " + e.getMessage());
                }
            }
    );

    private final Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
                if (tvDay != null) tvDay.setText(dayFormat.format(calendar.getTime()));
                if (tvDateToday != null) tvDateToday.setText(dateFormat.format(calendar.getTime()));

                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                String greeting = (hour < 12) ? "GOOD MORNING, " : (hour < 18) ? "GOOD AFTERNOON, " : "GOOD EVENING, ";
                if (greetingText != null) greetingText.setText(greeting);
                timeHandler.postDelayed(this, 60000);
            } catch (Exception e) {
                Log.e("Dashboard", "Time update error: " + e.getMessage());
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        loadTodayData();
        // Don't force refresh on resume to maintain same affirmation during day
        loadDailyAffirmation(false);
    }

    private void checkDailyResetAndLoadData() {
        try {
            resetManager.checkAndResetIfNeeded(loggedInUsername, wasReset -> {
                if (wasReset) {
                    runOnUiThread(() -> Toast.makeText(Dashboard.this, "Welcome to a new day! 🌅", Toast.LENGTH_LONG).show());
                }
                loadTodayData();
                // Force new affirmation on daily reset
                loadDailyAffirmation(wasReset);
                runOnUiThread(() -> { if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false); });
            });
        } catch (Exception e) {
            Log.e("Dashboard", "Daily reset check error: " + e.getMessage());
        }
    }

    private void loadTodayData() {
        try {
            currentDate = DailyResetManager.getCurrentDate();
            repository.getWaterIntakeForToday(loggedInUsername, currentDate, new BabyCalmaRepository.DataCallback<Integer>() {
                @Override
                public void onDataLoaded(Integer data) {
                    waterCount = (data != null) ? data : 0;
                    runOnUiThread(() -> updateWaterUI());
                }
                @Override public void onError(Exception e) { Log.e("Dashboard", "Load water error: " + e.getMessage()); }
            });

            repository.getTotalBreathingCyclesForToday(loggedInUsername, currentDate, new BabyCalmaRepository.DataCallback<Integer>() {
                @Override
                public void onDataLoaded(Integer data) {
                    totalbrth = (data != null) ? data : 0;
                    runOnUiThread(() -> {
                        if (tvBreathingCnt != null) tvBreathingCnt.setText(getString(R.string.cycles_format, totalbrth));
                        if (tvSummaryBreaths != null) tvSummaryBreaths.setText(String.valueOf(totalbrth));
                        calculateWellnessScore();
                    });
                }
                @Override public void onError(Exception e) { Log.e("Dashboard", "Load breathing error: " + e.getMessage()); }
            });

            repository.getTotalFocusMinutesForToday(loggedInUsername, currentDate, new BabyCalmaRepository.DataCallback<Integer>() {
                @Override
                public void onDataLoaded(Integer data) {
                    focusMinutes = (data != null) ? data : 0;
                    runOnUiThread(() -> {
                        if (tvFocusCount != null) tvFocusCount.setText(getString(R.string.minutes_today, focusMinutes));
                        if (tvSummaryFocus != null) tvSummaryFocus.setText(getString(R.string.minutes_format, focusMinutes));
                        calculateWellnessScore();
                    });
                }
                @Override public void onError(Exception e) { Log.e("Dashboard", "Load focus error: " + e.getMessage()); }
            });

            repository.getLanternStats(loggedInUsername, currentDate, new BabyCalmaRepository.DataCallback<BabyCalmaRepository.LanternStats>() {
                @Override
                public void onDataLoaded(BabyCalmaRepository.LanternStats data) {
                    releasedLanterns = (data != null) ? data.released : 0;
                    runOnUiThread(() -> {
                        if (releasedCountText != null) releasedCountText.setText(getString(R.string.released_format, releasedLanterns));
                        if (tvSummaryRelease != null) tvSummaryRelease.setText(String.valueOf(releasedLanterns));
                        calculateWellnessScore();
                    });
                }
                @Override public void onError(Exception e) { Log.e("Dashboard", "Load lantern error: " + e.getMessage()); }
            });
        } catch (Exception e) {
            Log.e("Dashboard", "Load today data error: " + e.getMessage());
        }
    }

    private void loadDailyAffirmation() {
        loadDailyAffirmation(false);
    }

    /**
     * Load daily affirmation with option to force new random selection
     * @param forceNew If true, always generate a new random affirmation
     */
    private void loadDailyAffirmation(boolean forceNew) {
        try {
            repository.getDailyAffirmation(currentDate, forceNew, new BabyCalmaRepository.DataCallback<String>() {
                @Override
                public void onDataLoaded(String affirmation) {
                    runOnUiThread(() -> {
                        if (tvDailyAffirmation != null) {
                            tvDailyAffirmation.setText(affirmation);
                        }
                    });
                }
                @Override
                public void onError(Exception e) {
                    Log.e("Dashboard", "Load affirmation error: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("Dashboard", "Load affirmation error: " + e.getMessage());
        }
    }
}