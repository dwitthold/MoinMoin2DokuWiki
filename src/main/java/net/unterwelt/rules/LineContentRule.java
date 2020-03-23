package net.unterwelt.rules;

/**
 * Start and end tags can occur anywhere in a line's content (i.e. not only at the start) and
 * only a part of a line's content can be affected by the rule.
 */
public abstract class LineContentRule {

    private boolean isActive = false;

    public abstract boolean applies(String content);

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Hook to execute when the rule is active at the start of a new line.
     *
     * @param output the Parser's output
     */
    public abstract void onActiveLineStart(StringBuilder output);

    public abstract String getInputStartToken();

    public abstract String getInputEndToken();

    public abstract String getOutputStartToken();

    public abstract String getOutputEndToken();
}
