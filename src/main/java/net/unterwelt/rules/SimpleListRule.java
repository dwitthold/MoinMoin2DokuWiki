package net.unterwelt.rules;

import java.util.*;

public class SimpleListRule extends LineStartWithTokenRule {

    private static final String ASTERISK = "* ";

    public SimpleListRule() {
        super(List.of(ASTERISK));
    }

    @Override
    String getOutputToken() {
        return "* ";
    }
}
