package com.bautista.myapplication;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bautista.myapplication.database.BabyCalmaRepository;
import com.bautista.myapplication.database.DailyResetManager;

import java.util.Locale;
import java.util.Random;

public class FocusTimer extends AppCompatActivity {

    private static final String TAG = "FocusTimerError";
    private TextView tvTimer, tvStatus;
    private Button btn15, btn30, btn45, btn60, btnSetCustom, btnReset;
    private ImageButton btnBack;
    private LinearLayout rootLayout, controlsLayout, headerLayout;
    private ProgressBar timerProgressBar;
    private CountDownTimer focusTimer;
    private MediaPlayer mediaPlayer;
    private long timeRemaining = 1800000;
    private long totalDuration = 1800000;
    private boolean isTimerRunning = false;
    private boolean isBreakMode = false;

    private final int COLOR_WORK_START = Color.parseColor("#E6E6FA");
    private final int COLOR_WORK_END = Color.parseColor("#9370DB");
    private final int COLOR_BREAK = Color.parseColor("#E8F5E9");

    private final String[] motivations = {
            "Great job! You stayed focused.",
            "Session complete! You're making progress.",
            "Well done! You crushed that session.",
            "Awesome work! Time for a well-deserved rest.",
            "Mission accomplished! Your focus is inspiring."
    };

