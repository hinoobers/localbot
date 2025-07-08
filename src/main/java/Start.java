import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.inference.Predictor;
import ai.djl.modality.nlp.Vocabulary;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
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
