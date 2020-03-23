package net.unterwelt.rules;

/**
 * End tag can be on another line and all the rule will apply to all text between start and end tag
 */
public abstract class MultiLineRule extends LineContentRule {

    @Override
    @SuppressWarnings("RedundantIfStatement")
    public boolean applies(String content) {
        if (isActive() && content.startsWith(getInputEndToken())) {
            return true;
        } else if (!isActive() && content.startsWith(getInputStartToken())) {
            return true;
        }
        return false;
    }

    @Override
    public void onActiveLineStart(StringBuilder output) {
        output.append(getOutputStartToken());
    }
}
