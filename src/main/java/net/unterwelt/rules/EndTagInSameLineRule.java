package net.unterwelt.rules;

/**
 * The end tag must be in the same line as the start tag or the start tag will be displayed as
 * regular text (and the format will not be changed).
 */
public abstract class EndTagInSameLineRule extends LineContentRule {

    @Override
    public boolean applies(String content) {
        if (!isActive() && content.startsWith(getInputStartToken())) {
            String withoutStartTag = content.substring(getInputStartToken().length());
            return withoutStartTag.contains(getInputEndToken());
        } else if(isActive() && content.startsWith(getInputEndToken())) {
            return true;
        }
        return false;
    }

    @Override
    public void onActiveLineStart(StringBuilder output) {
        throw new IllegalStateException("EndTagInSameLineRule must never be active at the start "
                + "of a new line\noutput so far: " + output.toString());
    }
}
