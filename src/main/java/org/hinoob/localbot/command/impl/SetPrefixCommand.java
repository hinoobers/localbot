package org.hinoob.localbot.command.impl;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.hinoob.localbot.LocalBot;
import org.hinoob.localbot.command.Command;

public class SetPrefixCommand extends Command {

    public SetPrefixCommand() {
        super("setprefix");
    }

    @Override
    public void handle(MessageReceivedEvent event, String[] args) {
        if (args.length < 2) {
            event.getChannel().sendMessage("Usage: !setprefix <new_prefix>").queue();
            return;
        }

        if(!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            event.getChannel().sendMessage("❗ You do not have permission to change the prefix.").queue();
            return;
        }

        String newPrefix = args[1];
        if(newPrefix.isBlank() || newPrefix.length() > 5) {
            event.getChannel().sendMessage("❗ Prefix must be between 1 and 5 characters.").queue();
            return;
        }

        LocalBot.getInstance().getDatastoreHandler().getUserDatastore(event.getMember().getId()).setString("prefix", newPrefix);
        event.getChannel().sendMessage("Prefix set to: " + newPrefix).queue();
    }
}
