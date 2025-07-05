package org.hinoob.localbot.util;

import org.jetbrains.annotations.Nullable;

public class OmdbAPI {

    private static final String API_KEY = FileUtil.readFileJson("secret.json")
            .get("omdb_api_key").getAsString();

    public static JSONResponse get(String imdb, @Nullable Integer season) {
        return FileUtil.read("https://www.omdbapi.com/?i="+imdb+"&apikey="+API_KEY+ (season == null ? "" : "&Season="+season));
    }

}
