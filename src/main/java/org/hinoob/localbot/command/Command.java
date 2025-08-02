package org.hinoob.localbot.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.hinoob.localbot.datastore.UserDatastore;

import java.util.ArrayList;
import java.util.List;

public abstract class Command {

    private final String name;
    private final List<String> aliases = new ArrayList<>();

    public Command(String name, String... aliases) {
        this.name = name;
        for (String alias : aliases) {
            this.aliases.add(alias.toLowerCase());
        }
    }

    public String getName() {
        return name;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public abstract boolean isAdminCommand();

    public abstract void handle(MessageReceivedEvent event, String[] args, UserDatastore userDatastore);

}
