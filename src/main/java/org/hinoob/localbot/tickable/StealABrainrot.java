package org.hinoob.localbot.tickable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import org.hinoob.localbot.LocalBot;
import org.hinoob.localbot.util.FileUtil;
import org.hinoob.localbot.util.JSONResponse;
import org.hinoob.localbot.util.MessageUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StealABrainrot extends Tickable {

    private boolean notified = false, notifiedLowLikes = false;
    private int currentGoal = -1;

    public StealABrainrot(JDA jda) {
        super(jda);
    }

    @Override
    public void onTick() {
        try {
            JSONResponse obj = FileUtil.read("https://games.roblox.com/v1/games/votes?universeIds=7709344486");
            if(!obj.isRateLimited()) {
                JsonArray data = obj.getAsJsonArray("data");
                if(data == null || data.isEmpty()) {
                    waitSeconds(15);
                    return;
                }

                int likes = data.get(0).getAsJsonObject().get("upVotes").getAsInt();
                int goal = getNextLikeGoal(likes);

                if(goal-likes <= 1000 && !notified) {
                    List<Long> notifiedList = new ArrayList<>();

                    for(Guild guild : jda.getGuilds()) {
                        for(Member member : guild.getMembers()) {
                            if(member.getUser().isBot()) continue;
                            if(!LocalBot.getInstance().getDatastoreHandler().getUserDatastore(member.getId()).contains("steal_brainrot_notifier")) continue;
                            if(notifiedList.contains(member.getIdLong())) continue;

                            notifiedList.add(member.getIdLong());

                            PrivateChannel channel = member.getUser().openPrivateChannel().complete();
                            if(channel != null) {
                                MessageEmbed embed = new EmbedBuilder()
                                        .setTitle("Steal a brainrot close to goal!")
                                        .addField("Goal", MessageUtil.beautifyCase(goal), true)
                                        .addField("Current Likes", MessageUtil.beautify(likes), true)
                                        .addField("To-go", MessageUtil.beautify(goal - likes), true)
                                        .setColor(Color.GREEN)
                                        .build();
                                channel.sendMessageEmbeds(embed).queue();
                            }
                        }
                    }
                    notified = true;
                } else if(goal-likes <= 500 && !notifiedLowLikes && notified) {
                    List<Long> notifiedList = new ArrayList<>();

                    for(Guild guild : jda.getGuilds()) {
                        for(Member member : guild.getMembers()) {
                            if(member.getUser().isBot()) continue;
                            if(!LocalBot.getInstance().getDatastoreHandler().getUserDatastore(member.getId()).contains("steal_brainrot_notifier")) continue;
                            if(notifiedList.contains(member.getIdLong())) continue;

                            notifiedList.add(member.getIdLong());

                            PrivateChannel channel = member.getUser().openPrivateChannel().complete();
                            if(channel != null) {
                                MessageEmbed embed = new EmbedBuilder()
                                        .setTitle("Steal a brainrot close to goal!")
                                        .addField("Goal", MessageUtil.beautifyCase(goal), true)
                                        .addField("Current Likes", MessageUtil.beautify(likes), true)
                                        .addField("To-go", MessageUtil.beautify(goal - likes), true)
                                        .setColor(Color.RED)
                                        .build();
                                channel.sendMessageEmbeds(embed).queue();
                            }
                        }
                    }
                    notifiedLowLikes = true;
                }

                if(currentGoal == -1 || goal > currentGoal) {
                    currentGoal = goal;
                    List<Long> notifiedList = new ArrayList<>();
                    for(Guild guild : jda.getGuilds()) {
                        for(Member member : guild.getMembers()) {
                            if (member.getUser().isBot()) continue;
                            if (!LocalBot.getInstance().getDatastoreHandler().getUserDatastore(member.getId()).contains("steal_brainrot_notifier"))
                                continue;
                            if (notifiedList.contains(member.getIdLong())) continue;
                            notifiedList.add(member.getIdLong());

                            PrivateChannel channel = member.getUser().openPrivateChannel().complete();
                            if (channel != null) {
                                MessageEmbed embed = new EmbedBuilder()
                                        .setTitle("Steal a brainrot NEW GOAL")
                                        .addField("Goal", MessageUtil.beautifyCase(goal), true)
                                        .addField("Current Likes", MessageUtil.beautify(likes), true)
                                        .setColor(Color.PINK)
                                        .build();
                                channel.sendMessageEmbeds(embed).queue();
                            }
                        }
                    }
                    notified = false;
                    notifiedLowLikes = false;
                }

                jda.getPresence().setActivity(Activity.of(Activity.ActivityType.PLAYING, "SaB " + MessageUtil.beautify(likes) + "/" + MessageUtil.beautifyCase(goal)));
            }
        } finally {
            waitSeconds(30);
        }
    }

    public static int getNextLikeGoal(int currentLikes) {
        int base = 2_000_000;
        int[] increments = {20_000, 30_000, 20_000, 30_000};
        int goal = base;
        int i = 0;

        while (goal <= currentLikes) {
            goal += increments[i % increments.length];
            i++;
        }

        return goal;
    }
}
