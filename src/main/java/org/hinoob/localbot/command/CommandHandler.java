package org.hinoob.localbot.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.hinoob.localbot.LocalBot;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler {

    private final List<Command> commands = new ArrayList<>();

    public void registerCommand(Command command) {
        commands.add(command);
    }

    public void handleMessage(MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split(" ");
        String userId = event.getAuthor().getId();
        String prefix = LocalBot.getInstance().getDatastoreHandler().getUserDatastore(userId).getString("prefix").orElse(",");
        if (args.length == 0) return;

        String commandName = args[0].toLowerCase();
        for (Command command : commands) {
            if (commandName.equalsIgnoreCase(prefix + command.getName())) {
                command.handle(event, args);
                return;
            }
        }
    }

}
