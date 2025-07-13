package org.hinoob.localbot.listener;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.chat.OllamaChatMessage;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.hinoob.localbot.ChatbotData;
import org.hinoob.localbot.LocalBot;
import org.hinoob.localbot.datastore.GuildDatastore;
import org.hinoob.localbot.datastore.UserDatastore;
import org.hinoob.localbot.util.GeoguessPicture;
import org.hinoob.localbot.util.TranslateAPI;

import java.awt.*;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MessageListener extends ListenerAdapter {


    @SubscribeEvent
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String userId = event.getAuthor().getId();
        String messageContent = event.getMessage().getContentRaw();
        String prefix = LocalBot.getInstance().getDatastoreHandler().getUserDatastore(userId).getString("prefix")
                .orElse(",");

        UserDatastore userDatastore = LocalBot.getInstance().getDatastoreHandler().getUserDatastore(userId);
        GuildDatastore guildDatastore = LocalBot.getInstance().getDatastoreHandler().getGuildDatastore(event.getGuild().getId());

        if (messageContent.startsWith(prefix)) {
            LocalBot.getInstance().getCommandHandler().handleMessage(event);
        } else if(event.getMessage().getMentions().isMentioned(LocalBot.getInstance().getJda().getSelfUser())) {
            messageContent = messageContent.replace("<@" + LocalBot.getInstance().getJda().getSelfUser().getId() + ">", "").trim();
            if (messageContent.isEmpty()) {
                event.getChannel().sendMessage("Your prefix is `" + prefix + "`.").queue();
                return;
            }

            List<OllamaChatMessage> history = ChatbotData.chatHistory.computeIfAbsent(userId, k -> new ArrayList<>());

            if(!history.isEmpty() && history.getLast().getRole() == OllamaChatMessageRole.USER) {
                event.getChannel().sendMessage("❗ Please wait for the assistant to respond before sending another message.").queue();
                event.getChannel().sendMessage("If you want to clear the memory, type `,clearmemory`.").queue();
                return;
            }

            if ((isQuestion(messageContent) && needsSearch(messageContent)) || messageContent.toLowerCase(Locale.ROOT).contains("search")) {
                fetchExternalInfoAndRespond(event, messageContent, history);
            } else {
                addUserMessageAndRespond(event, messageContent, history);
            }
        } else if(guildDatastore.contains("geoguess_channel") && event.getMessage().getChannelId().equals(guildDatastore.getString("geoguess_channel").orElse(""))) {
            // Handle GeoGuessr channel messages
            if(messageContent.isBlank() || messageContent.length() > 56 || messageContent.chars().boxed().collect(Collectors.toSet()).size() == 1) {
                event.getChannel().sendMessage("❗ Invalid guess! Please enter a valid country name.").queue(msg -> {
                    msg.delete().queueAfter(2, TimeUnit.SECONDS);
                    event.getMessage().delete().queue();
                });
                return;
            }

            GeoguessPicture pic = LocalBot.getInstance().getGeoguessGame().getPictureForGuild(event.getGuild().getId());
            if(pic == null || pic.isSwitching()) {
                event.getMessage().delete().queue();
                return;
            }

            if (messageContent.equalsIgnoreCase(pic.getCountry())) {
                pic.setSwitching(true);
                event.getChannel().sendMessage("🎉 Correct! The country was: " + pic.getCountry()).queue(msg -> {
                    msg.delete().queueAfter(2, TimeUnit.SECONDS, null, _ -> {});
                    event.getMessage().delete().queue(null, _ -> {});

                    LocalBot.getInstance().getScheduler().schedule(() -> {
                        LocalBot.getInstance().getGeoguessGame().resetPicture(event.getGuild().getId(), event.getChannel().getId());
                    }, 3, TimeUnit.SECONDS);
                });

            } else {
                event.getChannel().sendMessage("❌ Incorrect! Try again.").queue(msg -> {
                    msg.delete().queueAfter(2, TimeUnit.SECONDS);
                    event.getMessage().delete().queue(null, _ -> {});
                });
            }
        } else {
            if(!userDatastore.contains("auto_translate") || userDatastore.get("auto_translate").orElse(new JsonPrimitive(false)).getAsBoolean()) {
                // Only translate if they don't have the setting yet, or they enabled it
                String translation = TranslateAPI.translate(messageContent, "en");
                if(translation != null) {
                    EmbedBuilder embed = new EmbedBuilder()
                            .setColor(Color.CYAN)
                            .setTitle("🔄 Translated Message")
                            .addField("Original Message", messageContent, false)
                            .addField("Translation", translation, false)
                            .setFooter(TranslateAPI.translate("No longer want translations? Type `,autotranslate off` to disable them.", TranslateAPI.detectLanguage(messageContent)));

                    event.getMessage().replyEmbeds(embed.build()).queue();
                }
            }
        }
    }

    private void fetchExternalInfoAndRespond(MessageReceivedEvent event, String userMessage, List<OllamaChatMessage> history) {
        event.getChannel().sendMessage("🔍 Fetching extra info to improve response...").queue(message -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                String encodedQuery = URLEncoder.encode(userMessage, StandardCharsets.UTF_8);
                String apiUrl = "http://bs.byenoob.com:4082/search?q=" + encodedQuery;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    message.editMessage("❗ Search API returned status " + response.statusCode()).queue();
                    return;
                }

                JsonArray results = new Gson().fromJson(response.body(), JsonArray.class);
                if (results.isEmpty()) {
                    message.editMessage("❗ No results found from search API.").queue();
                    return;
                }

                String combinedText = resultsAsCombinedText(results);
                if (combinedText.isEmpty()) {
                    message.editMessage("❗ No text content found in search results.").queue();
                    return;
                }

                // Add system message with search context and user's message to history
                history.add(new OllamaChatMessage(OllamaChatMessageRole.SYSTEM, "Information (via Search Engine): " + combinedText));
                history.add(new OllamaChatMessage(OllamaChatMessageRole.USER, userMessage));
                trimHistory(history);

                event.getChannel().sendMessage("Link: " + apiUrl).queue();
                message.editMessage("✅ Added external info to context. Responding...").queue();

                sendOllamaChat(event, history);

            } catch (Exception e) {
                message.editMessage("❗ Error calling search API: " + e.getMessage()).queue();
                e.printStackTrace();
            }
        });
    }

    private boolean needsSearch(String msg) {
        String lower = msg.toLowerCase(Locale.ROOT);
        return lower.contains("news") ||
                lower.contains("latest") ||
                lower.contains("current") ||
                lower.contains("today") ||
                lower.contains("who won") ||
                lower.contains("when is") ||
                lower.contains("who is") ||
                lower.contains("time") ||
                lower.contains("what is") ||
                lower.contains("what are") ||
                lower.contains("release date") ||
                lower.contains("price of") ||
                lower.contains("how to") && !lower.contains("plugin") && !lower.contains("code");
    }

    private String resultsAsCombinedText(JsonArray results) {
        StringBuilder combinedText = new StringBuilder();
        int limit = Math.min(2, results.size());
        for (int i = 0; i < limit; i++) {
            JsonObject item = results.get(i).getAsJsonObject();
            if (item.has("text") && !item.get("text").getAsString().isBlank()) {
                String text = item.get("text").getAsString();
                if (text.length() > ChatbotData.SEARCH_TEXT_LIMIT) text = text.substring(0, ChatbotData.SEARCH_TEXT_LIMIT);
                combinedText.append(text).append("\n\n");
            }
        }

        System.out.println("Combined search text: " + combinedText.toString());
        return combinedText.toString().trim();
    }

    private void addUserMessageAndRespond(MessageReceivedEvent event, String userMessage, List<OllamaChatMessage> history) {
        history.add(new OllamaChatMessage(OllamaChatMessageRole.USER, userMessage));
        trimHistory(history);
        sendOllamaChat(event, history);
    }

    private void trimHistory(List<OllamaChatMessage> history) {
        if (history.size() > ChatbotData.HISTORY_LIMIT) {
            history.subList(0, history.size() - ChatbotData.HISTORY_LIMIT).clear();
        }
    }

    private boolean isQuestion(String msg) {
        String trimmed = msg.trim().toLowerCase(Locale.ROOT);
        if (trimmed.isEmpty()) return false;

        if (trimmed.endsWith("?")) {
            return true;
        }

        String[] words = trimmed.split("\\s+");
        if (words.length == 0) return false;

        String firstWord = words[0];

        if (ChatbotData.QUESTION_WORDS.contains(firstWord)) {
            return true;
        }

        if (ChatbotData.AUXILIARY_VERBS.contains(firstWord)) {
            if (words.length > 1) {
                String secondWord = words[1];
                Set<String> pronouns = Set.of("i", "you", "he", "she", "we", "they", "it");
                return pronouns.contains(secondWord) || Character.isUpperCase(secondWord.charAt(0));
            }
        }

        return false;
    }

    private void sendOllamaChat(MessageReceivedEvent event, List<OllamaChatMessage> history) {
        OllamaAPI api = LocalBot.getInstance().getOllamaAPI();
        OllamaChatRequest request = new OllamaChatRequest();
        request.setModel(LocalBot.getInstance().getDatastoreHandler().getUserDatastore(event.getAuthor().getId()).getString("model").orElse("gemma3:4b"));
        request.setMessages(history);
        request.setStream(true);

        event.getChannel().sendMessage("💬 Thinking...").queue(message -> {
            StringBuilder fullResponse = new StringBuilder();
            final long[] lastEditTime = {System.currentTimeMillis()};

            try {
                api.chatStreaming(request, streamMsg -> {
                    if (streamMsg.getMessage() == null) return;

                    String content = streamMsg.getMessage().getContent();
                    if (content == null || content.isBlank()) return;

                    fullResponse.append(content);

                    long now = System.currentTimeMillis();
                    if (now - lastEditTime[0] >= 1500) {
                        lastEditTime[0] = now;
                        message.editMessage(truncateWithRedact(fullResponse.toString())).queue();
                    }
                });

                // Final edit after streaming ends
                if (fullResponse.isEmpty()) {
                    message.editMessage("❗ No response generated from the model.").queue();
                } else {
                    message.editMessage(truncateWithRedact(fullResponse.toString())).queue();
                    // Add assistant reply to history

                    System.out.println("Added assistant reply to history: " + fullResponse.toString());
                }
            } catch (Exception e) {
                message.editMessage("❗ Error: " + e.getMessage()).queue();
                e.printStackTrace();
            }
        });
    }

    private String truncateWithRedact(String text) {
        int maxLength = 2000;
        String suffix = " (❗ redacted)";
        if (text.length() <= maxLength) {
            return text;
        } else {
            // Cut so that total length including suffix <= maxLength
            int cutLength = maxLength - suffix.length();
            if (cutLength < 0) cutLength = 0; // just in case
            return text.substring(0, cutLength) + suffix;
        }
    }
}
