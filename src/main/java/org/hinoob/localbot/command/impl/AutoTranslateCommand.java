package org.hinoob.localbot.command.impl;

import com.google.gson.JsonPrimitive;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.hinoob.localbot.command.Command;
import org.hinoob.localbot.datastore.UserDatastore;

public class AutoTranslateCommand extends Command {
    public AutoTranslateCommand() {
        super("autotranslate");
    }

    @Override
    public void handle(MessageReceivedEvent event, String[] args, UserDatastore userDatastore) {
        if(args.length == 2) {
            String action = args[1].toLowerCase();
            if(action.equalsIgnoreCase("on")) {
                event.getChannel().sendMessage("🔄 Auto-translation enabled!").queue();
                userDatastore.set("auto_translate", new JsonPrimitive(true));
            } else if(action.equalsIgnoreCase("off")) {
                event.getChannel().sendMessage("🔄 Auto-translation disabled!").queue();
                userDatastore.set("auto_translate", new JsonPrimitive(false));
            } else {
                event.getChannel().sendMessage("❗ Invalid argument. Use 'on' or 'off'.").queue();
            }
        }
    }
}
