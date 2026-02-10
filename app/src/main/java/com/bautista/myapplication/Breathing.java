package com.bautista.myapplication;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Breathing extends AppCompatActivity {
    private AnimatorSet animatorSet;
    private boolean isRunning = false;
    private int phaseCount = 0, count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breathing);

        try {
            final ImageView zoomImageView = findViewById(R.id.bexer);
            final Button startBtn = findViewById(R.id.start);
            final ImageButton backBtn = findViewById(R.id.btnBack);

            animatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.start);
            if (animatorSet != null) {
                animatorSet.setTarget(zoomImageView);

                for (Animator anim : animatorSet.getChildAnimations()) {
                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            if (!isRunning) return;
                            try {
                                switch (phaseCount) {
                                    case 0: zoomImageView.setImageResource(R.drawable.inhale); break;
                                    case 1: zoomImageView.setImageResource(R.drawable.hold); break;
                                    case 2: zoomImageView.setImageResource(R.drawable.exhale); count++; break;
                                }
                                phaseCount = (phaseCount + 1) % 3;
                            } catch (Exception e) { Log.e("Breathing", "Anim frame error: " + e.getMessage()); }
                        }
                    });
                }

                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) { if (isRunning) animatorSet.start(); }
                });
            }

            startBtn.setOnClickListener(v -> {
                try {
                    if (!isRunning) {
                        isRunning = true;
                        phaseCount = 0;
                        startBtn.setText("Stop");
                        zoomImageView.setPivotX(zoomImageView.getWidth() / 2f);
                        zoomImageView.setPivotY(zoomImageView.getHeight() / 2f);
                        if (animatorSet != null) animatorSet.start();
                    } else {
                        isRunning = false;
                        startBtn.setText("Start Breathing");
                        if (animatorSet != null) animatorSet.cancel();
                        zoomImageView.setImageResource(R.drawable.inhale);
                        zoomImageView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).start();
                        phaseCount = 0;
                    }
                } catch (Exception e) { Log.e("Breathing", "Start button error: " + e.getMessage()); }
            });

            backBtn.setOnClickListener(v -> goBackWithData());

        } catch (Exception e) {
            Log.e("Breathing", "Init error: " + e.getMessage());
            Toast.makeText(this, "Animation error", Toast.LENGTH_SHORT).show();
        }
    }

    private void goBackWithData() {
        try {
            if (isRunning && animatorSet != null) animatorSet.cancel();
            Intent countBrthe = new Intent();
            countBrthe.putExtra("total_cycles", count);
            setResult(RESULT_OK, countBrthe);
            finish();
        } catch (Exception e) { Log.e("Breathing", "Back navigation error: " + e.getMessage()); finish(); }
    }

    @Override
    public void onBackPressed() { goBackWithData(); }
}
