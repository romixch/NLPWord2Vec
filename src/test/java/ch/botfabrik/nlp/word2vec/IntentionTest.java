package ch.botfabrik.nlp.word2vec;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by roman on 19.01.17.
 */
public class IntentionTest {

    private static Logger LOGGER = LoggerFactory.getLogger(IntentionTest.class);
    private static ServerMain serverMain;
    private Lemmatizer lemmatizer = new Lemmatizer();

    @BeforeClass
    public static void loadModel() throws IOException {
        serverMain = new ServerMain();
        serverMain.load(new File("dewiki-latest-pages-articles1.txt.model"));
    }

    @Test
    public void testEnglish() throws IOException {
        assertIntention("Kannst du auch englisch?", "sprache");
    }

    @Test
    public void testBefinden() throws IOException {
        assertIntention("Wie geht's dir?", "befinden");
    }

    @Test
    public void testInteresse() throws IOException {
        assertIntention("Ich habe Interesse an einem Bot", "interesse");
    }

    @Test
    public void testJa() throws IOException {
        assertIntention("ja", "ja");
    }

    @Test
    public void testSicher() throws IOException {
        assertIntention("sicher", "ja");
    }

    @Test
    public void testNein() throws IOException {
        assertIntention("nein", "nein");
    }

    @Test
    public void testLieberNicht() throws IOException {
        assertIntention("lieber nicht", "nein");
    }

    @Test
    public void testBeispiele() throws IOException {
        assertIntention("Zeig mir Bot Beispiele", "beispiel");
    }

    @Test
    public void testReferenzen() throws IOException {
        assertIntention("Habt ihr Referenzen?", "beispiel");
    }

    @Test
    public void testKosten() throws IOException {
        assertIntention("wieviel würden denn so ein paar bots für instagram oder youtube kosten?", "kosten");
    }

    @Test
    public void testDokumentation() throws IOException {
        assertIntention("Hallo Pit. Wie kann ein Bot wie du auf einer Webseite eingebaut werden? Gibt es hierfür eine technische Dokumentation? ", "technologie");
    }

    private void assertIntention(String question, String intention) throws IOException {
        LOGGER.info("Testing question: " + question);
        List<String> words = Arrays.stream(lemmatizer.apply(question).split(" "))
                .filter(s -> !s.contains("."))
                .collect(Collectors.toList());
        LOGGER.info("Question lemmatized: " + words);
        GuessedIntention guesses = serverMain.guessIntention(words);
        LOGGER.info("'" + question + "' : " + guesses);
        assertThat(guesses.getSortedGuesses().first().getIntention(), is(intention));
    }
}
