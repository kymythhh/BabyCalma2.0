package com.bautista.myapplication;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Breathing extends AppCompatActivity {
    private AnimatorSet animatorSet;
    private boolean isRunning = false;
    private int phaseCount = 0, count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breathing);

        final ImageView zoomImageView = findViewById(R.id.bexer);
        final Button startBtn = findViewById(R.id.start);
        final ImageButton backBtn = findViewById(R.id.btnBack);

        animatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.start);
        animatorSet.setTarget(zoomImageView);

        for (Animator anim : animatorSet.getChildAnimations()) {
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (!isRunning) return;

                    switch (phaseCount) {
                        case 0:
                            zoomImageView.setImageResource(R.drawable.inhale);
                            break;
                        case 1:
                            zoomImageView.setImageResource(R.drawable.hold);
                            break;
                        case 2:
                            zoomImageView.setImageResource(R.drawable.exhale);
                            count++;
                            break;
                    }
                    phaseCount = (phaseCount + 1) % 3;
                }
            });
        }

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (isRunning) {
                    animatorSet.start();
                }
            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRunning) {
                    isRunning = true;
                    phaseCount = 0;
                    startBtn.setText("Stop");

                    zoomImageView.setPivotX(zoomImageView.getWidth() / 2f);
                    zoomImageView.setPivotY(zoomImageView.getHeight() / 2f);

                    animatorSet.start();
                } else {
                    isRunning = false;
                    startBtn.setText("Start Breathing");
                    animatorSet.cancel();

                    // Reset the image scale and position when stopped
                    zoomImageView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).start();
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop the animation if it's running before going back
                if (isRunning) {
                    isRunning = false;
                    animatorSet.cancel();
                }

                Intent countBrthe = new Intent();
                countBrthe.putExtra("total_cycles", count);
                setResult(RESULT_OK, countBrthe);

                finish(); // Goes back to the previous activity (Dashboard)
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Handle the system back button press as well
        Intent countBrthe = new Intent();
        countBrthe.putExtra("total_cycles", count);
        setResult(RESULT_OK, countBrthe);
        super.onBackPressed();
    }
}
