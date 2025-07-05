package org.hinoob.localbot.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class TranslateAPI {

    private static final String API_KEY = FileUtil.readFileJson("secret.json")
            .get("deepl_api_key").getAsString();

    public static String translate(String text, String targetLanguage) {
        if(text.isBlank() || text.length() < 2 || text.length() > 2000) {
            return null;
        }
        // DeepL

        // target
        String apiUrl = "https://api-free.deepl.com/v2/translate?auth_key="+API_KEY+"&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8) + "&target_lang=" + targetLanguage;
        JSONResponse response = FileUtil.read(apiUrl);
        if(response.isRateLimited()) {
            return null;
        }

        if (!response.has("translations")) {
            return null;
        }

        String returning = response.getAsJsonArray("translations")
                .get(0)
                .getAsJsonObject()
                .get("text")
                .getAsString();
        if(returning.equals(text)) {
            return null; // no translation, what a scam
        }

        return returning;
    }

    public static String detectLanguage(String text) {
        if(text.isBlank() || text.length() < 2 || text.length() > 2000) {
            return null;
        }
        // DeepL

        // target
        String apiUrl = "https://api-free.deepl.com/v2/translate?auth_key="+API_KEY+"&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8) + "&target_lang=en";
        JSONResponse response = FileUtil.read(apiUrl);
        if(response.isRateLimited()) {
            return null;
        }

        if (!response.has("translations")) {
            return null;
        }

        return response.getAsJsonArray("translations")
                .get(0)
                .getAsJsonObject()
                .get("detected_source_language")
                .getAsString();
    }
}
