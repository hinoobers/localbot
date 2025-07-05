package org.hinoob.localbot.command.impl;

import com.google.gson.JsonPrimitive;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.hinoob.localbot.LocalBot;
import org.hinoob.localbot.command.Command;

public class AutoTranslateCommand extends Command {
    public AutoTranslateCommand() {
        super("autotranslate");
    }

    @Override
    public void handle(MessageReceivedEvent event, String[] args) {
        if(args.length == 2) {
            String action = args[1].toLowerCase();
            if(action.equalsIgnoreCase("on")) {
                event.getChannel().sendMessage("🔄 Auto-translation enabled!").queue();
                LocalBot.getInstance().getDatastoreHandler().getUserDatastore(event.getAuthor().getId())
                        .set("auto_translate", new JsonPrimitive(true));
            } else if(action.equalsIgnoreCase("off")) {
                event.getChannel().sendMessage("🔄 Auto-translation disabled!").queue();
                LocalBot.getInstance().getDatastoreHandler().getUserDatastore(event.getAuthor().getId())
                        .set("auto_translate", new JsonPrimitive(false));
            } else {
                event.getChannel().sendMessage("❗ Invalid argument. Use 'on' or 'off'.").queue();
            }
        }
    }
}
