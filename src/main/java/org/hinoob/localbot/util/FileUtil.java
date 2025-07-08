package org.hinoob.localbot.util;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class FileUtil {

    public static String readFileString(String filePath) {
        try {
            return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath)));
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JsonObject readFileJson(String filePath) {
        try {
            String content = readFileString(filePath);
            if (content != null) {
                return com.google.gson.JsonParser.parseString(content).getAsJsonObject();
            } else {
                return new JsonObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @NotNull
    public static JSONResponse read(String url) {
        try {
            URL apiUrl = new URI(url).toURL();
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            if (connection.getResponseCode() != 200) {
                return new JSONResponse(false);
            }

            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                response.append(output);
            }
            connection.disconnect();

            return new JSONResponse(com.google.gson.JsonParser.parseString(response.toString()).getAsJsonObject());
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONResponse(false);
        }
    }
}
