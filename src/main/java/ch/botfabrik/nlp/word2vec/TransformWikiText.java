package ch.botfabrik.nlp.word2vec;

import org.apache.commons.lang.StringEscapeUtils;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.text.Normalizer;

/**
 * Created by roman on 15.01.17.
 */
public class TransformWikiText {
    public static void main(String[] args) throws Exception {

        File outFile = new File("dewiki-latest-pages-articles1.txt");
        outFile.delete();
        try (FileWriter out = new FileWriter(outFile)) {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            InputStream in = new FileInputStream("dewiki-latest-pages-articles1.xml");
            XMLEventReader eventReader = factory.createXMLEventReader(in);
            String text = "";
            boolean inTextElement = false;
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("text")) {
                    inTextElement=true;
                    text = "";
                } else if (event.isCharacters() && inTextElement) {
                    String chars = event.asCharacters().getData();
                    text += chars;
                } else if (event.isEndElement() && inTextElement) {
                    String cleanText = splitSentences(normalizeText(text.toLowerCase()));
                    if (cleanText.length()>0) {
                        out.append(cleanText).append("\n");
                    }
                } else {
                    inTextElement = false;
                }
            }
        }
    }

    static String normalizeText(String text) {
        String normalized = StringEscapeUtils.unescapeHtml(text);
        normalized = normalized.replaceAll("['–=\\(\\)\\{\\}\\[\\]\\#“’<>]", " ");
        normalized = normalized.replaceAll(" \\- ", " ");
        normalized = normalized.trim().replaceAll(" +", " ");
        return normalized;
    }

    static String splitSentences(String text) {
        String splitted = text.replaceAll("[\\.!?] ", ".\n");
        splitted = splitted.replaceAll("\r?\n[|:\\.*].*\\r?\\n", "");
        return splitted;
    }
}
