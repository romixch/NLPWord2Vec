package ch.botfabrik.nlp.word2vec;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides all the intents available
 *
 * Created by roman on 18.01.17.
 */
public class Intents {

    private Map<String, List<String>> intents = new HashMap<>();

    public void load() throws IOException {
        File intentsFile = new File("intents.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(intentsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] splitted = line.split(":");
                String[] context = splitted[1].trim().split(" ");
                List<String> trimmedContextList = Arrays.asList(context)
                        .stream()
                        .map(s -> s.trim())
                        .collect(Collectors.toList());
                intents.put(splitted[0], trimmedContextList);
            }
        }
    }

    public Map<String, List<String>> getIntents() {
        return intents;
    }
}
