package com.bautista.myapplication.database;

import android.content.Context;
import android.content.SharedPreferences;

import com.bautista.myapplication.database.dao.BreathingSessionDao;
import com.bautista.myapplication.database.dao.DailyStatsDao;
import com.bautista.myapplication.database.dao.FocusSessionDao;
import com.bautista.myapplication.database.dao.LanternDao;
import com.bautista.myapplication.database.dao.WaterIntakeDao;
import com.bautista.myapplication.database.entities.DailyStatsEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Manages the daily reset functionality for BabyCalma app.
 * Checks if a new day has started and resets daily data accordingly.
 */
public class DailyResetManager {

    private static final String PREFS_NAME = "BabyCalmaPrefs";
    private static final String KEY_LAST_RESET_DATE = "last_reset_date";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private final Context context;
    private final SharedPreferences prefs;
    private final Executor executor;
    private final BabyCalmaDatabase database;

    public DailyResetManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.executor = Executors.newSingleThreadExecutor();
        this.database = BabyCalmaDatabase.getInstance(context);
    }

    /**
     * Check if a new day has started and perform reset if needed.
     * Call this method when the app starts or resumes.
     */
    public void checkAndResetIfNeeded(final String username, final ResetCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                String currentDate = getCurrentDate();
                String lastResetDate = prefs.getString(KEY_LAST_RESET_DATE + "_" + username, "");

                if (!currentDate.equals(lastResetDate)) {
                    // New day detected - perform reset
                    performDailyReset(username, lastResetDate, currentDate);

                    // Update last reset date
                    prefs.edit().putString(KEY_LAST_RESET_DATE + "_" + username, currentDate).apply();

                    if (callback != null) {
                        callback.onResetComplete(true);
                    }
                } else {
                    if (callback != null) {
                        callback.onResetComplete(false);
                    }
                }
            }
        });
    }

    /**
     * Perform the daily reset operations
     */
    private void performDailyReset(String username, String lastDate, String currentDate) {
        try {
            // Save yesterday's stats before resetting
            if (!lastDate.isEmpty()) {
                saveDailyStats(username, lastDate);
            }

            // Reset today's data (delete current day's data)
            resetTodayData(username, currentDate);

            // Clean up old data (keep only last 30 days for history)
            cleanupOldData(username, currentDate);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Save the previous day's statistics
     */
    private void saveDailyStats(String username, String date) {
        DailyStatsDao statsDao = database.dailyStatsDao();
        WaterIntakeDao waterDao = database.waterIntakeDao();
        BreathingSessionDao breathingDao = database.breathingSessionDao();
        FocusSessionDao focusDao = database.focusSessionDao();
        LanternDao lanternDao = database.lanternDao();

        // Get totals for the previous day
        int totalWater = waterDao.getTotalGlassesForDate(username, date);
        int totalBreathingCycles = breathingDao.getTotalCyclesForDate(username, date);
        int totalFocusMinutes = focusDao.getTotalMinutesForDate(username, date);
        int totalLanternsReleased = lanternDao.getReleasedCountForDate(username, date);

        // Calculate wellness score (0-100)
        int wellnessScore = calculateWellnessScore(
                totalWater,
                totalBreathingCycles,
                totalFocusMinutes,
                totalLanternsReleased
        );

        // Check if stats already exist for this date
        DailyStatsEntity existingStats = statsDao.getStatsForDate(username, date);
        if (existingStats == null) {
            // Create new stats entry
            DailyStatsEntity stats = new DailyStatsEntity(
                    username,
                    date,
                    wellnessScore,
                    totalWater,
                    totalBreathingCycles,
                    totalFocusMinutes,
                    totalLanternsReleased,
                    System.currentTimeMillis()
            );
            statsDao.insert(stats);
        }
    }

    /**
     * Reset today's data (for the new day)
     */
    private void resetTodayData(String username, String currentDate) {
        // Delete today's entries to start fresh
        database.waterIntakeDao().deleteByDate(username, currentDate);
        database.breathingSessionDao().deleteByDate(username, currentDate);
        database.focusSessionDao().deleteByDate(username, currentDate);
        database.lanternDao().deleteByDate(username, currentDate);
    }

    /**
     * Clean up old data (keep only last 30 days)
     */
    private void cleanupOldData(String username, String currentDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            Date current = sdf.parse(currentDate);

            if (current != null) {
                // Calculate date 30 days ago
                long thirtyDaysAgo = current.getTime() - (30L * 24 * 60 * 60 * 1000);
                String cutoffDate = sdf.format(new Date(thirtyDaysAgo));

                // Delete old records
                database.waterIntakeDao().deleteOlderThan(username, cutoffDate);
                database.breathingSessionDao().deleteOlderThan(username, cutoffDate);
                database.focusSessionDao().deleteOlderThan(username, cutoffDate);
                database.lanternDao().deleteOlderThan(username, cutoffDate);
                database.dailyStatsDao().deleteOlderThan(username, cutoffDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculate wellness score based on daily activities
     * Score range: 0-100
     */
    private int calculateWellnessScore(int water, int breathingCycles, int focusMinutes, int lanternsReleased) {
        int score = 0;

        // Water: max 25 points (8 glasses = 25 points)
        score += Math.min(25, (water * 25) / 8);

        // Breathing: max 25 points (10 cycles = 25 points)
        score += Math.min(25, (breathingCycles * 25) / 10);

        // Focus: max 25 points (30 minutes = 25 points)
        score += Math.min(25, (focusMinutes * 25) / 30);

        // Lanterns: max 25 points (5 released = 25 points)
        score += Math.min(25, (lanternsReleased * 25) / 5);

        return score;
    }

    /**
     * Get current date in YYYY-MM-DD format
     */
    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Force a reset (for testing purposes)
     */
    public void forceReset(final String username) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                String currentDate = getCurrentDate();
                performDailyReset(username, currentDate, currentDate);
                prefs.edit().putString(KEY_LAST_RESET_DATE + "_" + username, currentDate).apply();
            }
        });
    }

    /**
     * Callback interface for reset completion
     */
    public interface ResetCallback {
        void onResetComplete(boolean wasReset);
    }
}