package org.hinoob.localbot;

import io.github.ollama4j.OllamaAPI;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.hinoob.localbot.command.CommandHandler;
import org.hinoob.localbot.command.impl.*;
import org.hinoob.localbot.datastore.DatastoreHandler;
import org.hinoob.localbot.listener.MessageListener;
import org.hinoob.localbot.tickable.*;
import org.hinoob.localbot.util.ActivityTracker;
import org.hinoob.localbot.util.FileUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@Slf4j
@Getter
public class LocalBot {

    @Getter @Setter private static LocalBot instance;
    @Getter private static Logger logger = Logger.getLogger("LocalBot");

    private JDA jda;
    private OllamaAPI ollamaAPI;
    private DatastoreHandler datastoreHandler = new DatastoreHandler();
    private CommandHandler commandHandler = new CommandHandler();

    private final List<Tickable> tickables = new ArrayList<>();
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private GeoguessGame geoguessGame;
    private ActivityTracker activityTracker;
    private LightningTracker lightningTracker;
    private StealABrainrot stealABrainrot;

    public void start() {
        if(new File("secret.json").exists()) {
            logger.info("Starting LocalBot...");
        } else {
            logger.severe("secret.json file not found! Please create a file named 'secret.json' with your bot token.");
            return;
        }

        Thread.setDefaultUncaughtExceptionHandler((_, throwable) -> {
            logCrash("Uncaught Exception", throwable);
        });

        this.jda = JDABuilder.createDefault(FileUtil.readFileJson("secret.json").get("bot_token").getAsString())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT,
                               GatewayIntent.GUILD_MESSAGES,
                               GatewayIntent.GUILD_MEMBERS,
                               GatewayIntent.GUILD_PRESENCES,
                               GatewayIntent.DIRECT_MESSAGES)
                .addEventListeners(new MessageListener())
                .build();

        tickables.addAll(Arrays.asList(stealABrainrot = new StealABrainrot(jda), new IMDBRemind(jda), geoguessGame = new GeoguessGame(jda), activityTracker = new ActivityTracker(jda), lightningTracker = new LightningTracker(jda)));

        commandHandler.registerCommand(new ClearMemoryCommand());
        commandHandler.registerCommand(new HistoryCommand());
        commandHandler.registerCommand(new SetModelCommand());
        commandHandler.registerCommand(new SetPrefixCommand());
        commandHandler.registerCommand(new StealBrainrotCommand());
        commandHandler.registerCommand(new IMDBRemindCommand());
        commandHandler.registerCommand(new UptimeCommand());
        commandHandler.registerCommand(new AutoTranslateCommand());
        commandHandler.registerCommand(new GeoguessCommand());
        commandHandler.registerCommand(new HelpCommand());

        this.ollamaAPI = new OllamaAPI("http://bs.byenoob.com:32771");
        ollamaAPI.setRequestTimeoutSeconds(30);

        scheduler.scheduleAtFixedRate(() -> {
            if (jda.getStatus() != JDA.Status.CONNECTED) {
                return;
            }

            try {
                for (Tickable tickable : tickables) {
                    tickable.tick();
                }
            } catch (Throwable t) {
                logCrash("Datastore Load Error", t);
            }
        }, 0, 1, TimeUnit.MILLISECONDS);

        try {
            this.jda.awaitReady();

            tickables.forEach(Tickable::onStartup);
        } catch (InterruptedException e) {
            logCrash("JDA Startup Error", e);
        }
    }

    public static void logCrash(String context, Throwable throwable) {
        try (PrintWriter out = new PrintWriter(new FileWriter("crash.log", true))) {
            out.println("==== CRASH @ " + java.time.LocalDateTime.now() + " ====");
            out.println("Context: " + context);
            throwable.printStackTrace(out);
            out.println("====================================");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
