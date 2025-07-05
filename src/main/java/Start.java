import org.hinoob.localbot.LocalBot;

public class Start {

    public static void main(String[] args) {
        LocalBot bot = new LocalBot();
        LocalBot.setInstance(bot);

        bot.start();
    }
}
