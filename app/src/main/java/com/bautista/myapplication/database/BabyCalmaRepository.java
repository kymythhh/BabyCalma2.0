package com.bautista.myapplication.database;

import android.content.Context;
import android.content.SharedPreferences;

import com.bautista.myapplication.database.entities.AffirmationEntity;
import com.bautista.myapplication.database.entities.BreathingSessionEntity;
import com.bautista.myapplication.database.entities.FocusSessionEntity;
import com.bautista.myapplication.database.entities.LanternEntity;
import com.bautista.myapplication.database.entities.UserProfileEntity;
import com.bautista.myapplication.database.entities.WaterIntakeEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Repository class to handle all database operations.
 * Provides a clean API for Activities to interact with the database.
 */
public class BabyCalmaRepository {

    private final BabyCalmaDatabase database;
    private final Executor executor;
    private final Context context;

    public BabyCalmaRepository(Context context) {
        this.context = context;
        this.database = BabyCalmaDatabase.getInstance(context);
        this.executor = Executors.newSingleThreadExecutor();
    }

    // ==================== WATER INTAKE ====================

    public void saveWaterIntake(final int glassCount, final String date, final DatabaseCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    WaterIntakeEntity existing = database.waterIntakeDao().getByDate(date);
                    if (existing != null) {
                        existing.glassCount = glassCount;
                        existing.timestamp = System.currentTimeMillis();
                        database.waterIntakeDao().update(existing);
                    } else {
                        WaterIntakeEntity entity = new WaterIntakeEntity(
                                glassCount,
                                date,
                                System.currentTimeMillis()
                        );
                        database.waterIntakeDao().insert(entity);
                    }
                    if (callback != null) callback.onSuccess();
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }

    public void getWaterIntakeForToday(final String date, final DataCallback<Integer> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int count = database.waterIntakeDao().getTotalGlassesForDate(date);
                    if (callback != null) callback.onDataLoaded(count);
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }

    // ==================== BREATHING SESSIONS ====================

    public void saveBreathingSession(final int cycles, final int durationSeconds,
                                     final String date, final DatabaseCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    BreathingSessionEntity entity = new BreathingSessionEntity(
                            cycles,
                            durationSeconds,
                            date,
                            System.currentTimeMillis()
                    );
                    database.breathingSessionDao().insert(entity);
                    if (callback != null) callback.onSuccess();
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }

    public void getTotalBreathingCyclesForToday(final String date, final DataCallback<Integer> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int total = database.breathingSessionDao().getTotalCyclesForDate(date);
                    if (callback != null) callback.onDataLoaded(total);
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }

    // ==================== FOCUS SESSIONS ====================

    public void saveFocusSession(final int durationMinutes, final String date,
                                 final boolean completed, final DatabaseCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    FocusSessionEntity entity = new FocusSessionEntity(
                            durationMinutes,
                            date,
                            System.currentTimeMillis(),
                            completed
                    );
                    database.focusSessionDao().insert(entity);
                    if (callback != null) callback.onSuccess();
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }

    public void getTotalFocusMinutesForToday(final String date, final DataCallback<Integer> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int total = database.focusSessionDao().getTotalMinutesForDate(date);
                    if (callback != null) callback.onDataLoaded(total);
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }

    // ==================== LANTERNS ====================

    public void saveLantern(final String stressorText, final String date, final DataCallback<Long> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    LanternEntity entity = new LanternEntity(
                            stressorText,
                            date,
                            System.currentTimeMillis(),
                            false
                    );
                    long id = database.lanternDao().insert(entity);
                    if (callback != null) callback.onDataLoaded(id);
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }

    public void releaseLantern(final int lanternId, final DatabaseCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    database.lanternDao().markAsReleased(lanternId, System.currentTimeMillis());
                    if (callback != null) callback.onSuccess();
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }

    public void getActiveLanterns(final String date, final DataCallback<List<LanternEntity>> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    List<LanternEntity> lanterns = database.lanternDao().getActiveLanternsForDate(date);
                    if (callback != null) callback.onDataLoaded(lanterns);
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }

    public void getLanternStats(final String date, final DataCallback<LanternStats> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int total = database.lanternDao().getTotalCountForDate(date);
                    int active = database.lanternDao().getActiveCountForDate(date);
                    int released = database.lanternDao().getReleasedCountForDate(date);

                    LanternStats stats = new LanternStats(total, active, released);
                    if (callback != null) callback.onDataLoaded(stats);
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }

    // ==================== USER PROFILE ====================

