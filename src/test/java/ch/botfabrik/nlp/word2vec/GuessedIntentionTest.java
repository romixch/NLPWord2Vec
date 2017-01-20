package ch.botfabrik.nlp.word2vec;

import org.junit.Test;

import java.util.Iterator;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Created by roman on 20.01.17.
 */
public class GuessedIntentionTest {

    @Test
    public void testSort() {
        GuessedIntention guesses = new GuessedIntention();
        guesses.addIntention("int1", 0.83235d);
        guesses.addIntention("int2", 0.45d);
        guesses.addIntention("int0", 0.9235d);
        Iterator<GuessedIntention.Guess> it = guesses.getSortedGuesses().iterator();
        assertThat(it.next().getIntention(), is("int0"));
        assertThat(it.next().getIntention(), is("int1"));
        assertThat(it.next().getIntention(), is("int2"));
    }

    @Test
    public void testSortIfRankingIsSame() {
        GuessedIntention guesses = new GuessedIntention();
        guesses.addIntention("int1", 1);
        guesses.addIntention("int2", 1);
        guesses.addIntention("int0", 1);
        Iterator<GuessedIntention.Guess> it = guesses.getSortedGuesses().iterator();
        assertThat(it.next().getIntention(), is("int0"));
        assertThat(it.next().getIntention(), is("int1"));
        assertThat(it.next().getIntention(), is("int2"));
    }

}