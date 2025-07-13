package org.hinoob.localbot.tickable;

import com.google.gson.JsonArray;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import org.hinoob.localbot.LocalBot;
import org.hinoob.localbot.util.ActivityTracker;
import org.hinoob.localbot.util.FileUtil;
import org.hinoob.localbot.util.JSONResponse;
import org.hinoob.localbot.util.MessageUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StealABrainrot extends Tickable {

    private boolean notified = false, notifiedLowLikes = false;
    private int currentGoal = -1, currentLikes, lastLikes;

    public StealABrainrot(JDA jda) {
        super(jda);
    }

    @Override
    public void onStartup() {
        LocalBot.getInstance().getActivityTracker().registerActivity(new ActivityTracker.ActivityEntry(60, 5, new ActivityTracker.ActivityEntry.UpdateCallback() {
            @Override
            public Activity update() {
                lastLikes = currentLikes;
                return Activity.of(Activity.ActivityType.PLAYING, "SaB " + MessageUtil.beautify(currentLikes) + "/" + MessageUtil.beautifyCase(currentGoal));
            }

            @Override
            public boolean shouldUpdate(long lastUpdate) {
                return lastLikes != currentLikes;
            }
        }));
    }

    @Override
    public void onTick() {
        try {
            JSONResponse obj = FileUtil.read("https://games.roblox.com/v1/games/votes?universeIds=7709344486");
            if (!obj.isSuccessful()) return;

            JsonArray data = obj.getAsJsonArray("data");
            if (data == null || data.isEmpty()) return;

            int likes = data.get(0).getAsJsonObject().get("upVotes").getAsInt();
            int goal = getNextLikeGoal(likes);
            List<MessageEmbed> embedsToSend = new ArrayList<>();

            int remaining = goal - likes;

            if (remaining <= 1000 && !notified) {
                embedsToSend.add(makeCloseEmbed(goal, likes, remaining, Color.GREEN));
                notified = true;
            } else if (remaining <= 500 && !notifiedLowLikes) {
                embedsToSend.add(makeCloseEmbed(goal, likes, remaining, Color.RED));
                notifiedLowLikes = true;
            }

            if (goal > currentGoal) {
                if (currentGoal != -1) {
                    embedsToSend.add(makeGoalReachedEmbed(goal, currentGoal, likes));
                    notified = false;
                    notifiedLowLikes = false;
                }
            }

            if (!embedsToSend.isEmpty()) {
                sendToAllNotifiedUsers(embedsToSend);
            }

            if (goal != currentGoal) currentGoal = goal;
            currentLikes = likes;

        } finally {
            waitSeconds(30);
        }
    }

    private MessageEmbed makeCloseEmbed(int goal, int likes, int remaining, Color color) {
        return new EmbedBuilder()
                .setTitle("Steal a brainrot close to goal!")
                .addField("Goal", MessageUtil.beautifyCase(goal), true)
                .addField("Current Likes", MessageUtil.beautify(likes), true)
                .addField("To-go", MessageUtil.beautify(remaining), true)
                .setColor(color)
                .build();
    }

    private MessageEmbed makeGoalReachedEmbed(int goal, int previousGoal, int likes) {
        return new EmbedBuilder()
                .setTitle("Steal a brainrot NEW GOAL")
                .addField("Goal", MessageUtil.beautifyCase(goal), true)
                .addField("Previous Goal", MessageUtil.beautifyCase(previousGoal), true)
                .addField("Current Likes", MessageUtil.beautify(likes), true)
                .setColor(Color.PINK)
                .build();
    }

    private void sendToAllNotifiedUsers(List<MessageEmbed> embeds) {
        List<Long> notifiedIds = new ArrayList<>();

        for (Guild guild : jda.getGuilds()) {
            for (Member member : guild.getMembers()) {
                if (member.getUser().isBot()) continue;
                if (!LocalBot.getInstance().getDatastoreHandler().getUserDatastore(member.getId())
                        .contains("steal_brainrot_notifier")) continue;
                if (notifiedIds.contains(member.getIdLong())) continue;

                notifiedIds.add(member.getIdLong());

                member.getUser().openPrivateChannel().queue(channel ->
                        channel.sendMessageEmbeds(embeds).queue()
                );
            }
        }
    }

    public static int getNextLikeGoal(int currentLikes) {
        int base = 2_000_000;
        int[] increments = {20_000, 30_000};
        int goal = base;
        int i = 0;

        while (goal <= currentLikes) {
            goal += increments[i % 2];
            i++;
        }

        return goal;
    }

    public int getLikes() {
        return currentLikes;
    }

    public int getCurrentGoal() {
        return currentGoal;
    }
}
