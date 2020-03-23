package net.unterwelt.rules;

public interface LineStartRule {

    /**
     * Cut the rule's token from the start of the line, if present.
     *
     * @param line the original line
     * @return the trimmed or the original line
     */
    String trimTokenIfPresent(String line);

    /**
     * Get the output token with indentation.
     *
     * @return the output token (with leading and trailing spaces)
     */
    String getOutputTokenWithIndentation();

}
