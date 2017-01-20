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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Cool stuff to learn a word2vec model and asking it some questions.
 * Created by roman on 15.01.17.
 */
public class ServerMain {

    private static Logger logger = LoggerFactory.getLogger(LearnMain.class);

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new RuntimeException("You must define an input file as program parameter.");
        }
        ServerMain serverMain = new ServerMain();

        File modelFile = new File(args[0]);
        if (modelFile.exists()) {
            serverMain.load(modelFile);
            serverMain.askCoolQuestions();
        } else {
            throw new RuntimeException("Model file " + modelFile.getAbsolutePath() + " does not exist.");
        }
    }

    public ServerMain() {
        System.setProperty("java.util.logging.config.file", "logging.properties");
    }

    private Word2Vec word2Vec;

    private void askCoolQuestions() throws IOException {
        logger.info("Closest words:");
        Collection<String> nearFilms = word2Vec.wordsNearest("film", 10);
        logger.info("10 Words closest to 'film': " + nearFilms);
        Collection<String> maybeFrau = word2Vec.wordsNearest(Arrays.asList("königin", "mann"), Arrays.asList("könig"), 10);
        logger.info("Given König -> Königin. What is Mann -> ? " + maybeFrau);
        Collection<String> maybeParis = word2Vec.wordsNearest(Arrays.asList("frankreich", "zürich"), Arrays.asList("schweiz"), 10);
        logger.info("Given Schweiz -> Zürich. What is Frankreich -> ? " + maybeParis);
        while (true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String input = br.readLine().toLowerCase();
            if (input.equals("exit")) {
                break;
            }
            String[] words = input.split(" ");
            if (words.length == 1) {
                if (word2Vec.hasWord(words[0])) {
                    Collection<String> wordsNearest = word2Vec.wordsNearest(words[0], 10);
                    logger.info("10 Words closest to '" + words[0] + "': " + wordsNearest);
                    int indexOfWord = word2Vec.getVocab().indexOf(words[0]);
                    logger.info("Index of " + words[0] + ": " + indexOfWord);
                } else {
                    logger.info("Word " + words[0] + " is not in my dictionary");
                }
            } else if (words.length == 2) {
                if (word2Vec.hasWord(words[0]) && word2Vec.hasWord(words[1])) {
                    double similarity = word2Vec.similarity(words[0], words[1]);
                    logger.info("Similarity: " + similarity);
                    int indexOfWord1 = word2Vec.getVocab().indexOf(words[0]);
                    logger.info("Index of " + words[0] + ": " + indexOfWord1);
                    int indexOfWord2 = word2Vec.getVocab().indexOf(words[1]);
                    logger.info("Index of " + words[1] + ": " + indexOfWord2);
                } else {
                    logger.info("Words " + words[0] + " and " + words[1] + " are not in my dictionary");
                }
            } else {
                try {
                    GuessedIntention guessedIntentionses = guessIntention(input);
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

    public GuessedIntention guessIntention(String input) throws IOException {
        String normalized = TransformWikiText.normalizeText(input.toLowerCase());
        String[] words = normalized.replaceAll("[\\.\\!\\?]", "").split(" ");
        List<String> wordList = Arrays.asList(words).stream()
                .map(String::trim)
                .filter(word -> !word.isEmpty())
                .filter(word2Vec::hasWord)
                .filter(this::filterOutTop50Words)
                .collect(Collectors.toList());

        logger.debug("using following words: " + wordList);

        GuessedIntention guesses = new GuessedIntention();
        Intents intent = new Intents();
        intent.load();
        double dictionarySize = word2Vec.vocab().numWords();
        Map<String, List<String>> intents = intent.getIntents();
        for (Map.Entry<String, List<String>> intentionEntry : intents.entrySet()) {
            List<String> intentionWords = intentionEntry.getValue();
            BigDecimal sumForIntention = new BigDecimal(0);

            for (String word: wordList) {
                BigDecimal sumOfOneWord = intentionWords.stream()
                        .filter(intentionWord -> word2Vec.hasWord(intentionWord))
                        .map(intentionWord -> getSimilarity(word, intentionWord))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                double wordRelevance = (double) word2Vec.vocab().indexOf(word) / dictionarySize;
                wordRelevance = 1;
                sumForIntention = sumForIntention.add(sumOfOneWord.multiply(new BigDecimal(wordRelevance)));
            }
            BigDecimal weightedSum = sumForIntention.divide(new BigDecimal(wordList.size()), BigDecimal.ROUND_HALF_UP);
            guesses.addIntention(intentionEntry.getKey(), weightedSum.doubleValue());
        }
        return guesses;
    }

    private BigDecimal getSimilarity(String word, String intentionWord) {
        double similarity = word2Vec.similarity(word, intentionWord);
        return new BigDecimal(similarity);
    }

    private boolean filterOutTop50Words(String word) {
        int index = word2Vec.getVocab().indexOf(word);
        logger.debug(word + ": " + index);
        return index > 50;
    }
}
