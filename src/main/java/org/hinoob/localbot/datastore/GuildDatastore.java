package org.hinoob.localbot.datastore;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.Optional;

public class GuildDatastore implements Datastorage{

    private String guildId;
    private File file;
    private JsonObject data;

    public GuildDatastore(String guildId) {
        this.guildId = guildId;
        load();
    }

    @Override
    public void load() {
        this.file = new File("guilds/" + guildId + ".json");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs(); // Ensure the directory exists
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Gson gson = new Gson();
        try {
            data = gson.fromJson(new java.io.FileReader(file), JsonObject.class);
            if (data == null) {
                data = new JsonObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
            data = new JsonObject();
        }
    }

    @Override
    public Optional<JsonElement> get(String key) {
        if (data.has(key)) {
            return Optional.of(data.get(key));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void set(String key, JsonElement value) {
        data.add(key, value);
        Gson gson = new Gson();
        try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
            gson.toJson(data, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setString(String key, String value) {
        data.addProperty(key, value);
        Gson gson = new Gson();
        try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
            gson.toJson(data, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<String> getString(String key) {
        if (data.has(key)) {
            return Optional.of(data.get(key).getAsString());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void delete(String key) {
        if (data.has(key)) {
            data.remove(key);
            Gson gson = new Gson();
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                gson.toJson(data, writer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean contains(String key) {
        return data.has(key);
    }
}
