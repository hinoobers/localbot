package org.hinoob.localbot.command.impl;

import com.google.gson.JsonPrimitive;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.hinoob.localbot.command.Command;
import org.hinoob.localbot.datastore.UserDatastore;

public class StealBrainrotCommand extends Command {
    public StealBrainrotCommand() {
        super("stealbrainrot");
    }

    @Override
    public void handle(MessageReceivedEvent event, String[] args, UserDatastore userDatastore) {
        if(userDatastore.contains("steal_brainrot_notifier")) {
            userDatastore.delete("steal_brainrot_notifier");
            event.getMessage().getChannel().sendMessage("🧠 Steal Brainrot notifier disabled!").queue();
        } else {
            userDatastore.set("steal_brainrot_notifier", new JsonPrimitive(true));
            event.getMessage().getChannel().sendMessage("🧠 Steal Brainrot notifier enabled!").queue();
        }
    }
}
