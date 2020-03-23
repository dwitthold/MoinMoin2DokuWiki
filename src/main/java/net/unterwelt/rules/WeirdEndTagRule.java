package net.unterwelt.rules;

/**
 * If the end tag is on the same line as the start tag, the rule behaves like
 * EndTagInSameLineRule. If the end tag is not in the same line as the start tag, the rule will
 * apply to all text until the end of the line. Start tags before the next end tag will be
 * displayed as regular text, while the next end tag will not be displayed.
 */
public abstract class WeirdEndTagRule extends LineContentRule {

    boolean isWeird = false;

    @Override
    public boolean applies(String content) {
        if (!isActive() && content.startsWith(getInputStartToken())) {
            isWeird = false;
            return true;
        }

        if (isActive() && content.startsWith(getInputEndToken())) {
            return true;
        }

        if (isWeird && content.startsWith(getInputEndToken())) {
            isWeird = false;
            return true;
        }

        return false;
    }

    @Override
    public void onActiveLineStart(StringBuilder output) {
        if (isActive()) {
            isWeird = true;
        }
    }

    @Override
    public String getOutputEndToken() {
        if (isWeird) {
            return "";
        }
        return getDefinedOutputEndToken();
    }

    protected abstract String getDefinedOutputEndToken();
}
