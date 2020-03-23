package net.unterwelt.rules;

import java.util.*;

public class NumeratedListRule extends LineStartWithTokenRule {

    private static final String NUMERICAL = "1. ";
    private static final String ALPHABETICAL = "a. ";
    private static final String ROMAN = "i. ";

    public NumeratedListRule() {
        super(Arrays.asList(NUMERICAL, ALPHABETICAL, ROMAN));
    }

    @Override
    public String getOutputToken() {
        return "- ";
    }
}