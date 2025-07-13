package org.hinoob.localbot.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.websocket.*;
import org.hinoob.localbot.LocalBot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ClientEndpoint
public class LightningMapsClient {

    private float minLat, maxLat, minLon, maxLon;
    private int zoom;

    public LightningMapsClient(float minLat, float maxLat, float minLon, float maxLon, int zoom) {
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;
        this.zoom = zoom;
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to server");

        double latRange = Math.abs(maxLat - minLat);
        double lonRange = Math.abs(maxLon - minLon);

        int windowWidth = estimateWindowWidth(zoom);
        int lValue = estimateL(latRange, lonRange);

        // TODO: Reverse engineer these values more
        Map<String, Object> initMsg = new HashMap<>();
        initMsg.put("v", 24);
        initMsg.put("i", Map.of());
        initMsg.put("s", false);
        initMsg.put("x", 0);
        initMsg.put("w", windowWidth);
        initMsg.put("tx", 0);
        initMsg.put("tw", windowWidth);
        initMsg.put("a", 4);
        initMsg.put("z", zoom);
        initMsg.put("b", true);
        initMsg.put("from_lightningmaps_org", true);
        initMsg.put("h", "");
        initMsg.put("l", lValue);
        initMsg.put("p", List.of(minLat, maxLat, minLon, maxLon));
        initMsg.put("r", "v");
        initMsg.put("t", (System.currentTimeMillis() / 1000.0));

        String json = new Gson().toJson(initMsg);
        try {
            session.getBasicRemote().sendText(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(String message) {
        JsonObject o = new Gson().fromJson(message, JsonObject.class);
        if(o.has("strokes")) {
            LocalBot.getInstance().getLightningTracker().onStrike(o.getAsJsonArray("strokes"));
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        throw new RuntimeException("Connection closed: " + closeReason.getReasonPhrase());
    }


    private int estimateWindowWidth(int zoom) {
        return Math.max(0, 6 - zoom); // rough: zoom 5 → w=1, zoom 6 → w=2, etc.
    }

    private int estimateL(double latRange, double lonRange) {
        return (int) Math.ceil(latRange * lonRange * 0.8); // empirical scaling factor
    }
}