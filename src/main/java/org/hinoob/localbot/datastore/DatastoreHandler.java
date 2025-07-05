package org.hinoob.localbot.datastore;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DatastoreHandler {

    private GlobalDatastore globalDatastore = new GlobalDatastore();
    private Map<String, UserDatastore> userDatastores = new HashMap<>();

    public DatastoreHandler() {
        globalDatastore.load();
    }

    public GlobalDatastore getGlobalDatastore() {
        return globalDatastore;
    }

    public UserDatastore getUserDatastore(String userId) {
        return userDatastores.computeIfAbsent(userId, UserDatastore::new);
    }

    public Set<Map.Entry<String, UserDatastore>> getAllUserDatastores() {
        return userDatastores.entrySet();
    }
}
