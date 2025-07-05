package org.hinoob.localbot.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class JSONResponse {

    private final boolean rateLimited;
    private final JsonObject data;

    public JSONResponse(JsonObject o) {
        this.rateLimited = false;
        this.data = o;
    }

    public JSONResponse(boolean rateLimited) {
        this.rateLimited = rateLimited;
        this.data = new JsonObject();
    }

    public boolean isRateLimited() {
        return rateLimited;
    }

    public JsonElement get(String key) {
        return data.get(key);
    }

    public JsonArray getAsJsonArray(String key) {
        return data.getAsJsonArray(key);
    }

    public boolean has(String key) {
        return data.has(key);
    }

    public JsonObject getData() {
        return data;
    }
}
