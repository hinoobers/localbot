package org.hinoob.localbot.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.hinoob.localbot.LocalBot;
import org.hinoob.localbot.datastore.UserDatastore;

import java.util.*;

public class CommandHandler {

    private final List<Command> commands = new ArrayList<>();
    private final Map<String, Long> commandHelpCooldowns = new HashMap<>();

    public void registerCommand(Command command) {
        commands.add(command);
    }

    public void handleMessage(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        String[] args = message.split(" ");
        String userId = event.getAuthor().getId();
        String prefix = LocalBot.getInstance().getDatastoreHandler().getUserDatastore(userId).getString("prefix").orElse(",");
        if (args.length == 0) return;
        if(!args[0].startsWith(prefix)) return;

        UserDatastore userDatastore = LocalBot.getInstance().getDatastoreHandler().getUserDatastore(userId);

        String commandName = args[0].toLowerCase();
        boolean handled = false;
        for (Command command : commands) {
            if (commandName.equalsIgnoreCase(prefix + command.getName()) || command.getAliases().stream().anyMatch(alias -> commandName.equalsIgnoreCase(prefix + alias))) {
                command.handle(event, args, userDatastore);
                handled = true;
                break;
            }
        }

        if(!handled && message.length() > 3) {
            long lastHelped = commandHelpCooldowns.getOrDefault(userId, 0L);
            if(System.currentTimeMillis() - lastHelped > 60 * 1000L) { // 10 seconds cooldown
                commandHelpCooldowns.put(userId, System.currentTimeMillis());
                event.getChannel().sendMessage("❓ Unknown command! Use `" + prefix + "help` to see the list of commands.").queue();
            }
        }
    }

    public Collection<Command> getCommands() {
        return Collections.unmodifiableList(commands);
    }
}
