package org.hinoob.localbot.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class JSONResponse {

    private final boolean successful;
    private final JsonObject data;

    public JSONResponse(JsonObject o) {
        this.successful = true;
        this.data = o;
    }

    public JSONResponse(boolean successful) {
        this.successful = successful;
        this.data = new JsonObject();
    }

    public boolean isSuccessful() {
        return successful;
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
