package org.hinoob.localbot.tickable;

import com.google.gson.JsonElement;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import org.hinoob.localbot.LocalBot;
import org.hinoob.localbot.datastore.GuildDatastore;
import org.hinoob.localbot.util.GeoguessPicture;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

            if(!geoguessPictures.containsKey(datastore.getKey())) {
                geoguessPictures.put(datastore.getKey(), GeoguessPicture.getRandom());

                channel.sendFiles(FileUpload.fromData(geoguessPictures.get(datastore.getKey()).getFile())).queue();
                channel.sendMessage("Guess the location!").queue();
            }
        }
    }

    public void resetPicture(String guild, String channelId) {
        TextChannel channel = jda.getGuildById(guild).getTextChannelById(channelId);
        if (channel == null) return;

        channel.getHistory().retrievePast(5).queue(messages -> messages.forEach(message -> message.delete().queue(null, e -> {})));

        geoguessPictures.remove(guild);
    }

    public GeoguessPicture getPictureForGuild(String guildId) {
        return geoguessPictures.get(guildId);
    }
}
