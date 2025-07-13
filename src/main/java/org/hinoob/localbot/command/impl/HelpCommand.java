package org.hinoob.localbot.command.impl;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.hinoob.localbot.LocalBot;
import org.hinoob.localbot.command.Command;
import org.hinoob.localbot.datastore.UserDatastore;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help");
    }

    @Override
    public void handle(MessageReceivedEvent event, String[] args, UserDatastore userDatastore) {
        event.getChannel().sendMessage("Listing all commands:").queue();
        for(Command command : LocalBot.getInstance().getCommandHandler().getCommands()) {
            event.getChannel().sendMessage("`" + LocalBot.getInstance().getDatastoreHandler().getUserDatastore(event.getAuthor().getId()).getString("prefix").orElse(",") + command.getName() + "`").queue();
        }
    }
}
