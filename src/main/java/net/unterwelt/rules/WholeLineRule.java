package net.unterwelt.rules;

public interface WholeLineRule {

    /**
     * Convert the whole line. Return the original line if the rule doesn't apply.
     *
     * @param line the original line
     * @return the converted or original line
     */
    String convert(String line);
}
