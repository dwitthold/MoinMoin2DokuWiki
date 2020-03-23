package net.unterwelt.rules;

public class BoldRule extends MultiLineRule {
    @Override
    public String getInputStartToken() {
        return "'''";
    }

    @Override
    public String getInputEndToken() {
        return "'''";
    }

    @Override
    public String getOutputStartToken() {
        return "**";
    }

    @Override
    public String getOutputEndToken() {
        return "**";
    }
}
