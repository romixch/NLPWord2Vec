package ch.botfabrik.nlp.word2vec;

import java.util.*;

/**
 * Created by roman on 19.01.17.
 */
public class GuessedIntention {

    TreeSet<Guess> guesses = new TreeSet<>();

    public void addIntention(String intention, double ranking) {
        guesses.add(new Guess(intention, ranking));
    }

    public SortedSet<Guess> getSortedGuesses() {
        return Collections.unmodifiableSortedSet(guesses);
    }

    @Override
    public String toString() {
        return guesses.toString();
    }

    public static class Guess implements Comparable<Guess> {
        private final String intention;
        private final double ranking;
        private Guess(String intention, double ranking) {
            this.intention = intention;
            this.ranking = ranking;
        }

        public String getIntention() {
            return intention;
        }

        public double getRanking() {
            return ranking;
        }

        @Override
        public String toString() {
            return intention + ": " + ranking;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof  Guess)) {
                return false;
            }
            Guess that = (Guess) obj;
            return ranking == that.ranking && intention.equals(that.intention);
        }

        @Override
        public int hashCode() {
            return (int)(ranking * 1000d) + intention.hashCode();
        }

        @Override
        public int compareTo(Guess other) {
            double r = other.ranking - this.ranking;
            if (r != 0d) {
                return (int)(r * 1000d);
            } else {
                return other.intention.compareTo(this.intention);
            }
        }
    }
}
