package com.bautista.myapplication;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
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
import java.util.Locale;
import java.util.Random;

public class FocusTimer extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_timer);

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

        btnBack.setOnClickListener(v -> finish());
        btnSetCustom.setOnClickListener(v -> showCustomTimerDialog());
        btnReset.setOnClickListener(v -> resetTimer());

        tvTimer.setOnClickListener(v -> {
            if (isTimerRunning) pauseTimer();
            else startTheTimer();
        });
    }

    private void manageMusic(boolean shouldPlay) {
        if (shouldPlay) {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, R.raw.music2);
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(0.6f, 0.6f);
            }
            if (!mediaPlayer.isPlaying()) mediaPlayer.start();
        } else {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
        }
    }

    private void resetTimer() {
        if (focusTimer != null) focusTimer.cancel();
        manageMusic(false);

        isTimerRunning = false;
        isBreakMode = false;
        timeRemaining = totalDuration;
        updateTimerUI();
        timerProgressBar.setProgress(1000);
        rootLayout.setBackgroundColor(COLOR_WORK_START);
        headerLayout.setVisibility(View.VISIBLE);
        controlsLayout.setVisibility(View.VISIBLE);
        tvStatus.setText("Click here to start");
        resetButtonHighlights();
    }

    void startTheTimer() {
        if (focusTimer != null) focusTimer.cancel();
        isTimerRunning = true;

        if (!isBreakMode) manageMusic(true);

        headerLayout.setVisibility(View.GONE);
        controlsLayout.setVisibility(View.GONE);
        tvStatus.setText(isBreakMode ? "Resting..." : "Focusing...");

        focusTimer = new CountDownTimer(timeRemaining, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                if (millisUntilFinished % 1000 < 20) updateTimerUI();

                // Logical fix for counter-clockwise movement
                int progress = (int) ((millisUntilFinished * 1000) / totalDuration);
                timerProgressBar.setProgress(progress);

                float fraction = 1f - ((float) timeRemaining / totalDuration);
                if (!isBreakMode) {
                    int color = (int) new ArgbEvaluator().evaluate(fraction, COLOR_WORK_START, COLOR_WORK_END);
                    rootLayout.setBackgroundColor(color);
                } else {
                    rootLayout.setBackgroundColor(COLOR_BREAK);
                }
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                manageMusic(false);

                if (!isBreakMode) {
                    showRandomMotivationalMessage();
                    showBreakOptionsDialog();
                } else {
                    resetTimer();
                    Toast.makeText(FocusTimer.this, "Break Over! Ready for more?", Toast.LENGTH_SHORT).show();
                }
            }
        }.start();
    }

    private void showRandomMotivationalMessage() {
        String msg = motivations[new Random().nextInt(motivations.length)];
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void showBreakOptionsDialog() {
        String[] options = {"5 Minutes", "10 Minutes", "15 Minutes", "No Break (Reset)"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Focus Ended! Choose Break Duration:");
        builder.setCancelable(false);
        builder.setItems(options, (dialog, which) -> {
            if (which == 3) {
                resetTimer();
            } else {
                isBreakMode = true;
                int breakMins = (which + 1) * 5;
                totalDuration = (long) breakMins * 60 * 1000;
                timeRemaining = totalDuration;
                startTheTimer();
            }
        });
        builder.show();
    }

    private void pauseTimer() {
        if (focusTimer != null) focusTimer.cancel();
        isTimerRunning = false;
        manageMusic(false);
        headerLayout.setVisibility(View.VISIBLE);
        controlsLayout.setVisibility(View.VISIBLE);
        tvStatus.setText("Paused");
    }

    private void updateTimerUI() {
        int minutes = (int) (timeRemaining / 1000) / 60;
        int seconds = (int) (timeRemaining / 1000) % 60;
        tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    private void setupPresetButtons() {
        Button[] presetButtons = {btn15, btn30, btn45, btn60};
        int[] minutes = {15, 30, 45, 60};

        for (int i = 0; i < presetButtons.length; i++) {
            final int mins = minutes[i];
            final Button currentBtn = presetButtons[i];

            currentBtn.setOnClickListener(v -> {
                if (!isTimerRunning) {
                    isBreakMode = false;
                    totalDuration = (long) mins * 60 * 1000;
                    timeRemaining = totalDuration;
                    updateTimerUI();
                    timerProgressBar.setProgress(1000);
                    resetButtonHighlights();
                    currentBtn.getBackground().setAlpha(80);
                }
            });
        }
    }

    private void resetButtonHighlights() {
        Button[] allButtons = {btn15, btn30, btn45, btn60, btnSetCustom};
        for (Button b : allButtons) {
            if (b.getBackground() != null) b.getBackground().setAlpha(255);
        }
    }

    private void showCustomTimerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Custom Minutes");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Set", (dialog, which) -> {
            String mText = input.getText().toString();
            if (!mText.isEmpty()) {
                int mins = Integer.parseInt(mText);
                totalDuration = (long) mins * 60 * 1000;
                timeRemaining = totalDuration;
                updateTimerUI();
                timerProgressBar.setProgress(1000);
                resetButtonHighlights();
                btnSetCustom.getBackground().setAlpha(80);
            }
        });
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
