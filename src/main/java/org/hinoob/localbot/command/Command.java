package org.hinoob.localbot.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Command {

    private String name;

    public Command(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract void handle(MessageReceivedEvent event, String[] args);

}
