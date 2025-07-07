package org.hinoob.localbot.datastore;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DatastoreHandler {

    private GlobalDatastore globalDatastore = new GlobalDatastore();
    private Map<String, UserDatastore> userDatastores = new HashMap<>();
    private Map<String, GuildDatastore> guildDatastores = new HashMap<>();

    public DatastoreHandler() {
        globalDatastore.load();

        File usersDir = new File("users");
        if (!usersDir.exists()) {
            usersDir.mkdirs();
        }
        for(File f : usersDir.listFiles()) {
            if(f.isFile() && f.getName().endsWith(".json")) {
                String userId = f.getName().replace(".json", "");
                userDatastores.put(userId, new UserDatastore(userId));
            }
        }

        File guildsDir = new File("guilds");
        if (!guildsDir.exists()) {
            guildsDir.mkdirs();
        }
        for(File f : guildsDir.listFiles()) {
            if(f.isFile() && f.getName().endsWith(".json")) {
                String guildId = f.getName().replace(".json", "");
                guildDatastores.put(guildId, new GuildDatastore(guildId));
            }
        }
    }

    public GlobalDatastore getGlobalDatastore() {
        return globalDatastore;
    }

    public UserDatastore getUserDatastore(String userId) {
        return userDatastores.computeIfAbsent(userId, UserDatastore::new);
    }

    public GuildDatastore getGuildDatastore(String guildId) {
        return guildDatastores.computeIfAbsent(guildId, GuildDatastore::new);
    }

    public Set<Map.Entry<String, UserDatastore>> getAllUserDatastores() {
        return userDatastores.entrySet();
    }

    public Set<Map.Entry<String, GuildDatastore>> getAllGuildDatastores() {
        return guildDatastores.entrySet();
    }
}
