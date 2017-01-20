package ch.botfabrik.nlp.word2vec;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**Test some logic in TransformWikiText
 * Created by roman on 17.01.17.
 */
public class TransformWikiTextTest {

    @Test
    public void testSplitSentences1() {
        String splitted = TransformWikiText.splitSentences(TransformWikiText.normalizeText("Das Pseudonym entstand 1968 infolge der Arbeiten am Western-Film ''Death of a Gunfighter'' (deutscher Titel ''[[Frank Patch – Deine Stunden sind gezählt]]''). Regisseur [[Robert Totten]] und Hauptdarsteller [[Richard Widmark]] gerieten in einen Streit, woraufhin [[Don Siegel]] als neuer Regisseur eingesetzt wurde."));
        assertThat(splitted, is("Das Pseudonym entstand 1968 infolge der Arbeiten am Western-Film Death of a Gunfighter deutscher Titel Frank Patch Deine Stunden sind gezählt .\n" +
                "Regisseur Robert Totten und Hauptdarsteller Richard Widmark gerieten in einen Streit, woraufhin Don Siegel als neuer Regisseur eingesetzt wurde."));
    }
}
