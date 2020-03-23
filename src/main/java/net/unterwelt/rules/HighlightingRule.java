package net.unterwelt.rules;

public class HighlightingRule implements NoFormattingRule {

    private boolean isActive = false;
    private String language;

    @Override
    public int applies(String content) {
        if (isActive() && isEndTag(content)) {
            return getInputEndToken().length();
        }

        if (!isActive() && content.startsWith(getInputStartTokenBase())) {
            String contentAfterStartToken = content.substring(getInputEndToken().length());
            String[] strings = contentAfterStartToken.split(" ");
            if (strings.length > 1) {
                language = strings[1];
            } else {
                language = "";
            }
            return getInputStartTokenBase().length() + contentAfterStartToken.length();
        }

        return 0;
    }

    private boolean isEndTag(String content) {
        return content.startsWith(getInputEndToken());
    }

    private String getInputStartTokenBase() {
        return "{{{#!highlight ";
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void setActive(boolean active) {
        isActive = active;
        if (!active) {
            language = "";
        }
    }

    @Override
    public String getInputEndToken() {
        return "}}}";
    }

    @Override
    public String getOutputStartToken() {
        return String.format("<code %s>", language);
    }

    @Override
    public String getOutputEndToken() {
        return "</code>";
    }

    @Override
    public boolean needsCloseAtEnd() {
        return false;
    }
}
