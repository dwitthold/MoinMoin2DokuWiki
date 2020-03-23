package net.unterwelt.util;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Transliterator {
    private HashMap<String, String> mappings = new HashMap<>();

    public Transliterator() {
        // common
        mappings.put(" ", "_");
        mappings.put(",", "_");
        mappings.put("!", "_");
        mappings.put("&", "_");
        mappings.put("(", "_");
        mappings.put(")", "_");
        mappings.put("/", "_");
        mappings.put("\\", "_");

        // german
        mappings.put("ä", "ae");
        mappings.put("Ä", "Ae");
        mappings.put("ö", "oe");
        mappings.put("Ö", "Oe");
        mappings.put("ü", "ue");
        mappings.put("Ü", "Ue");
        mappings.put("ß", "ss");

        // french
        mappings.put("ç", "c");
        mappings.put("Ç", "C");
        mappings.put("á", "a");
        mappings.put("Á", "A");
        mappings.put("à", "a");
        mappings.put("À", "A");
        mappings.put("é", "e");
        mappings.put("É", "E");
        mappings.put("è", "e");
        mappings.put("È", "E");
        mappings.put("û", "u");
    }

    public Transliterator(Path newMappings) throws IOException {
        this();

        List<String> lines;
        lines = Files.readAllLines(newMappings);

        if (lines.get(lines.size() - 1).isEmpty()) {
            lines.remove(lines.size() - 1);
        }
        if (lines.size() % 2 != 0) {
            throw new IllegalArgumentException("Mapping file must have an even number of lines");
        }

        for (int i = 0; i < lines.size(); i += 2) {
            mappings.put(lines.get(i), lines.get(i + 1));
        }
    }

    public String transliterate(String in) {
        StringBuilder transliterated = new StringBuilder(in.length());

        for (int i = 0; i < in.length(); i++) {
            transliterated.append(get(in.substring(i, i + 1)));
        }
        String collapsed = collapse(transliterated.toString());
        return trim(collapsed);
    }

    String get(String in) {
        String value = mappings.get(in);
        if (value != null) {
            return value;
        }
        return in;
    }

    private String collapse(String text) {
        return text.replaceAll("_+", "_");
    }

    private String trim(String text) {
        if (text.startsWith("_")) {
            text = text.substring(1);
        }
        if (text.endsWith("_")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }
}