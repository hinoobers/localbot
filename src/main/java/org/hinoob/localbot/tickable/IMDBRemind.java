package org.hinoob.localbot.tickable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import org.hinoob.localbot.LocalBot;
import org.hinoob.localbot.datastore.UserDatastore;
import org.hinoob.localbot.util.DateParser;
import org.hinoob.localbot.util.FileUtil;
import org.hinoob.localbot.util.JSONResponse;
import org.hinoob.localbot.util.OmdbAPI;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class IMDBRemind extends Tickable{
    public IMDBRemind(JDA jda) {
        super(jda);
    }

    @Override
    protected void onTick() {
        for(Map.Entry<String, UserDatastore> key : LocalBot.getInstance().getDatastoreHandler().getAllUserDatastores()) {
            UserDatastore datastore = key.getValue();
            User member = jda.getUserById(key.getKey());

            JsonElement imdbReminders = datastore.get("imdb_reminders").orElse(null);
            if(imdbReminders == null || !imdbReminders.isJsonArray()) {
                continue;
            }

            JsonArray reminders = imdbReminders.getAsJsonArray();
            List<JsonElement> toRemove = new ArrayList<>();
            for(JsonElement reminder : reminders) {
                JsonObject reminderObj = reminder.getAsJsonObject();

                String imdbId = reminderObj.get("imdb_id").getAsString();
                if(reminderObj.has("target_season")) {
                    int targetSeason = reminderObj.get("target_season").getAsInt();

                    JSONResponse data = OmdbAPI.get(imdbId, targetSeason);
                    if(data.isRateLimited() || !data.get("Response").getAsBoolean()) {
                        continue;
                    }
                    JsonArray episodes = data.getAsJsonArray("Episodes");

                    if(episodes == null || episodes.isEmpty()) {
                        continue;
                    }

                    if(episodes.get(0).getAsJsonObject().get("Released").getAsString().equals("N/A")) {
                        continue; // No release date available
                    }

                    toRemove.add(reminder);

                    // here we can assume it has released
                    member.openPrivateChannel().queue(privateChannel -> {
                        MessageEmbed embed = new net.dv8tion.jda.api.EmbedBuilder()
                                .setTitle("IMDB Reminder")
                                .setDescription("The show with IMDB ID " + imdbId + " has released!")
                                .addField("Name", data.get("Title").getAsString(), true)
                                .addField("Season", String.valueOf(targetSeason), true)
                                .setColor(0x00FF00)
                                .build();
                        privateChannel.sendMessageEmbeds(embed).queue();
                    });
                } else {
                    JSONResponse data = OmdbAPI.get(imdbId, null);
                    if(data.isRateLimited() || !data.get("Response").getAsBoolean() || !data.has("Released") || data.get("Released").getAsString().equals("N/A")) {
                        continue; // No release date available
                    }

                    String releaseDate = data.get("Released").getAsString(); // 27 Jun 2025
                    if(releaseDate.isEmpty() || releaseDate.equals("N/A")) {
                        continue; // No release date available
                    }

                    LocalDate date = DateParser.parse(releaseDate);
                    LocalDate now = LocalDate.now();

                    if (now.isBefore(date)) {
                        // Not yet released
                        continue;
                    }

                    toRemove.add(reminder);

                    // here we can assume it has released
                    member.openPrivateChannel().queue(privateChannel -> {
                        MessageEmbed embed = new net.dv8tion.jda.api.EmbedBuilder()
                                .setTitle("IMDB Reminder")
                                .setDescription("The show with IMDB ID " + imdbId + " has released!")
                                .addField("Name", data.get("Title").getAsString(), true)
                                .addField("Release Date", releaseDate, true)
                                .setColor(0x00FF00)
                                .build();
                        privateChannel.sendMessageEmbeds(embed).queue();
                    });
                }
            }

            toRemove.forEach(reminders::remove);
            datastore.set("imdb_reminders", reminders);
        }

        waitSeconds(90);
    }
}
