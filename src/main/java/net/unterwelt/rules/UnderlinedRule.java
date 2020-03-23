package net.unterwelt.rules;

public class UnderlinedRule extends WeirdEndTagRule {

    @Override
    public String getInputStartToken() {
        return "__";
    }

    @Override
    public String getInputEndToken() {
        return "__";
    }

    @Override
    public String getOutputStartToken() {
        return "__";
    }

    @Override
    protected String getDefinedOutputEndToken() {
        return "__";
    }
}
