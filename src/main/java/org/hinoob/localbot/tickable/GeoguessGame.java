package org.hinoob.localbot.tickable;

import com.google.gson.JsonElement;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import org.hinoob.localbot.LocalBot;
import org.hinoob.localbot.datastore.GuildDatastore;
import org.hinoob.localbot.util.GeoguessPicture;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class GeoguessGame extends Tickable{
    public GeoguessGame(JDA jda) {
        super(jda);
    }

    private final Map<String, GeoguessPicture> geoguessPictures = new HashMap<>();

    @Override
    public void onStartup() {
        for(Map.Entry<String, GuildDatastore> datastore : LocalBot.getInstance().getDatastoreHandler().getAllGuildDatastores()) {
            JsonElement channelId = datastore.getValue().get("geoguess_channel").orElse(null);
            if(channelId == null) continue;

            TextChannel channel = jda.getGuildById(datastore.getKey()).getTextChannelById(channelId.getAsString());
            System.out.println("Deleting old geoguess messages in channel: " + channel.getName());
            channel.getHistory().retrievePast(100).queue(messages -> {
                for(Message m : messages) {
                    m.delete().queue();
                }
            });
        }
    }

    @Override
    protected void onTick() {
        for(Map.Entry<String, GuildDatastore> datastore : LocalBot.getInstance().getDatastoreHandler().getAllGuildDatastores()) {
            JsonElement channelId = datastore.getValue().get("geoguess_channel").orElse(null);
            if(channelId == null) continue;

            TextChannel channel = jda.getGuildById(datastore.getKey()).getTextChannelById(channelId.getAsString());
            try {
                long a = channel.getLatestMessageIdLong() - 4;
            } catch(Exception ignored) {
                continue;
            }

            boolean changeCondition = !geoguessPictures.containsKey(datastore.getKey()) || geoguessPictures.get(datastore.getKey()).needsChange();
            if(changeCondition) {
                channel.getHistory().retrievePast(5).queue(messages -> {
                    AtomicInteger count = new AtomicInteger();

                    Runnable run = () -> {
                        if(!geoguessPictures.containsKey(datastore.getKey()) || geoguessPictures.get(datastore.getKey()).needsChange()) {
                            GeoguessPicture picture = geoguessPictures.getOrDefault(datastore.getKey(), GeoguessPicture.getRandom(null));
                            geoguessPictures.put(datastore.getKey(), picture);

                            picture.setNeedsChange(false);

                            channel.sendFiles(FileUpload.fromData(picture.getFile())).queue(_ -> channel.sendMessage("`Guess the country!`").queue());
                        }
                    };

                    if(messages.isEmpty()) {
                        run.run();
                    } else if(!geoguessPictures.containsKey(datastore.getKey()) || geoguessPictures.get(datastore.getKey()).needsChange()){
                        messages.forEach(m -> m.delete().queue(unused -> {
                            count.incrementAndGet();
                            if(count.get() == messages.size()) {
                                run.run();
                            }
                        }, e -> {
                        }));
                    }

                });
            }
        }
    }

    public void resetPicture(String guild, String channelId) {
        TextChannel channel = jda.getGuildById(guild).getTextChannelById(channelId);
        if (channel == null) return;

        geoguessPictures.get(guild).reset();
    }

    public GeoguessPicture getPictureForGuild(String guildId) {
        return geoguessPictures.get(guildId);
    }
}
