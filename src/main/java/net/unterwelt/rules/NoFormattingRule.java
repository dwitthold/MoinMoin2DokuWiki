package net.unterwelt.rules;

public interface NoFormattingRule {

    int applies(String content);

    boolean isActive();

    void setActive(boolean active);

    String getInputEndToken();

    String getOutputStartToken();

    String getOutputEndToken();

    boolean needsCloseAtEnd();
}