    private BabyCalmaRepository repository;
    private String currentDate;
    private String loggedInUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_focus_timer);

            // Initialize repository
            repository = new BabyCalmaRepository(this);
            currentDate = DailyResetManager.getCurrentDate();

            // Get username from intent
            loggedInUsername = getIntent().getStringExtra("username");
            if (loggedInUsername == null || loggedInUsername.isEmpty()) {
                Toast.makeText(this, "Error: No user logged in", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            rootLayout = findViewById(R.id.mainLayout);
            controlsLayout = findViewById(R.id.controlsLayout);
            headerLayout = findViewById(R.id.headerLayout);
            timerProgressBar = findViewById(R.id.timerProgressBar);
            tvTimer = findViewById(R.id.tvTimer);
            tvStatus = findViewById(R.id.tvStatus);
            btnBack = findViewById(R.id.btnBack);

            btn15 = findViewById(R.id.btn15);
            btn30 = findViewById(R.id.btn25);
            btn45 = findViewById(R.id.btn45);
            btn60 = findViewById(R.id.btn60);
            btnSetCustom = findViewById(R.id.btnSetCustom);
            btnReset = findViewById(R.id.btnReset);

            setupPresetButtons();

            if (btnBack != null) btnBack.setOnClickListener(v -> finish());
            if (btnSetCustom != null) btnSetCustom.setOnClickListener(v -> showCustomTimerDialog());
            if (btnReset != null) btnReset.setOnClickListener(v -> resetTimer());

            if (tvTimer != null) {
                tvTimer.setOnClickListener(v -> {
                    if (isTimerRunning) pauseTimer();
                    else startTheTimer();
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing activity: " + e.getMessage());
            Toast.makeText(this, "Error loading screen", Toast.LENGTH_SHORT).show();
        }
    }

    private void manageMusic(boolean shouldPlay) {
        try {
            if (shouldPlay) {
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(this, R.raw.music2);
                    if (mediaPlayer != null) {
                        mediaPlayer.setLooping(true);
                        mediaPlayer.setVolume(0.6f, 0.6f);
                    }
                }
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
            } else {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Music player error: " + e.getMessage());
        }
    }

    private void resetTimer() {
        try {
            if (focusTimer != null) focusTimer.cancel();
            manageMusic(false);

            isTimerRunning = false;
            isBreakMode = false;
            timeRemaining = totalDuration;
            updateTimerUI();

            if (timerProgressBar != null) timerProgressBar.setProgress(1000);
            if (rootLayout != null) rootLayout.setBackgroundColor(COLOR_WORK_START);
            if (headerLayout != null) headerLayout.setVisibility(View.VISIBLE);
            if (controlsLayout != null) controlsLayout.setVisibility(View.VISIBLE);
            if (tvStatus != null) tvStatus.setText("Click here to start");

            resetButtonHighlights();
        } catch (Exception e) {
            Log.e(TAG, "Error resetting timer: " + e.getMessage());
        }
    }

    void startTheTimer() {
        try {
            if (focusTimer != null) focusTimer.cancel();
            isTimerRunning = true;

            if (!isBreakMode) manageMusic(true);

            if (headerLayout != null) headerLayout.setVisibility(View.GONE);
            if (controlsLayout != null) controlsLayout.setVisibility(View.GONE);
            if (tvStatus != null) tvStatus.setText(isBreakMode ? "Resting..." : "Focusing...");

            focusTimer = new CountDownTimer(timeRemaining, 10) {
                @Override
                public void onTick(long millisUntilFinished) {
                    try {
                        timeRemaining = millisUntilFinished;
                        if (millisUntilFinished % 1000 < 20) updateTimerUI();

                        if (timerProgressBar != null && totalDuration > 0) {
                            int progress = (int) ((millisUntilFinished * 1000) / totalDuration);
                            timerProgressBar.setProgress(progress);
                        }

                        float fraction = 1f - ((float) timeRemaining / totalDuration);
                        if (!isBreakMode) {
                            int color = (int) new ArgbEvaluator().evaluate(fraction, COLOR_WORK_START, COLOR_WORK_END);
                            if (rootLayout != null) rootLayout.setBackgroundColor(color);
                        } else {
                            if (rootLayout != null) rootLayout.setBackgroundColor(COLOR_BREAK);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error during timer tick: " + e.getMessage());
                    }
                }

                @Override
                public void onFinish() {
                    try {
                        isTimerRunning = false;
                        manageMusic(false);

                        if (!isBreakMode) {
                            // Save completed focus session to database
                            int durationMinutes = (int) (totalDuration / 60000);
                            saveFocusSession(durationMinutes, true);

                            showRandomMotivationalMessage();
                            showBreakOptionsDialog();
                        } else {
                            resetTimer();
                            Toast.makeText(FocusTimer.this, "Break Over! Ready for more?", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error finishing timer: " + e.getMessage());
                    }
                }
            }.start();
        } catch (Exception e) {
            Log.e(TAG, "Error starting timer: " + e.getMessage());
        }
    }

    private void showRandomMotivationalMessage() {
        try {
            if (motivations.length > 0) {
                String msg = motivations[new Random().nextInt(motivations.length)];
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing message: " + e.getMessage());
        }
    }

    private void showBreakOptionsDialog() {
        try {
            String[] options = {"5 Minutes", "10 Minutes", "15 Minutes", "No Break (Reset)"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Focus Ended! Choose Break Duration:");
            builder.setCancelable(false);
            builder.setItems(options, (dialog, which) -> {
                try {
                    if (which == 3) {
                        resetTimer();
                    } else {
                        isBreakMode = true;
                        int breakMins = (which + 1) * 5;
                        totalDuration = (long) breakMins * 60 * 1000;
                        timeRemaining = totalDuration;
                        startTheTimer();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in dialog selection: " + e.getMessage());
                }
            });
            builder.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing break dialog: " + e.getMessage());
            resetTimer();
        }
    }

    private void pauseTimer() {
        try {
            if (focusTimer != null) focusTimer.cancel();
            isTimerRunning = false;
            manageMusic(false);
            if (headerLayout != null) headerLayout.setVisibility(View.VISIBLE);
            if (controlsLayout != null) controlsLayout.setVisibility(View.VISIBLE);
            if (tvStatus != null) tvStatus.setText("Paused");
        } catch (Exception e) {
            Log.e(TAG, "Error pausing timer: " + e.getMessage());
        }
    }

    private void updateTimerUI() {
        try {
            if (tvTimer != null) {
                int minutes = (int) (timeRemaining / 1000) / 60;
                int seconds = (int) (timeRemaining / 1000) % 60;
                tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating UI: " + e.getMessage());
        }
    }

    private void setupPresetButtons() {
        try {
            Button[] presetButtons = {btn15, btn30, btn45, btn60};
            int[] minutes = {15, 30, 45, 60};

            for (int i = 0; i < presetButtons.length; i++) {
                final int mins = minutes[i];
                final Button currentBtn = presetButtons[i];

                if (currentBtn != null) {
                    currentBtn.setOnClickListener(v -> {
                        try {
                            if (!isTimerRunning) {
                                isBreakMode = false;
                                totalDuration = (long) mins * 60 * 1000;
                                timeRemaining = totalDuration;
                                updateTimerUI();
                                if (timerProgressBar != null) timerProgressBar.setProgress(1000);
                                resetButtonHighlights();
                                if (currentBtn.getBackground() != null) currentBtn.getBackground().setAlpha(80);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error on preset button click: " + e.getMessage());
                        }
                    });
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up buttons: " + e.getMessage());
        }
    }

    private void resetButtonHighlights() {
        try {
            Button[] allButtons = {btn15, btn30, btn45, btn60, btnSetCustom};
            for (Button b : allButtons) {
                if (b != null && b.getBackground() != null) b.getBackground().setAlpha(255);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error resetting highlights: " + e.getMessage());
        }
    }

    private void showCustomTimerDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Set Custom Minutes");
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);

            builder.setPositiveButton("Set", (dialog, which) -> {
                try {
                    String mText = input.getText().toString();
                    if (!mText.isEmpty()) {
                        int mins = Integer.parseInt(mText);
                        if (mins > 0) {
                            totalDuration = (long) mins * 60 * 1000;
                            timeRemaining = totalDuration;
                            updateTimerUI();
                            if (timerProgressBar != null) timerProgressBar.setProgress(1000);
                            resetButtonHighlights();
                            if (btnSetCustom != null && btnSetCustom.getBackground() != null) {
                                btnSetCustom.getBackground().setAlpha(80);
                            }
                        } else {
                            Toast.makeText(this, "Please enter a value greater than 0", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (NumberFormatException nfe) {
                    Toast.makeText(this, "Invalid number entered", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(TAG, "Error in custom timer: " + e.getMessage());
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing custom dialog: " + e.getMessage());
        }
    }

    private void saveFocusSession(int durationMinutes, boolean completed) {
        if (repository != null && loggedInUsername != null) {
            repository.saveFocusSession(loggedInUsername, durationMinutes, currentDate, completed,
                    new BabyCalmaRepository.DatabaseCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Focus session saved: " + durationMinutes + " minutes");
                            runOnUiThread(() -> {
                                Toast.makeText(FocusTimer.this,
                                        "✅ " + durationMinutes + " minutes logged!",
                                        Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "Error saving focus session: " + e.getMessage());
                        }
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (focusTimer != null) {
                focusTimer.cancel();
                focusTimer = null;
            }
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy: " + e.getMessage());
        }
    }
}