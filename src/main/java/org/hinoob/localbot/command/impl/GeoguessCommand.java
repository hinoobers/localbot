package org.hinoob.localbot.command.impl;

import com.google.gson.JsonPrimitive;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.hinoob.localbot.LocalBot;
import org.hinoob.localbot.command.Command;
import org.hinoob.localbot.datastore.GuildDatastore;
import org.hinoob.localbot.datastore.UserDatastore;

public class GeoguessCommand extends Command {
    public GeoguessCommand() {
        super("geog");
    }

    @Override
    public void handle(MessageReceivedEvent event, String[] args, UserDatastore userDatastore) {
        GuildDatastore guildDatastore = LocalBot.getInstance().getDatastoreHandler().getGuildDatastore(event.getGuild().getId());
        guildDatastore.set("geoguess_channel", new JsonPrimitive(event.getMessage().getChannel().getId()));
    }
}
