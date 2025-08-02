package org.hinoob.localbot.tickable;

import com.google.gson.JsonElement;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import org.hinoob.localbot.LocalBot;
import org.hinoob.localbot.datastore.GuildDatastore;
import org.hinoob.localbot.util.GeoguessPicture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class GeoguessGame extends Tickable{
    public GeoguessGame(JDA jda) {
        super(jda);
    }

    private final Map<String, GuildData> guildDataHashMap = new HashMap<>();
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

            GuildData guildDatastore = guildDataHashMap.computeIfAbsent(datastore.getKey(), k -> new GuildData());
            if(guildDatastore.updating) continue;

            boolean changeCondition = !geoguessPictures.containsKey(datastore.getKey()) || (geoguessPictures.containsKey(datastore.getKey()) && geoguessPictures.get(datastore.getKey()).isNeedsChange());
            if(changeCondition) {

                Runnable run = () -> {
                    if(!geoguessPictures.containsKey(datastore.getKey()) || geoguessPictures.get(datastore.getKey()).isNeedsChange()) {
                        GeoguessPicture picture = geoguessPictures.getOrDefault(datastore.getKey(), GeoguessPicture.getRandom(null));
                        geoguessPictures.put(datastore.getKey(), picture);

                        picture.setNeedsChange(false);

                        channel.sendFiles(FileUpload.fromData(picture.getFile())).queue(firstMsg -> {
                            guildDatastore.messageIds.add(firstMsg.getId());
                            channel.sendMessage("`Guess the country!`").queue(secondMsg -> {
                                guildDatastore.messageIds.add(secondMsg.getId());
                                guildDatastore.updating = false;
                            });
                        });
                    }
                };

                if(guildDatastore.messageIds.isEmpty()) {
                    run.run();
                } else if(!geoguessPictures.containsKey(datastore.getKey()) || geoguessPictures.get(datastore.getKey()).isNeedsChange()){
                    guildDatastore.updating = true;
                    if(guildDatastore.messageIds.size() == 1) {
                        channel.deleteMessageById(guildDatastore.messageIds.getFirst()).queue(_ -> {
                            run.run();
                        }, e -> {});
                    } else {
                        channel.deleteMessagesByIds(guildDatastore.messageIds).queue(_ -> {
                            // I don't know if this callback runs for every message dfeleted or once they're all deleted
                            run.run();
                        }, e -> {
                        });
                    }
                }
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

    public class GuildData {
        private boolean updating;
        private List<String> messageIds;

        public GuildData() {
            this.updating = false;
            this.messageIds = new ArrayList<>();
        }
    }
}
