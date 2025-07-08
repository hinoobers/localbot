package org.hinoob.localbot.datastore;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Optional;

public interface Datastorage {

    Gson GSON = new Gson();

    void load();

    Optional<JsonElement> get(String key);

    void set(String key, JsonElement value);

    void setString(String key, String value);

    Optional<String> getString(String key); // <-- added this

    void delete(String key);

    boolean contains(String key);
}