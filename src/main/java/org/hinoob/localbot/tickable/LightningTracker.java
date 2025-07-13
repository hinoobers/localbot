package org.hinoob.localbot.tickable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.hinoob.localbot.LocalBot;
import org.hinoob.localbot.datastore.GuildDatastore;
import org.hinoob.localbot.util.FileUtil;
import org.hinoob.localbot.util.JSONResponse;
import org.hinoob.localbot.util.LightningMapsClient;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LightningTracker extends Tickable{

    private final Map<String, ConcurrentLinkedQueue<MessageEmbed>> messageQueues = new ConcurrentHashMap<>();
    private int ticks = 0;

    public LightningTracker(JDA jda) {
        super(jda);
    }

    @Override
    protected void onTick() {
        if(++ticks % 5000 == 0) {
            ticks = 0;

            for (Map.Entry<String, ConcurrentLinkedQueue<MessageEmbed>> entry : messageQueues.entrySet()) {
                String channelId = entry.getKey();
                ConcurrentLinkedQueue<MessageEmbed> queue = entry.getValue();

                MessageEmbed embed = queue.poll();
                if (embed != null) {
                    jda.getTextChannelById(channelId).sendMessageEmbeds(embed).queue();
                }
            }

            if(messageQueues.size() > 100) {
                // Clear up queue
                messageQueues.clear();
            }
        }
    }

    @Override
    public void onStartup() {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        final int maxRetries = 5;
        final long retryDelayMs = 5000; // 5 seconds

        new Thread(() -> {
            int attempt = 0;
            while (attempt < maxRetries) {
                try {
                    container.connectToServer(
                            new LightningMapsClient(
                                    (float)57.59834501933561,
                                    (float)59.63967994527055,
                                    (float)21.613372799260702,
                                    (float)28.11219592864192,
                                    8),
                            URI.create("wss://live.lightningmaps.org/")
                    );
                    System.out.println("Connected successfully");
                    break;  // exit loop on success
                } catch (Exception e) {
                    attempt++;
                    System.err.println("Connection failed, attempt " + attempt + " of " + maxRetries);
                    e.printStackTrace();
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break; // stop retrying if interrupted
                    }
                }
            }
            if (attempt == maxRetries) {
                System.err.println("Max reconnect attempts reached, giving up.");
            }
        }).start();
    }

    public void onStrike(JsonArray strikes) {
        for (JsonElement strike : strikes) {
            float lat = strike.getAsJsonObject().get("lat").getAsFloat();
            float lon = strike.getAsJsonObject().get("lon").getAsFloat();

            JsonObject reverseData = reverse(lat, lon);
            if (reverseData == null || !reverseData.has("address")) {
                continue;
            }
            String country = reverseData.getAsJsonObject("address").get("country").getAsString();
            if(!country.equals("Eesti")) {
                // Just for now
                continue;
            }
            for (Map.Entry<String, GuildDatastore> datastore : LocalBot.getInstance().getDatastoreHandler().getAllGuildDatastores()) {
                JsonElement channelIdElem = datastore.getValue().get("lightning_channel").orElse(null);
                if (channelIdElem == null) continue;

                String channelId = channelIdElem.getAsString();

                MessageEmbed strikeEmbed = new EmbedBuilder()
                        .setTitle("Lightning strike detected!")
                        .addField("Country", country, true)
                        .addField("Coordinates", String.format("Lat: %.2f, Lon: %.2f", lat, lon), true)
                        .setImage("https://static-maps.yandex.ru/1.x/?ll=" + (lat + "," + lon) + "&size=300,200&z=14&l=sat&pt=" + (lat + "," + lon) + ",pm2rdm")
                        .build();

                // Add message to queue for the channel
                messageQueues.computeIfAbsent(channelId, k -> new ConcurrentLinkedQueue<>()).add(strikeEmbed);
            }
        }
    }

    private boolean isInside(float lat, float lon) {
        return lat >= 57.59834501933561 && lat <= 59.63967994527055 && lon >= 21.613372799260702 && lon <= 28.11219592864192;
    }

    private JsonObject reverse(float lat, float lon) {
        JSONResponse o = FileUtil.read("https://nominatim.openstreetmap.org/reverse?format=json&lat="+lat+"&lon="+lon);
        if(!o.isSuccessful()) return null;

        return o.getData();
    }
}
