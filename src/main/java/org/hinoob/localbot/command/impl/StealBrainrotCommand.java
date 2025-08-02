package org.hinoob.localbot.command.impl;

import com.google.gson.JsonPrimitive;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.hinoob.localbot.LocalBot;
import org.hinoob.localbot.command.Command;
import org.hinoob.localbot.datastore.UserDatastore;

public class StealBrainrotCommand extends Command {
    public StealBrainrotCommand() {
        super("stealbrainrot");
    }

    @Override
    public void handle(MessageReceivedEvent event, String[] args, UserDatastore userDatastore) {
        if(args.length == 1) {
            int likes = LocalBot.getInstance().getStealABrainrot().getLikes();
            int goal = LocalBot.getInstance().getStealABrainrot().getCurrentGoal();

            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Steal a brainrot")
                    .addField("Likes", String.valueOf(likes), true)
                    .addField("Goal", String.valueOf(goal), true)
                    .setFooter("Use `,stealbrainrot notifier` to toggle the notifier.")
                    .build();
            event.getMessage().getChannel().sendMessageEmbeds(embed).queue();
            return;
        }

        if(args.length > 1 && args[1].equalsIgnoreCase("notifier")) {
            if(userDatastore.contains("steal_brainrot_notifier")) {
                userDatastore.delete("steal_brainrot_notifier");
                event.getMessage().getChannel().sendMessage("🧠 Steal Brainrot notifier disabled!").queue();
            } else {
                userDatastore.set("steal_brainrot_notifier", new JsonPrimitive(true));
                event.getMessage().getChannel().sendMessage("🧠 Steal Brainrot notifier enabled!").queue();
            }
        }
    }

    @Override
    public boolean isAdminCommand() {
        return false;
    }
}
