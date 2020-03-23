package net.unterwelt.rules;

import java.util.*;

public abstract class LineStartWithTokenRule implements LineStartRule {

    private final List<String> listPrefixes;
    private int indentation = 0;

    LineStartWithTokenRule(List<String> listPrefixes) {
        this.listPrefixes = listPrefixes;
    }

    @Override
    public String trimTokenIfPresent(String line) {
        if (!line.startsWith(" ")) {
            return line;
        }

        final String trimmedLine = line.trim();
        indentation = 0;
        String contentWithoutPrefix = "";
        for (String prefix : listPrefixes) {
            if (trimmedLine.startsWith(prefix)) {
                indentation = line.indexOf(prefix);
                contentWithoutPrefix = trimmedLine.substring(prefix.length());
                break;
            }
        }

        if (indentation > 0) {
            return contentWithoutPrefix;
        } else {
            return line;
        }
    }

    @Override
    public String getOutputTokenWithIndentation() {
        return "  ".repeat(indentation) + getOutputToken();
    }

    /**
     * Get the output token.
     *
     * @return the output token (without leading, but with trailing spaces)
     */
    abstract String getOutputToken();
}
