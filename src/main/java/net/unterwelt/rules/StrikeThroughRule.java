package net.unterwelt.rules;

public class StrikeThroughRule extends WeirdEndTagRule {

    @Override
    public String getInputStartToken() {
        return "--(";
    }

    @Override
    public String getInputEndToken() {
        return ")--";
    }

    @Override
    public String getOutputStartToken() {
        return "<del>";
    }

    @Override
    protected String getDefinedOutputEndToken() {
        return "</del>";
    }
}
