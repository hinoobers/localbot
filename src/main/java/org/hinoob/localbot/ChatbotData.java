package org.hinoob.localbot;

import io.github.ollama4j.models.chat.OllamaChatMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatbotData {

    public static final Map<String, List<OllamaChatMessage>> chatHistory = new HashMap<>();
    public static final Set<String> VALID_MODELS = Set.of("llama3.2:3b", "gemma3:4b");
    public static final int HISTORY_LIMIT = 4;
    public static final int SEARCH_TEXT_LIMIT = 512;

    public static final Set<String> QUESTION_WORDS = Set.of(
            "who", "what", "where", "why", "when", "how", "which"
    );

    public static final Set<String> AUXILIARY_VERBS = Set.of(
            "is", "are", "am", "was", "were",
            "do", "does", "did",
            "can", "could",
            "will", "would",
            "shall", "should",
            "has", "have", "had",
            "may", "might", "must"
    );
}