    public void saveUserProfile(final String username, final String firstLetter,
                                final String createdDate, final DatabaseCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    UserProfileEntity entity = new UserProfileEntity(
                            username,
                            firstLetter,
                            createdDate,
                            System.currentTimeMillis()
                    );
                    database.userProfileDao().insert(entity);
                    if (callback != null) callback.onSuccess();
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }

    public void getUserProfile(final DataCallback<UserProfileEntity> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    UserProfileEntity profile = database.userProfileDao().getUserProfile();
                    if (callback != null) callback.onDataLoaded(profile);
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }

    // ==================== AFFIRMATIONS ====================

    private static final String PREFS_NAME = "AffirmationPrefs";
    private static final String KEY_LAST_DATE = "last_affirmation_date";
    private static final String KEY_CURRENT_INDEX = "current_affirmation_index";

    private static final String[] DEFAULT_AFFIRMATIONS = {
            "Calmness and clarity guide today's actions.",
            "Peace, happiness, and success are deserved.",
            "Thoughts chosen today support growth and confidence.",
            "Today's efforts build a better tomorrow.",
            "Strength increases with every challenge faced.",
            "Trust in good decisions comes naturally.",
            "Stress is released and inner peace is welcomed.",
            "Progress made so far is worthy of pride.",
            "Mind and body work together in balance.",
            "Positive energy flows into everyday life.",
            "Love and respect are always deserved.",
            "Any challenge today can be handled with confidence.",
            "Focus, motivation, and productivity come easily.",
            "Progress matters more than perfection.",
            "Confidence is inhaled and doubt is exhaled.",
            "Growth happens every single day.",
            "Gratitude fills this day with opportunity.",
            "Kindness and patience are given freely to the self.",
            "Success is possible through steady effort.",
            "Peace is chosen over worry today."
    };

    public void initializeAffirmations(final DatabaseCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int count = database.affirmationDao().getAffirmationCount();
                    if (count == 0) {
                        List<AffirmationEntity> affirmations = new ArrayList<>();
                        for (int i = 0; i < DEFAULT_AFFIRMATIONS.length; i++) {
                            affirmations.add(new AffirmationEntity(DEFAULT_AFFIRMATIONS[i], i + 1));
                        }
                        database.affirmationDao().insertAll(affirmations);
                    }
                    if (callback != null) callback.onSuccess();
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }

    public void getDailyAffirmation(final String currentDate, final DataCallback<String> callback) {
        getDailyAffirmation(currentDate, false, callback);
    }

    /**
     * Get daily affirmation with option to force a new random selection.
     * @param currentDate Current date string
     * @param forceNew If true, always generate a new random affirmation
     * @param callback Callback to receive the affirmation
     */
    public void getDailyAffirmation(final String currentDate, final boolean forceNew, final DataCallback<String> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    String lastDate = prefs.getString(KEY_LAST_DATE, "");
                    int currentIndex = prefs.getInt(KEY_CURRENT_INDEX, -1);

                    // Get all affirmations
                    List<AffirmationEntity> allAffirmations = database.affirmationDao().getAllAffirmations();

                    if (allAffirmations == null || allAffirmations.isEmpty()) {
                        if (callback != null) callback.onDataLoaded("Take a deep breath and believe in yourself.");
                        return;
                    }

                    // Generate new affirmation if: new day, first time, or forced refresh
                    if (forceNew || !currentDate.equals(lastDate) || currentIndex < 0) {
                        Random random = new Random();
                        currentIndex = random.nextInt(allAffirmations.size());

                        // Save the new date and index
                        prefs.edit()
                                .putString(KEY_LAST_DATE, currentDate)
                                .putInt(KEY_CURRENT_INDEX, currentIndex)
                                .apply();
                    }

                    // Return the affirmation for today
                    String affirmation = allAffirmations.get(currentIndex).affirmationText;
                    if (callback != null) callback.onDataLoaded(affirmation);
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }

    /**
     * Clear affirmation cache to force a new random selection on next load.
     * Call this on logout to ensure fresh affirmation on next login.
     */
    public void clearAffirmationCache() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(KEY_LAST_DATE)
                .remove(KEY_CURRENT_INDEX)
                .apply();
    }

    // ==================== CALLBACKS ====================

    public interface DatabaseCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface DataCallback<T> {
        void onDataLoaded(T data);
        void onError(Exception e);
    }

    // ==================== HELPER CLASSES ====================

    public static class LanternStats {
        public final int total;
        public final int active;
        public final int released;

        public LanternStats(int total, int active, int released) {
            this.total = total;
            this.active = active;
            this.released = released;
        }
    }
}