package ch.botfabrik.nlp.word2vec;


import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Cool stuff to learn a word2vec model and asking it some questions. Created by roman on 15.01.17.
 */
public class ServerMain {

    private static Logger logger;

    static {
        System.setProperty("java.util.logging.config.file", "./logging.properties");
        logger = LoggerFactory.getLogger(LearnMain.class);
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            throw new RuntimeException("You must define an input file as program parameter.");
        }
        ServerMain serverMain = new ServerMain();

        File modelFile = new File(args[0]);
        if (modelFile.exists()) {
            logger.info("Modell wird geladen...");
            serverMain.load(modelFile);
            serverMain.askCoolQuestions();
        } else {
            throw new RuntimeException("Model file " + modelFile.getAbsolutePath() + " does not exist.");
        }
    }

    private Word2Vec word2Vec;
    private Lemmatizer lemmatizer = new Lemmatizer();

    private void askCoolQuestions() throws IOException {
        logger.info("Ich bin bereit zum Rechnen mit Wörtern.");
        /*Collection<String> maybeFrau = word2Vec.wordsNearest(Arrays.asList("königin", "mann"), Arrays.asList("könig"), 10);
        logger.info("Given König -> Königin. What is Mann -> ? " + maybeFrau);
        Collection<String> maybeParis = word2Vec.wordsNearest(Arrays.asList("frankreich", "zürich"), Arrays.asList("schweiz"), 10);
        logger.info("Given Schweiz -> Zürich. What is Frankreich -> ? " + maybeParis);
        Collection<String> maybePutin = word2Vec.wordsNearest(Arrays.asList("russland", "obama"), Arrays.asList("usa"), 10);
        logger.info("Given USA -> Obama. What is Russland -> ? " + maybePutin);
        Collection<String> maybePutinAgain = word2Vec.wordsNearestSum(Arrays.asList("präsident", "russland"), Collections.emptyList(), 10);
        logger.info("Russland + Präsident" + maybePutinAgain);*/
        while (true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String input = br.readLine().toLowerCase();
            if (input.equals("exit")) {
                break;
            }
            List<String> words = lemmatizer.applyLeavingPlusAndMinusIntact(input).stream()
                    .filter(s -> !s.contains("."))
                    .collect(Collectors.toList());
            if (words.size() == 0)
                continue;
            String word1 = words.get(0);
            if (words.size() == 1) {
                if (word2Vec.hasWord(word1)) {
                    Collection<String> wordsNearest = word2Vec.wordsNearest(word1, 10);
                    logger.info("10 Wörter, die am nächsten zu '" + word1 + "' stehen: " + wordsNearest);
                    int indexOfWord = word2Vec.getVocab().indexOf(word1);
                    logger.info(word1 + " ist das " + indexOfWord + ". häufigste Wort.");
                } else {
                    logger.info("Ich kenne das Wort " + word1 + " nicht.");
                }
            } else if (words.size() == 2) {
                String word2 = words.get(1);
                if (word2Vec.hasWord(word1) && word2Vec.hasWord(word2)) {
                    double similarity = word2Vec.similarity(word1, word2);
                    logger.info("Ähnlichkeit: " + similarity);
                    int indexOfWord1 = word2Vec.getVocab().indexOf(word1);
                    logger.info(word1 + " ist das " + indexOfWord1 + ". häufigste Wort.");
                    int indexOfWord2 = word2Vec.getVocab().indexOf(word2);
                    logger.info(word2 + " ist das " + indexOfWord2 + ". häufigste Wort.");
                } else {
                    if (!word2Vec.hasWord(word1))
                        logger.info("Ich kenne das Wort " + word1 + " nicht.");
                    if (!word2Vec.hasWord(word2))
                        logger.info("Ich kenne das Wort " + word2 + " nicht.");
                }
            } else if (input.matches("\\S+[ [\\+|\\-] \\S+]*")) {
                ArrayList<String> positive = new ArrayList<>();
                ArrayList<String> negative = new ArrayList<>();
                boolean isPositive = true;
                for (int i = 0; i < words.size(); i++) {
                    String currentWord = words.get(i);
                    if ("+".equals(currentWord)) {
                        isPositive = true;
                    } else if ("-".equals(currentWord)) {
                        isPositive = false;
                    } else {
                        if (isPositive)
                            positive.add(currentWord);
                        else
                            negative.add(currentWord);
                    }
                }
                Collection<String> nearest = word2Vec.wordsNearest(positive, negative, 10);
                List<String> resultWords = nearest.stream().filter(s -> !words.contains(s)).collect(Collectors.toList());
                logger.info("= " + resultWords);
            } else {
                try {
                    GuessedIntention guessedIntentionses = guessIntention(words);
                    logger.info("Result: " + guessedIntentionses);
                } catch (Exception e) {
                    logger.error("Oops! There was an exception:", e);
                }
            }
        }
    }

    public void load(File modelFile) throws IOException {
        word2Vec = WordVectorSerializer.loadFullModel(modelFile.getAbsolutePath());
    }

    public GuessedIntention guessIntention(List<String> words) throws IOException {
        Intents intent = new Intents();
        intent.load();
        List<String> whitelistWordsNotToFilterOut = intent.getAllWords();

        List<String> wordList = words.stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(word -> !word.isEmpty())
                .filter(this::filterOutWordsNotExistingInDictionary)
                .filter(word -> filterOutTop40Words(word, whitelistWordsNotToFilterOut))
                .collect(Collectors.toList());

        logger.info("using following words: " + wordList);

        GuessedIntention guesses = new GuessedIntention();
        double dictionarySize = word2Vec.vocab().numWords();
        Map<String, List<String>> intents = intent.getIntents();
        for (Map.Entry<String, List<String>> intentionEntry : intents.entrySet()) {
            logger.info("****** Analysing intention " + intentionEntry.getKey());
            List<String> intentionWords = intentionEntry.getValue();
            BigDecimal sumForIntention = new BigDecimal(0);

            for (String word : wordList) {
                BigDecimal sumOfOneWord = intentionWords.stream()
                        .filter(intentionWord -> word2Vec.hasWord(intentionWord))
                        .map(intentionWord -> getSimilarity(word, intentionWord))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                sumForIntention = sumForIntention.add(sumOfOneWord);
            }
            BigDecimal weightedSum = sumForIntention.divide(new BigDecimal(intentionWords.size()), BigDecimal.ROUND_HALF_UP);
            guesses.addIntention(intentionEntry.getKey(), weightedSum.doubleValue());
        }
        return guesses;
    }

    private BigDecimal getSimilarity(String word, String intentionWord) {
        double similarity = word2Vec.similarity(word, intentionWord);
        logger.info("Similarity between " + word + " and " + intentionWord + ": " + similarity);
        return new BigDecimal(similarity);
    }

    private boolean filterOutWordsNotExistingInDictionary(String word) {
        boolean filterValue;
        filterValue = word2Vec.hasWord(word);
        if (!filterValue) {
            logger.info("Word '" + word + "' filtered out because it is not in dictionary.");
        }
        return filterValue;
    }

    private boolean filterOutTop40Words(String word, List<String> whitelistWords) {
        int index = word2Vec.getVocab().indexOf(word);
        boolean filterValue = true;
        if (!whitelistWords.contains(word)) {
            if (index <= 40) {
                filterValue = false;
                logger.info("Word '" + word + "' filtered out because of it's dictionary index " + index);
            }
        }
        return filterValue;
    }
}
