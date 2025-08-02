package org.hinoob.localbot.command.impl;

import io.github.ollama4j.models.chat.OllamaChatMessage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.hinoob.localbot.ChatbotData;
import org.hinoob.localbot.command.Command;
import org.hinoob.localbot.datastore.UserDatastore;

import java.util.List;

public class HistoryCommand extends Command {

    public HistoryCommand() {
        super("history");
    }

    @Override
    public void handle(MessageReceivedEvent event, String[] args, UserDatastore userDatastore) {
        List<OllamaChatMessage> history = ChatbotData.chatHistory.get(event.getAuthor().getId());
        if (history == null || history.isEmpty()) {
            event.getChannel().sendMessage("❗ No chat history found.").queue();
            return;
        }
        StringBuilder historyMessage = new StringBuilder("📝 Chat History:\n");
        for (OllamaChatMessage msg : history) {
            historyMessage.append("`" + msg.getRole().getRoleName().toUpperCase()).append("`: ```").append(msg.getContent()).append("```\n");
        }
        event.getChannel().sendMessage(historyMessage.toString()).queue();
    }

    @Override
    public boolean isAdminCommand() {
        return false;
    }
}
