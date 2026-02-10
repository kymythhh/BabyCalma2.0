package com.bautista.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Lantern extends AppCompatActivity {

    private EditText stressorInput;
    private Button addButton;
    private Button releaseAllButton;
    private ImageButton backButton;
    private FrameLayout lanternContainer;
    private TextView totalCountText;
    private TextView activeCountText;
    private TextView releasedCountText;
    private View emptyStateView;
    private Vibrator vibrator;

    private List<View> lanterns = new ArrayList<>();
    private int totalCreated = 0;
    private int totalReleased = 0;
    private static final int MAX_LANTERNS = 15;
    private static final int LANTERN_SIZE = 140; // dp - fixed size for all lanterns
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lantern);

        initializeViews();
        setupListeners();
        updateStats();
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
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This will close the activity and return to the previous screen (dashboard)
                finish();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLantern();
            }
        });

        releaseAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseAllLanterns();
            }
        });

        stressorInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    addLantern();
                    return true;
                }
                return false;
            }
        });
    }

    private void addLantern() {
        String text = stressorInput.getText().toString().trim();

        if (text.isEmpty()) {
            Toast.makeText(this, "Please enter something that stresses you", Toast.LENGTH_SHORT).show();
            return;
        }

        if (lanterns.size() >= MAX_LANTERNS) {
            Toast.makeText(this, "Maximum " + MAX_LANTERNS + " lanterns reached. Release some first!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Inflate lantern item
        LayoutInflater inflater = LayoutInflater.from(this);
        final View lanternView = inflater.inflate(R.layout.item_lantern, lanternContainer, false);

        TextView lanternText = lanternView.findViewById(R.id.lanternText);
        String truncatedText = text.length() > 30 ? text.substring(0, 30) + "..." : text;
        lanternText.setText(truncatedText);

        // Get random position
        int[] position = getRandomPosition();

        // Set layout params with random position
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = position[0];
        params.topMargin = position[1];
        lanternView.setLayoutParams(params);

        // Add click listener to release
        lanternView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseLantern(lanternView);
            }
        });

        // Add to container
        lanternContainer.addView(lanternView);
        lanterns.add(lanternView);
        totalCreated++;

        // Clear input
        stressorInput.setText("");

        // Vibrate
        vibrateShort();

        // Animate entrance
        lanternView.setAlpha(0f);
        lanternView.setScaleX(0.5f);
        lanternView.setScaleY(0.5f);
        lanternView.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        startFloatingAnimation(lanternView);
                    }
                })
                .start();

        updateStats();
    }

    private int[] getRandomPosition() {
        // Get container dimensions
        int containerWidth = lanternContainer.getWidth();
        int containerHeight = lanternContainer.getHeight();

        // If dimensions not available yet, use defaults
        if (containerWidth == 0) containerWidth = 800;
        if (containerHeight == 0) containerHeight = 1200;

        // Calculate safe bounds with extra padding to prevent cropping
        // We need padding for: lantern size + floating animation range
        int edgePadding = 100; // Extra padding to account for floating animations (40-80dp movement)
        int minX = edgePadding;
        int minY = edgePadding;
        int maxX = containerWidth - LANTERN_SIZE - edgePadding;
        int maxY = containerHeight - LANTERN_SIZE - edgePadding;

        // Make sure we have valid bounds
        if (maxX <= minX) maxX = minX + 50;
        if (maxY <= minY) maxY = minY + 50;

        // Try to find a non-overlapping position
        int maxAttempts = 50;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // Generate random position within safe bounds
            int x = minX + random.nextInt(Math.max(1, maxX - minX));
            int y = minY + random.nextInt(Math.max(1, maxY - minY));

            // Check if this position overlaps with existing lanterns
            boolean overlaps = false;
            for (View existingLantern : lanterns) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) existingLantern.getLayoutParams();
                int existingX = params.leftMargin;
                int existingY = params.topMargin;

                // Calculate distance between centers
                int centerX1 = x + LANTERN_SIZE / 2;
                int centerY1 = y + LANTERN_SIZE / 2;
                int centerX2 = existingX + LANTERN_SIZE / 2;
                int centerY2 = existingY + LANTERN_SIZE / 2;

                double distance = Math.sqrt(Math.pow(centerX2 - centerX1, 2) + Math.pow(centerY2 - centerY1, 2));

                // Minimum distance should be at least the lantern size to prevent overlap
                // Adding 40dp buffer for the floating animation
                if (distance < LANTERN_SIZE + 40) {
                    overlaps = true;
                    break;
                }
            }

            if (!overlaps) {
                return new int[]{x, y};
            }
        }

        // If we couldn't find a non-overlapping position after max attempts,
        // return a position within safe bounds anyway
        int x = minX + random.nextInt(Math.max(1, maxX - minX));
        int y = minY + random.nextInt(Math.max(1, maxY - minY));
        return new int[]{x, y};
    }

    private void startFloatingAnimation(final View lanternView) {
        // Reduced floating parameters to keep lanterns fully visible
        float horizontalRange = 30 + random.nextInt(20); // 30-50dp horizontal movement
        float verticalRange = 25 + random.nextInt(20);   // 25-45dp vertical movement
        long durationX = 3000 + random.nextInt(2000);    // 3-5 seconds for horizontal
        long durationY = 2500 + random.nextInt(2000);    // 2.5-4.5 seconds for vertical

        // Random starting direction
        float startX = random.nextBoolean() ? -horizontalRange : horizontalRange;
        float startY = random.nextBoolean() ? -verticalRange : verticalRange;

        // Horizontal floating animation
        final ValueAnimator floatX = ValueAnimator.ofFloat(startX, -startX);
        floatX.setDuration(durationX);
        floatX.setRepeatMode(ValueAnimator.REVERSE);
        floatX.setRepeatCount(ValueAnimator.INFINITE);
        floatX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (lanternView.getTag() != null && lanternView.getTag().equals("releasing")) {
                    animation.cancel();
                    return;
                }
                lanternView.setTranslationX((Float) animation.getAnimatedValue());
            }
        });

        // Vertical floating animation
        final ValueAnimator floatY = ValueAnimator.ofFloat(startY, -startY);
        floatY.setDuration(durationY);
        floatY.setRepeatMode(ValueAnimator.REVERSE);
        floatY.setRepeatCount(ValueAnimator.INFINITE);
        floatY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (lanternView.getTag() != null && lanternView.getTag().equals("releasing")) {
                    animation.cancel();
                    return;
                }
                lanternView.setTranslationY((Float) animation.getAnimatedValue());
            }
        });

        // Gentle rotation animation
        final ValueAnimator rotation = ValueAnimator.ofFloat(-5f, 5f);
        rotation.setDuration(4000 + random.nextInt(2000)); // 4-6 seconds
        rotation.setRepeatMode(ValueAnimator.REVERSE);
        rotation.setRepeatCount(ValueAnimator.INFINITE);
        rotation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (lanternView.getTag() != null && lanternView.getTag().equals("releasing")) {
                    animation.cancel();
                    return;
                }
                lanternView.setRotation((Float) animation.getAnimatedValue());
            }
        });

        // Start all animations
        floatX.start();
        floatY.start();
        rotation.start();

        // Store animators in tag for cleanup
        lanternView.setTag(R.id.lanternText, new ValueAnimator[]{floatX, floatY, rotation});
    }

    private void releaseLantern(final View lanternView) {
        if (lanternView.getTag() != null && lanternView.getTag().equals("releasing")) {
            return;
        }
        lanternView.setTag("releasing");

        // Cancel floating animations
        Object animatorTag = lanternView.getTag(R.id.lanternText);
        if (animatorTag instanceof ValueAnimator[]) {
            ValueAnimator[] animators = (ValueAnimator[]) animatorTag;
            for (ValueAnimator animator : animators) {
                if (animator != null) {
                    animator.cancel();
                }
            }
        }

        // Reset translation and rotation before release animation
        lanternView.setTranslationX(0);
        lanternView.setRotation(0);

        // Vibrate
        vibrateMedium();

        // Animate release (move up and fade out)
        ObjectAnimator animator = ObjectAnimator.ofFloat(lanternView, "translationY", lanternView.getTranslationY(), -1000f);
        animator.setDuration(1500);
        animator.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator fadeAnimator = ObjectAnimator.ofFloat(lanternView, "alpha", 1f, 0f);
        fadeAnimator.setDuration(1500);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Remove from list and view
                lanterns.remove(lanternView);
                lanternContainer.removeView(lanternView);
                totalReleased++;
                updateStats();
            }
        });

        animator.start();
        fadeAnimator.start();
    }

    private void releaseAllLanterns() {
        if (lanterns.isEmpty()) {
            Toast.makeText(this, "No lanterns to release", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vibrate
        vibrateMedium();

        int lanternCount = lanterns.size();

        // Create a copy of the list to avoid concurrent modification
        List<View> lanternsToRelease = new ArrayList<>(lanterns);

        // Release each lantern with a slight delay for cascade effect
        for (int i = 0; i < lanternsToRelease.size(); i++) {
            final View lanternView = lanternsToRelease.get(i);
            final int delay = i * 100; // 100ms delay between each

            lanternView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (lanternView.getTag() == null || !lanternView.getTag().equals("releasing")) {
                        releaseLantern(lanternView);
                    }
                }
            }, delay);
        }

        Toast.makeText(this, "✨ Releasing " + lanternCount + " lanterns!", Toast.LENGTH_SHORT).show();
    }

    private void updateStats() {
        totalCountText.setText(String.valueOf(totalCreated));
        activeCountText.setText(String.valueOf(lanterns.size()));
        releasedCountText.setText(String.valueOf(totalReleased));

        // Show/hide empty state
        emptyStateView.setVisibility(lanterns.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void vibrateShort() {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(50);
            }
        }
    }

    private void vibrateMedium() {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 50, 30, 50}, -1));
            } else {
                vibrator.vibrate(100);
            }
        }
    }
}