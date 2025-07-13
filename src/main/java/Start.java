
import org.hinoob.localbot.LocalBot;

import java.io.IOException;
import java.util.List;

public class Start {

    public static void main(String[] args) throws IOException {
        LocalBot bot = new LocalBot();
        LocalBot.setInstance(bot);

        bot.start();
    }

}
