package org.hinoob.localbot.command.impl;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.hinoob.localbot.ChatbotData;
import org.hinoob.localbot.LocalBot;
import org.hinoob.localbot.command.Command;
import org.hinoob.localbot.datastore.UserDatastore;

public class SetModelCommand extends Command {

    public SetModelCommand() {
        super("setmodel");
    }

    @Override
    public void handle(MessageReceivedEvent event, String[] args, UserDatastore userDatastore) {
        if (args.length < 2) {
            event.getChannel().sendMessage("❗ Please specify a model name.").queue();
            return;
        }

        String modelName = args[1];
        if(ChatbotData.VALID_MODELS.stream().anyMatch(s -> s.equalsIgnoreCase(modelName))) {
            userDatastore.setString("model", modelName);
            event.getChannel().sendMessage("✅ Model set to: " + modelName).queue();
        } else {
            event.getChannel().sendMessage("❗ Invalid model name.").queue();
            event.getChannel().sendMessage("Available models: " + String.join(", ", ChatbotData.VALID_MODELS)).queue();
        }
    }

    @Override
    public boolean isAdminCommand() {
        return false;
    }
}
