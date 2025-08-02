package org.hinoob.localbot.util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import org.hinoob.localbot.command.impl.UptimeCommand;
import org.hinoob.localbot.tickable.Tickable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ActivityTracker extends Tickable {

    private final List<ActivityEntry> activities = new ArrayList<>();
    private final Deque<Long> timestamps = new ArrayDeque<>();
    private int currentIndex = 0;
    private int ticksRemaining = 0;
    private int ticks = 0;
    private long lastUpdate = System.currentTimeMillis();

    private static final int MAX_UPDATES = 5;
    private static final long WINDOW_MILLIS = 60_000;

    public ActivityTracker(JDA jda) {
        super(jda);
    }

    @Override
    public void onStartup() {
        ActivityEntry entry = new ActivityEntry(60, 3, new ActivityEntry.UpdateCallback() {
            @Override
            public Activity update() {
                long uptimeMillis = System.currentTimeMillis() - UptimeCommand.startup;
                int seconds = (int) (uptimeMillis / 1000);
                int minutes = seconds / 60;

                String uptimeMessage;
                if(minutes > 1000) {
                    uptimeMessage = "Uptime: " + (minutes / 60) + " hours";
                } else if(seconds > 1000) {
                    uptimeMessage = "Uptime: " + minutes + " minutes";
                } else {
                    uptimeMessage = "Uptime: " + seconds + " seconds";
                }

                return Activity.of(Activity.ActivityType.PLAYING, uptimeMessage);
            }

            @Override
            public boolean shouldUpdate(long lastUpdate) {
                return System.currentTimeMillis() - lastUpdate > (12_000/5);
            }
        });
        registerActivity(entry);
    }

    @Override
    protected void onTick() {
        ++ticks;

        ActivityEntry current = activities.get(currentIndex);

        if(current.callback != null && ticksRemaining > 1) { // 5 times / minute, according to discord's rate limit
            boolean forcedUpdate = (ticks % 12_000 == 0) && tryUpdate();
            boolean dynamicUpdate = current.callback.shouldUpdate(lastUpdate) && tryUpdate();

            if(forcedUpdate || dynamicUpdate) {
                current.update();

                jda.getPresence().setActivity(current.getActivity());
                lastUpdate = System.currentTimeMillis();
                ticks = 0;
            }
        }

        ticksRemaining--;
        if (ticksRemaining <= 0) {
            currentIndex = (currentIndex + 1) % activities.size();
            current.update();
            current = activities.get(currentIndex);
            jda.getPresence().setActivity(current.getActivity());
            ticksRemaining = current.duration * 1000;
        }
    }

    public void registerActivity(ActivityEntry activity) {
        if (activity == null || activity.duration <= 0) {
            throw new IllegalArgumentException("Invalid activity parameters");
        }
        activities.add(activity);
    }

    public synchronized boolean tryUpdate() {
        long now = System.currentTimeMillis();

        // Remove expired entries
        while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MILLIS) {
            timestamps.pollFirst();
        }

        if (timestamps.size() < MAX_UPDATES) {
            timestamps.addLast(now);
            return true;
        }

        return false;
    }

    public static class ActivityEntry {
        private int duration; // in seconds
        private int priority;
        private UpdateCallback callback;

        protected net.dv8tion.jda.api.entities.Activity activity;

        public ActivityEntry(int duration, int priority, UpdateCallback callback) {
            this.duration = duration;
            this.priority = priority;
            this.callback = callback;
        }

        public net.dv8tion.jda.api.entities.Activity getActivity() {
            if (activity == null) {
                update();
            }
            return activity;
        }

        public void update() {
            if (callback != null) {
                activity = callback.update();
            }
        }

        public interface UpdateCallback {
            net.dv8tion.jda.api.entities.Activity update();
            boolean shouldUpdate(long lastUpdate); // Will try to update if it can
        }
    }
}
