package net.unterwelt.rules;

public class SuperscriptRule extends EndTagInSameLineRule {
    @Override
    public String getInputStartToken() {
        return "^";
    }

    @Override
    public String getInputEndToken() {
        return "^";
    }

    @Override
    public String getOutputStartToken() {
        return "<sup>";
    }

    @Override
    public String getOutputEndToken() {
        return "</sup>";
    }
}
