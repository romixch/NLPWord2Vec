package ch.botfabrik.nlp.word2vec;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by roman on 18.01.17.
 */
public class LearnMain {

    private static Logger logger = LoggerFactory.getLogger(LearnMain.class);

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            logger.error("You must define an input file as program parameter.");
        }
        File inputFile = new File(args[0]);
        if (inputFile.exists()) {
            Word2Vec word2Vec = learn(inputFile);
            persistModel(word2Vec, deriveModelFrom(inputFile));
        } else {
            logger.error("File " + inputFile + " does not exist.");
        }
    }

    private static void persistModel(Word2Vec word2Vec, File modelFile) {
        logger.info("Persist model...");
        modelFile.delete();
        WordVectorSerializer.writeFullModel(word2Vec, modelFile.getAbsolutePath());
    }

    private static File deriveModelFrom(File inputFile) {
        return new File(inputFile.getAbsolutePath().concat(".model"));
    }

    private static Word2Vec learn(File rawSentencesFile) throws IOException {
        logger.info("Learn from input...");
        // Strip white space before and after for each line
        SentenceIterator iter = new BasicLineIterator(rawSentencesFile);
        TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());
        Word2Vec.Builder builder = new Word2Vec.Builder();
        Word2Vec word2Vec = builder
                .minWordFrequency(60)
                .iterations(1)
                .layerSize(100)
                .seed(42)
                .windowSize(5)
                .iterate(iter)
                .tokenizerFactory(t)
                .workers(4)
                .build();
        word2Vec.fit();
        return word2Vec;
    }
}
