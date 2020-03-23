package net.unterwelt.rules;

/**
 * Transforms generic code-formatting form MoinMoin into Dokuwiki's nowiki-format.
 *
 * <p>The enclosing box from MoinMoin will be lost.
 *
 * <p>Dokuwiki's tags need to be placed around each line, as nowiki collapses all lines within the
 * tags into a single line.
 *
 * <p>MoinMoin's start tags for multiline code may only be followed by whitespace* (on the same
 * line). The end tags may be followed by any characters.
 *
 * <p>If MoinMoin's start and end tags are on the same line, the start tags may be followed by any
 * characters. If the end tags are not on the same line and the start tags are followed by at
 * least one whitespace*, the start tags and the following characters are displayed regularly.
 *
 * <p>MoinMoin's 'even more weird' behavior (if the start tag is immediately followed
 * by non-whitespace* and the end tags are not on the same line, the start tags and anything after
 * it are ignored), is not implemented.
 *
 * <p>Also not implemented is the possibility to have e.g. bold, unformatted text: in MoinMoin
 * unformatted/ code text can be bold, if the bold tags start before and end after the
 * unformatted text.
 *
 * <p>It is unclear whioch whitespace characters are actually included. So far only space is
 * considered whitespace.
 */
public class NoWikiRule implements NoFormattingRule {

    private boolean isActive = false;

    @Override
    public int applies(String content) {
        if (isActive() && isEndTag(content)) {
            return getInputEndToken().length();
        }

        if (!isActive() && content.startsWith(getInputStartToken())) {
            String restOfLine = content.substring(getInputStartToken().length());
            if (isMultiLineStart(restOfLine)) {
                return getInputStartToken().length();
            }
            if (hasEndTagInSameLine(restOfLine)) {
                return getInputStartToken().length();
            }
        }

        return 0;
    }

    private boolean isEndTag(String content) {
        return content.startsWith(getInputEndToken());
    }

    private String getInputStartToken() {
        return "{{{";
    }

    private boolean isMultiLineStart(String restOfLine) {
        return restOfLine.trim().length() == 0;
    }

    private boolean hasEndTagInSameLine(String restOfLine) {
        return restOfLine.contains(getInputEndToken());
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String getInputEndToken() {
        return "}}}";
    }

    @Override
    public String getOutputStartToken() {
        return "<nowiki>";
    }

    @Override
    public String getOutputEndToken() {
        return "</nowiki>";
    }

    @Override
    public boolean needsCloseAtEnd() {
        return true;
    }
}
