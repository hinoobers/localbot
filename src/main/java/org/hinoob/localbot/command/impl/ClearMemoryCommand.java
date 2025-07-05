package org.hinoob.localbot.command.impl;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.hinoob.localbot.ChatbotData;
import org.hinoob.localbot.LocalBot;
import org.hinoob.localbot.command.Command;
import org.hinoob.localbot.datastore.UserDatastore;

public class ClearMemoryCommand extends Command {
    public ClearMemoryCommand() {
        super("clearmemory");
    }

    @Override
    public void handle(MessageReceivedEvent event, String[] args, UserDatastore userDatastore) {
        ChatbotData.chatHistory.remove(event.getAuthor().getId());
        event.getChannel().sendMessage("🧠 Memory cleared!").queue();
    }
}
