package net.unterwelt;

import java.util.*;
import net.unterwelt.rules.*;

public class TagCleaner {

    private final List<String> tagCombos = new ArrayList<>();

    TagCleaner(List<LineContentRule> rules) {
        for (LineContentRule rule : rules) {
            tagCombos.add(rule.getOutputStartToken() + rule.getOutputEndToken());
        }
    }

    String clean(String parsedString) {
        for (int i = 0; i < tagCombos.size(); i++) {
            if (parsedString.contains(tagCombos.get(i))) {
                parsedString = parsedString.replace(tagCombos.get(i), "");
                i = -1;  // check all tag combos again; i is increased at loop end
            }
        }

        return parsedString;
    }
}
