package org.hinoob.localbot;

import io.github.ollama4j.OllamaAPI;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.hinoob.localbot.command.CommandHandler;
import org.hinoob.localbot.command.impl.*;
import org.hinoob.localbot.datastore.DatastoreHandler;
import org.hinoob.localbot.listener.MessageListener;
import org.hinoob.localbot.tickable.GeoguessGame;
import org.hinoob.localbot.tickable.IMDBRemind;
import org.hinoob.localbot.tickable.StealABrainrot;
import org.hinoob.localbot.tickable.Tickable;
import org.hinoob.localbot.util.FileUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Getter
public class LocalBot {

    @Getter @Setter private static LocalBot instance;
    @Getter private static Logger logger = Logger.getLogger("LocalBot");

    private JDA jda;
    private OllamaAPI ollamaAPI;
    private DatastoreHandler datastoreHandler = new DatastoreHandler();
    private CommandHandler commandHandler = new CommandHandler();

    private final List<Tickable> tickables = new ArrayList<>();

    private GeoguessGame geoguessGame;

    public void start() {
        if(new File("secret.json").exists()) {
            logger.info("Starting LocalBot...");
        } else {
            logger.severe("secret.json file not found! Please create a file named 'secret.json' with your bot token.");
            return;
        }

        Thread.setDefaultUncaughtExceptionHandler((_, throwable) -> {
            try (PrintWriter out = new PrintWriter(new FileWriter("crash.log", true))) {
                out.println("==== CRASH @ " + java.time.LocalDateTime.now() + " ====");
                throwable.printStackTrace(out);
                out.println("====================================");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        this.jda = JDABuilder.createDefault(FileUtil.readFileJson("secret.json").get("bot_token").getAsString())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT,
                               GatewayIntent.GUILD_MESSAGES,
                               GatewayIntent.GUILD_MEMBERS,
                               GatewayIntent.GUILD_PRESENCES,
                               GatewayIntent.DIRECT_MESSAGES)
                .addEventListeners(new MessageListener())
                .build();

        tickables.addAll(Arrays.asList(new StealABrainrot(jda), new IMDBRemind(jda), geoguessGame = new GeoguessGame(jda)));

        commandHandler.registerCommand(new ClearMemoryCommand());
        commandHandler.registerCommand(new HistoryCommand());
        commandHandler.registerCommand(new SetModelCommand());
        commandHandler.registerCommand(new SetPrefixCommand());
        commandHandler.registerCommand(new StealBrainrotCommand());
        commandHandler.registerCommand(new IMDBRemindCommand());
        commandHandler.registerCommand(new UptimeCommand());
        commandHandler.registerCommand(new AutoTranslateCommand());
        commandHandler.registerCommand(new GeoguessCommand());

        this.ollamaAPI = new OllamaAPI("http://bs.byenoob.com:32771");
        ollamaAPI.setRequestTimeoutSeconds(30);

        new Thread(() -> {
            while(true) {
                try {
                    if (jda.getStatus() != JDA.Status.CONNECTED) {
                        Thread.sleep(1000L);
                        continue;
                    }

                    for (Tickable tickable : tickables) {
                        tickable.tick();
                    }
                    Thread.sleep(1L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        try {
            this.jda.awaitReady();

            tickables.forEach(Tickable::onStartup);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
