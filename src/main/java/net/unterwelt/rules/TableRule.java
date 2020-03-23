package net.unterwelt.rules;

public class TableRule {

    private static final String MOINMOIN_SEPARATOR = "||";
    private static final String ALIGN_LEFT = "<style=\"text-align: left;\">";
    private static final String ALIGN_RIGHT = "<style=\"text-align: right;\">";
    private static final String ALIGN_CENTER = "<style=\"text-align: center;\">";


    boolean isActive = false;
    int colSpan = 0;
    boolean alignmentAtEnd = false;

    public TableRuleResult convert(String linePart) {
        if (!isActive) {
            return new TableRuleResult(0, "");
        }
        if (!linePart.startsWith("||")) {
            return new TableRuleResult(0, "");
        }
        StringBuilder result = new StringBuilder();

        if (alignmentAtEnd) {
            result.append("  ");
            alignmentAtEnd = false;
        }

        closeExistingColSpans(result);

        int processedCharacters = parseNewColSpans(linePart);

        processedCharacters += parseAlignment(result, linePart, processedCharacters);

        return new TableRuleResult(processedCharacters, result.toString());
    }

    private int parseAlignment(StringBuilder result, String linePart, int processedCharacters) {
        String withoutLeadingSeparators = linePart.substring(processedCharacters);

        if (withoutLeadingSeparators.startsWith(ALIGN_RIGHT)) {
            result.append("  ");
            return ALIGN_RIGHT.length();
        } else if (withoutLeadingSeparators.startsWith(ALIGN_CENTER)) {
            result.append("  ");
            alignmentAtEnd = true;
            return ALIGN_CENTER.length();
        } else if (withoutLeadingSeparators.startsWith(ALIGN_LEFT)) {
            alignmentAtEnd = true;
            return ALIGN_LEFT.length();
        }

        return 0;
    }

    private StringBuilder closeExistingColSpans(StringBuilder result) {
        result.append("|");
        while (colSpan > 0) {
            colSpan--;
            result.append("|");
        }
        return result;
    }

    private int parseNewColSpans(String linePart) {
        int processedCharacters = handleImplicitColSpans(linePart);
        processedCharacters += handleExplicitColSpans(linePart, processedCharacters);

        return processedCharacters;
    }

    private int handleImplicitColSpans(String linePart) {
        int separatorCount = 1;
        while (linePart.substring(separatorCount * MOINMOIN_SEPARATOR.length()).startsWith(MOINMOIN_SEPARATOR)) {
            colSpan++;
            separatorCount++;
        }
        return separatorCount * MOINMOIN_SEPARATOR.length();
    }

    private int handleExplicitColSpans(String linePart, int processedCharacters) {
        String withoutLeadingSeparators = linePart.substring(processedCharacters);
        int tagLength = 0;

        if (withoutLeadingSeparators.startsWith("<-")) {
            int colSpanEndTag = withoutLeadingSeparators.indexOf(">");
            if (colSpanEndTag >= 0) {
                try {
                    int additionalSpan = Integer.parseInt(withoutLeadingSeparators.substring(2,
                            colSpanEndTag));
                    this.colSpan += additionalSpan - 1;  // adjust for default colSpan (1)
                    tagLength = colSpanEndTag + 1;
                } catch (NumberFormatException e) {
                    // not an integer; ergo no colSpan increase
                }
            }
        }

        return tagLength;
    }

    public void checkActivation(String line) {
        isActive = line.startsWith(MOINMOIN_SEPARATOR) && line.endsWith(MOINMOIN_SEPARATOR);
    }


    public static class TableRuleResult {
        public final int processedCharacters;
        public final String resultingSeparators;

        TableRuleResult(int processedCharacters, String resultingSeparators) {
            this.processedCharacters = processedCharacters;
            this.resultingSeparators = resultingSeparators;
        }
    }

}
