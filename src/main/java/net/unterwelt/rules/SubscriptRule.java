package net.unterwelt.rules;

public class SubscriptRule extends EndTagInSameLineRule {
    @Override
    public String getInputStartToken() {
        return ",,";
    }

    @Override
    public String getInputEndToken() {
        return ",,";
    }

    @Override
    public String getOutputStartToken() {
        return "<sub>";
    }

    @Override
    public String getOutputEndToken() {
        return "</sub>";
    }
}
