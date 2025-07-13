package org.hinoob.localbot.command.impl;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.hinoob.localbot.command.Command;
import org.hinoob.localbot.datastore.UserDatastore;

public class UptimeCommand extends Command {
    public UptimeCommand() {
        super("uptime");
    }

    public static final long startup = System.currentTimeMillis();

    @Override
    public void handle(MessageReceivedEvent event, String[] args, UserDatastore userDatastore) {
        long uptimeMillis = System.currentTimeMillis() - startup;
        long seconds = (uptimeMillis / 1000) % 60;
        long minutes = (uptimeMillis / (1000 * 60)) % 60;
        long hours = (uptimeMillis / (1000 * 60 * 60)) % 24;
        long days = uptimeMillis / (1000 * 60 * 60 * 24);

        String uptimeMessage = String.format("Uptime: %d days, %d hours, %d minutes, %d seconds", days, hours, minutes, seconds);
        event.getChannel().sendMessage(uptimeMessage).queue();
    }
}
