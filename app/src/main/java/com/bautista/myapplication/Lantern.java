package com.bautista.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bautista.myapplication.database.BabyCalmaRepository;
import com.bautista.myapplication.database.DailyResetManager;
import com.bautista.myapplication.database.entities.LanternEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Lantern extends AppCompatActivity {

    private EditText stressorInput;
    private Button addButton, releaseAllButton;
    private ImageButton backButton;
    private FrameLayout lanternContainer;
    private TextView totalCountText, activeCountText, releasedCountText;
    private View emptyStateView;
    private Vibrator vibrator;

    private List<View> lanterns = new ArrayList<>();
    private static final int MAX_LANTERNS = 15;
    private static final int LANTERN_SIZE = 140;
    private Random random = new Random();

    private BabyCalmaRepository repository;
    private String currentDate;
    private Map<View, Integer> lanternViewToIdMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lantern);

        try {
            repository = new BabyCalmaRepository(this);
            currentDate = DailyResetManager.getCurrentDate();

            initializeViews();
            setupListeners();
            loadStatsFromDatabase();
            loadActiveLanterns();
        } catch (Exception e) {
            Log.e("Lantern", "Init error: " + e.getMessage());
        }
    }

    private void initializeViews() {
        stressorInput = findViewById(R.id.stressorInput);
        addButton = findViewById(R.id.addButton);
        releaseAllButton = findViewById(R.id.releaseAllButton);
        backButton = findViewById(R.id.backButton);
        lanternContainer = findViewById(R.id.lanternContainer);
        totalCountText = findViewById(R.id.totalCount);
        activeCountText = findViewById(R.id.activeCount);
        releasedCountText = findViewById(R.id.releasedCount);
        emptyStateView = findViewById(R.id.emptyState);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    private void setupListeners() {
        if (backButton != null) backButton.setOnClickListener(v -> finish());
        if (addButton != null) addButton.setOnClickListener(v -> addLantern());
        if (releaseAllButton != null) releaseAllButton.setOnClickListener(v -> releaseAllLanterns());
        if (stressorInput != null) {
            stressorInput.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    addLantern();
                    return true;
                }
                return false;
            });
        }
    }

    private void addLantern() {
        try {
            String text = stressorInput.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "Please enter your stressor", Toast.LENGTH_SHORT).show();
                return;
            }
            if (lanterns.size() >= MAX_LANTERNS) {
                Toast.makeText(this, "Release some lanterns first!", Toast.LENGTH_SHORT).show();
                return;
            }

            LayoutInflater inflater = LayoutInflater.from(this);
            final View lanternView = inflater.inflate(R.layout.item_lantern, lanternContainer, false);
            TextView lanternText = lanternView.findViewById(R.id.lanternText);
            if (lanternText != null) lanternText.setText(text.length() > 30 ? text.substring(0, 30) + "..." : text);

            int[] pos = getRandomPosition();
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LANTERN_SIZE, LANTERN_SIZE);
            params.leftMargin = pos[0];
            params.topMargin = pos[1];
            lanternView.setLayoutParams(params);

            lanternView.setOnClickListener(v -> releaseLantern(lanternView));
            if (lanternContainer != null) lanternContainer.addView(lanternView);
            lanterns.add(lanternView);
            stressorInput.setText("");

            repository.saveLantern(text, currentDate, new BabyCalmaRepository.DataCallback<Long>() {
                @Override public void onDataLoaded(Long id) { lanternViewToIdMap.put(lanternView, id.intValue()); }
                @Override public void onError(Exception e) { Log.e("Lantern", "Save error: " + e.getMessage()); }
            });

            vibrateShort();
            lanternView.setAlpha(0f);
            lanternView.animate().alpha(1f).setDuration(300).withEndAction(() -> startFloatingAnimation(lanternView)).start();
            loadStatsFromDatabase();
        } catch (Exception e) { Log.e("Lantern", "Add lantern error: " + e.getMessage()); }
    }

    private int[] getRandomPosition() {
        try {
            int w = lanternContainer != null ? lanternContainer.getWidth() : 800;
            int h = lanternContainer != null ? lanternContainer.getHeight() : 1200;
            if (w <= 0) w = 800;
            if (h <= 0) h = 1200;
            return new int[]{random.nextInt(Math.max(1, w - LANTERN_SIZE)), random.nextInt(Math.max(1, h - LANTERN_SIZE))};
        } catch (Exception e) { return new int[]{100, 100}; }
    }

    private void startFloatingAnimation(final View lanternView) {
        try {
            float range = 30 + random.nextInt(20);
            ValueAnimator anim = ValueAnimator.ofFloat(-range, range);
            anim.setDuration(2500 + random.nextInt(2000));
            anim.setRepeatMode(ValueAnimator.REVERSE);
            anim.setRepeatCount(ValueAnimator.INFINITE);
            anim.addUpdateListener(a -> {
                if (lanternView.getTag() == null || !lanternView.getTag().equals("releasing")) {
                    lanternView.setTranslationY((Float) a.getAnimatedValue());
                }
            });
            anim.start();
        } catch (Exception e) { Log.e("Lantern", "Anim error: " + e.getMessage()); }
    }

    private void releaseLantern(final View lanternView) {
        try {
            if (lanternView.getTag() != null && lanternView.getTag().equals("releasing")) return;
            lanternView.setTag("releasing");

            Integer id = lanternViewToIdMap.get(lanternView);
            if (id != null) {
                repository.releaseLantern(id, new BabyCalmaRepository.DatabaseCallback() {
                    @Override public void onSuccess() { runOnUiThread(() -> loadStatsFromDatabase()); }
                    @Override public void onError(Exception e) { Log.e("Lantern", "Release DB error: " + e.getMessage()); }
                });
            }

            vibrateMedium();
            lanternView.animate().translationY(-1000f).alpha(0f).setDuration(1500).setInterpolator(new AccelerateInterpolator()).withEndAction(() -> {
                lanterns.remove(lanternView);
                if (lanternContainer != null) lanternContainer.removeView(lanternView);
                if (emptyStateView != null) emptyStateView.setVisibility(lanterns.isEmpty() ? View.VISIBLE : View.GONE);
            }).start();
        } catch (Exception e) { Log.e("Lantern", "Release error: " + e.getMessage()); }
    }

    private void releaseAllLanterns() {
        try {
            if (lanterns.isEmpty()) return;
            List<View> copy = new ArrayList<>(lanterns);
            for (int i = 0; i < copy.size(); i++) {
                final View v = copy.get(i);
                v.postDelayed(() -> releaseLantern(v), i * 100);
            }
        } catch (Exception e) { Log.e("Lantern", "Release all error: " + e.getMessage()); }
    }

    private void loadStatsFromDatabase() {
        try {
            repository.getLanternStats(currentDate, new BabyCalmaRepository.DataCallback<BabyCalmaRepository.LanternStats>() {
                @Override
                public void onDataLoaded(BabyCalmaRepository.LanternStats stats) {
                    runOnUiThread(() -> {
                        if (totalCountText != null) totalCountText.setText(String.valueOf(stats.total));
                        if (activeCountText != null) activeCountText.setText(String.valueOf(stats.active));
                        if (releasedCountText != null) releasedCountText.setText(String.valueOf(stats.released));
                        if (emptyStateView != null) emptyStateView.setVisibility(lanterns.isEmpty() ? View.VISIBLE : View.GONE);
                    });
                }
                @Override public void onError(Exception e) { Log.e("Lantern", "Stats error: " + e.getMessage()); }
            });
        } catch (Exception e) { Log.e("Lantern", "Load stats error: " + e.getMessage()); }
    }

    private void loadActiveLanterns() {
        try {
            repository.getActiveLanterns(currentDate, new BabyCalmaRepository.DataCallback<List<LanternEntity>>() {
                @Override
                public void onDataLoaded(List<LanternEntity> list) {
                    runOnUiThread(() -> {
                        lanterns.clear();
                        if (lanternContainer != null) lanternContainer.removeAllViews();
                        if (list != null) { for (LanternEntity e : list) recreateLanternView(e); }
                    });
                }
                @Override public void onError(Exception e) { Log.e("Lantern", "Load active error: " + e.getMessage()); }
            });
        } catch (Exception e) { Log.e("Lantern", "Load lanterns error: " + e.getMessage()); }
    }

    private void recreateLanternView(LanternEntity entity) {
        try {
            LayoutInflater inflater = LayoutInflater.from(this);
            View v = inflater.inflate(R.layout.item_lantern, lanternContainer, false);
            TextView t = v.findViewById(R.id.lanternText);
            if (t != null) t.setText(entity.stressorText);
            int[] pos = getRandomPosition();
            FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(LANTERN_SIZE, LANTERN_SIZE);
            p.leftMargin = pos[0]; p.topMargin = pos[1];
            v.setLayoutParams(p);
            v.setOnClickListener(view -> releaseLantern(v));
            if (lanternContainer != null) lanternContainer.addView(v);
            lanterns.add(v);
            lanternViewToIdMap.put(v, entity.id);
            startFloatingAnimation(v);
        } catch (Exception e) { Log.e("Lantern", "Recreate error: " + e.getMessage()); }
    }

    private void vibrateShort() { if (vibrator != null) vibrator.vibrate(50); }
    private void vibrateMedium() { if (vibrator != null) vibrator.vibrate(100); }
}