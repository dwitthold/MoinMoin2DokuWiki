package net.unterwelt.rules;

public class LinkRule {

    private static final String START_TOKEN = "[[";
    private static final String END_TOKEN = "]]";
    private static final String HTTP_TOKEN = "http://";
    private static final String HTTPS_TOKEN = "https://";

    private static final String MOINMOIN_PATH_ELEMENT = "/";
    private static final String MOINMOIN_SUPER_NAVIGATION = "../";
    private static final String DOKUWIKI_CURRENT = ".:";
    private static final String DOKUWIKI_PATH_ELEMENT = ":";

    private static final String[] INVALID_CHARACTERS = {":", "?"};
    private static final String REPLACEMENT = "_";

    public LinkRuleResult convert(String original, String pageName) {
        // start token not found
        if (!applies(original)) {
            return new LinkRuleResult(0, null);
        }

        // end token not found
        int endIndex = original.indexOf(END_TOKEN);
        if (endIndex == -1) {
            return new LinkRuleResult(0, null);
        }

        String link = original.substring(START_TOKEN.length(), endIndex);
        // http(s)-link -> keep link as it is
        if (link.startsWith(HTTP_TOKEN) || link.startsWith(HTTPS_TOKEN)) {
            return createResult(endIndex, link);
        }

        // replace invalid characters in DokuWiki
        for (String character : INVALID_CHARACTERS) {
            link = link.replace(character, REPLACEMENT);
        }

        // replace navigation tokens
        if (link.startsWith(MOINMOIN_PATH_ELEMENT)) {
            link = DOKUWIKI_CURRENT + pageName + link;
        } else if (link.startsWith(MOINMOIN_SUPER_NAVIGATION)) {
            link = DOKUWIKI_CURRENT + pageName + DOKUWIKI_PATH_ELEMENT + link;
        } else {
            link = DOKUWIKI_PATH_ELEMENT + link;
        }
        link = link.replace(MOINMOIN_PATH_ELEMENT, DOKUWIKI_PATH_ELEMENT);

        return createResult(endIndex, link);
    }

    private boolean applies(String uncheckedLinePart) {
        return uncheckedLinePart.startsWith("[[");
    }

    private LinkRuleResult createResult(int endIndex, String link) {
        link = START_TOKEN + link + END_TOKEN;
        return new LinkRuleResult(endIndex + END_TOKEN.length(), link);
    }

    /**
     * ResultingLink may be null if processedCharacters is zero.
     */
    public static class LinkRuleResult {

        public final int processedCharacters;
        public final String resultingLink;

        LinkRuleResult(int processedCharacters, String resultingLink) {
            this.processedCharacters = processedCharacters;
            this.resultingLink = resultingLink;
        }
    }
}
