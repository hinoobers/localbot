package org.hinoob.localbot.command.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.hinoob.localbot.LocalBot;
import org.hinoob.localbot.command.Command;
import org.hinoob.localbot.datastore.UserDatastore;
import org.hinoob.localbot.util.DateParser;
import org.hinoob.localbot.util.FileUtil;
import org.hinoob.localbot.util.JSONResponse;
import org.hinoob.localbot.util.OmdbAPI;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class IMDBRemindCommand extends Command {
    public IMDBRemindCommand() {
        super("imdbremind");
    }

    @Override
    public void handle(MessageReceivedEvent event, String[] args) {
        UserDatastore userDatastore = LocalBot.getInstance().getDatastoreHandler().getUserDatastore(event.getAuthor().getId());
        JsonArray reminders = userDatastore.get("imdb_reminders").orElse(new JsonArray()).getAsJsonArray();

        if(args.length == 2) {
            String imdbId = args[1];
            // To save api calls, lets do some lazy checks to see if imdb is valid
            if(!imdbId.matches("tt\\d{7,8}")) {
                event.getChannel().sendMessage("❗ Invalid IMDb ID format. Please use the format `tt1234567` or `tt12345678`.").queue();
                return;
            }

            // Check if the IMDb ID is already in the reminders
            for (int i = 0; i < reminders.size(); i++) {
                JsonObject reminder = reminders.get(i).getAsJsonObject();
                if (reminder.has("imdb_id") && !reminder.has("target_season") && reminder.get("imdb_id").getAsString().equals(imdbId)) {
                    event.getChannel().sendMessage("❗ You already have a reminder set for the movie/series with IMDb ID " + imdbId + ".").queue();
                    return;
                }
            }

            JSONResponse o = OmdbAPI.get(imdbId, null);
            if(o.isRateLimited()) {
                event.getChannel().sendMessage("❗ Rate limit exceeded. Please try again later.").queue();
                return;
            }
            if(!o.get("Response").getAsBoolean()) {
                event.getChannel().sendMessage(o.get("Error").getAsString()).queue();
            } else {
                String releaseDate = o.get("Released").getAsString();
                if(!releaseDate.equalsIgnoreCase("N/A")) {
                    LocalDate date =DateParser.parse(releaseDate);
                    LocalDate now = LocalDate.now();

                    if (now.isAfter(date)) {
                        // Not yet released
                        event.getChannel().sendMessage("❗ The movie/series with IMDb ID " + imdbId + " has already been released on " + DateParser.parse(releaseDate) + ".").queue();
                        event.getChannel().sendMessage("Looking for a specific season? If so, use the command like this: `!imdbremind <imdb_id> <season_number>`").queue();
                    } else {
                        JsonObject reminderObj = new JsonObject();
                        reminderObj.addProperty("imdb_id", imdbId);

                        reminders.add(reminderObj);

                        event.getChannel().sendMessage("✅ Reminder set for the movie/series with IMDb ID " + imdbId + " releasing on " + DateParser.parse(releaseDate) + ".").queue();
                    }

                } else {
                    JsonObject reminderObj = new JsonObject();
                    reminderObj.addProperty("imdb_id", imdbId);

                    reminders.add(reminderObj);
                    event.getChannel().sendMessage("✅ Reminder set for the movie/series with IMDb ID " + imdbId + " (release date unknown).").queue();
                }
            }
        } else if(args.length == 3) {
            String imdbId = args[1];
            if(!imdbId.matches("tt\\d{7,8}")) {
                event.getChannel().sendMessage("❗ Invalid IMDb ID format. Please use the format `tt1234567` or `tt12345678`.").queue();
                return;
            }
            if(!args[2].matches("\\d+")) {
                event.getChannel().sendMessage("❗ Invalid season number. Please provide a valid integer.").queue();
                return;
            }
            int season = Integer.parseInt(args[2]);

            for (int i = 0; i < reminders.size(); i++) {
                JsonObject reminder = reminders.get(i).getAsJsonObject();
                if (reminder.has("imdb_id") && reminder.has("target_season") && reminder.get("imdb_id").getAsString().equals(imdbId) && reminder.get("target_season").getAsInt() == season) {
                    event.getChannel().sendMessage("❗ You already have a reminder set for the movie/series with IMDb ID " + imdbId + " and Season " + season + ".").queue();
                    return;
                }
            }

            JSONResponse o = OmdbAPI.get(imdbId, season);
            if(o.isRateLimited()) {
                event.getChannel().sendMessage("❗ Rate limit exceeded. Please try again later.").queue();
                return;
            }

            if(!o.get("Response").getAsBoolean()) {
                event.getChannel().sendMessage(o.get("Error").getAsString()).queue();
            } else {
                JsonArray episodes = o.getAsJsonArray("Episodes");
                if(episodes == null || episodes.isEmpty()) {
                    JsonObject reminderObj = new JsonObject();
                    reminderObj.addProperty("imdb_id", imdbId);
                    reminderObj.addProperty("target_season", season);

                    reminders.add(reminderObj);
                    event.getChannel().sendMessage("✅ Reminder set for the series with IMDb ID " + imdbId + " Season " + season + " (release date unknown).").queue();
                } else if(episodes.get(0).getAsJsonObject().get("Released").getAsString().equals("N/A")) {
                    JsonObject reminderObj = new JsonObject();
                    reminderObj.addProperty("imdb_id", imdbId);
                    reminderObj.addProperty("target_season", season);

                    reminders.add(reminderObj);
                    event.getChannel().sendMessage("✅ Reminder set for the series with IMDb ID " + imdbId + " Season " + season + " (release date unknown).").queue();
                } else {
                    String releaseDate = episodes.get(0).getAsJsonObject().get("Released").getAsString();
                    LocalDate date = DateParser.parse(releaseDate);
                    LocalDate now = LocalDate.now();

                    if (now.isAfter(date)) {
                        // Not yet released
                        event.getChannel().sendMessage("❗ The series with IMDb ID " + imdbId + " Season " + season + " has already been released on " + DateParser.parse(releaseDate) + ".").queue();
                    } else {
                        JsonObject reminderObj = new JsonObject();
                        reminderObj.addProperty("imdb_id", imdbId);
                        reminderObj.addProperty("target_season", season);

                        reminders.add(reminderObj);
                        event.getChannel().sendMessage("✅ Reminder set for the series with IMDb ID " + imdbId + " Season " + season + " releasing on " + DateParser.parse(releaseDate) + ".").queue();
                    }
                }
            }
        }

        userDatastore.set("imdb_reminders", reminders);
    }
}
