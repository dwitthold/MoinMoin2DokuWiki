package net.unterwelt;

import java.util.*;

public class Page {
    private final List<String> superPages;
    private final String name;
    private final List<String> content;

    Page(List<String> superPages, String name, List<String> content) {
        this.superPages = superPages;
        this.name = name;
        this.content = content;
    }

    List<String> getSuperPages() {
        return superPages;
    }

    String getName() {
        return name;
    }

    List<String> getContent() {
        return content;
    }

    /**
     * Return the page's name preceded by all super pages.
     *
     * @param divider used to separate super pages and page name
     * @return the super pages and name, separated by the divider
     */
    String getLongName(String divider) {
        if (superPages.size() == 0) {
            return name;
        }
        StringBuilder result = new StringBuilder();

        boolean firstSuperPage = true;
        for (String superPage : superPages) {
            if (firstSuperPage) {
                firstSuperPage = false;
            } else {
                result.append(divider);
            }
            result.append(superPage);
        }
        result.append(name);

        return result.toString();
    }
}
