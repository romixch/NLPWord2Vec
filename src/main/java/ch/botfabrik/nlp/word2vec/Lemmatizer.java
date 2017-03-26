package ch.botfabrik.nlp.word2vec;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.GermanyGerman;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by roman on 06.02.17.
 */
public class Lemmatizer {

    private JLanguageTool languageTool = new JLanguageTool(new GermanyGerman());

    String apply(String text) throws IOException {
        StringBuilder sb = new StringBuilder();
        List<AnalyzedSentence> sentences = languageTool.analyzeText(text);
        for (AnalyzedSentence sentence : sentences) {
            for (AnalyzedTokenReadings readings : sentence.getTokensWithoutWhitespace()) {
                if (readings.getReadings().size() > 0) {
                    String word = readings.getToken();
                    for (AnalyzedToken reading : readings) {
                        String lemma = reading.getLemma();
                        if (lemma != null) {
                            word = lemma;
                            break;
                        }
                    }
                    if (!word.isEmpty() && !word.equals(".") && !word.equals(",") && !word.equals(":") && !word.equals("“") && !word.equals("„")) {
                        sb.append(word).append(' ');
                    }
                }
            }
            sb.append(".\n");
        }
        return sb.toString();
    }

    List<String> applyLeavingPlusAndMinusIntact(String text) throws IOException {
        List<String> words = new ArrayList<>();
        List<AnalyzedSentence> sentences = languageTool.analyzeText(text);
        for (AnalyzedSentence sentence : sentences) {
            for (AnalyzedTokenReadings readings : sentence.getTokensWithoutWhitespace()) {
                if (readings.getReadings().size() > 0) {
                    String word = readings.getToken();
                    if (!isPlusOrMinus(word)) {
                        for (AnalyzedToken reading : readings) {
                            String lemma = reading.getLemma();
                            if (lemma != null) {
                                word = lemma;
                                break;
                            }
                        }
                    }

                    if (!word.isEmpty()) {
                        words.add(word);
                    }
                }
            }
        }
        return words;
    }

    private boolean isPlusOrMinus(String word) {
        return "+".equals(word) || "-".equals(word);
    }
}
