package net.unterwelt;

import java.util.*;
import net.unterwelt.rules.*;

/**
 * Parse the given content Strings from MoinMoin's format to DokuWiki's format.
 */
class Parser {

    private static final String IMPORT_WARNING = "FIXME **//imported from MoinMoin//**\n\n";

    final private List<WholeLineRule> wholeLineRules;
    final private List<LineStartRule> lineStartRules;
    final private TableRule tableRule;

    final private List<NoFormattingRule> noFormattingRules;
    private final boolean addFixMe;
    private final boolean cleanTags;
    private NoFormattingRule activeNoFormattingRule = null;

    final private LinkRule linkRule;

    final private List<LineContentRule> lineContentRules;
    final private List<LineContentRule> activeLineContentRules = new ArrayList<>();

    private final TagCleaner tagCleaner;
    private StringBuilder output;


    Parser() {
        this(false, false);
    }

    Parser(boolean addFixMe, boolean cleanTags){
        this.addFixMe = addFixMe;
        this.cleanTags = cleanTags;

        wholeLineRules = Collections.singletonList(new HeadingRule());

        lineStartRules = List.of(
                new SimpleListRule(),
                new NumeratedListRule()
        );

        tableRule = new TableRule();

        noFormattingRules = List.of(
                // Highlighting must be checked before NoWiki as they both use {{{...}}}
                new HighlightingRule(),
                new NoWikiRule()
        );

        linkRule = new LinkRule();

        lineContentRules = List.of(
                // bold (''') must be checked before italics ('')
                new BoldRule(),
                new ItalicsRule(),

                // in no particular order:
                new MonospacedRule(),
                new SubscriptRule(),
                new SuperscriptRule(),
                new StrikeThroughRule(),
                new UnderlinedRule()
        );

        if (cleanTags) {
            tagCleaner = new TagCleaner(lineContentRules);
        } else {
            tagCleaner = new TagCleaner(new ArrayList<>());
        }
    }

    String parse(Page page) {
        StringBuilder outputForList = new StringBuilder();
        if (addFixMe) {
            outputForList.append(IMPORT_WARNING);
        }
        for (String string : page.getContent()) {
            outputForList.append(parse(string, page.getName()));
        }
        return outputForList.toString();
    }

    String parse(String input, String pageName) {
        Scanner scanner = new Scanner(input).useDelimiter(System.lineSeparator());
        output = new StringBuilder();

        while (scanner.hasNext()) {
            final String line = scanner.nextLine();

            boolean wasConverted = convertWholeLine(line);
            if (wasConverted) {
                continue;
            }

            tableRule.checkActivation(line);  // check before line is trimmed

            final String trimmedLine = convertLineStart(line);

            convertInLineTokens(trimmedLine, pageName);

            if (scanner.hasNext()) {
                output.append(System.lineSeparator());
            }
        }

        if (input.endsWith("\n")) {
            output.append("\n");
        }

        String parsedString = output.toString();
        if (cleanTags) {
            parsedString = tagCleaner.clean(parsedString);
        }

        return parsedString;
    }

    private boolean formattingIsIgnored() {
        return activeNoFormattingRule != null;
    }

    private boolean convertWholeLine(String line) {
        if (formattingIsIgnored()) {
            return false;
        }

        for (WholeLineRule rule : wholeLineRules) {
            String convertedLine = rule.convert(line);
            if (!convertedLine.equals(line)) {
                output.append(convertedLine);
                return true;
            }
        }
        return false;
    }

    private String convertLineStart(String line) {
        if (formattingIsIgnored()) {
            return line;
        }

        for (LineStartRule rule : lineStartRules) {
            String trimmedLine = rule.trimTokenIfPresent(line);
            if (!trimmedLine.equals(line)) {
                output.append(rule.getOutputTokenWithIndentation());
                return trimmedLine;
            }
        }
        return line;
    }

    private void convertInLineTokens(String line, String pageName) {
        reactivateCurrentFormats();
        convertLineContent(line, pageName);
        writeEndTagsForCurrentFormats();
    }

    /**
     * Tags that were closed at the end of the previous line need to be reactivated.
     */
    private void reactivateCurrentFormats() {
        if (formattingIsIgnored() && activeNoFormattingRule.needsCloseAtEnd()) {
            output.append(activeNoFormattingRule.getOutputStartToken());
        } else {
            for (LineContentRule activeRule : activeLineContentRules) {
                activeRule.onActiveLineStart(output);
            }
        }
    }

    private void convertLineContent(String line, String pageName) {
        for (int i = 0; i < line.length(); i++) {
            boolean isRegularContent = true;
            String uncheckedLinePart = line.substring(i);

            // handle active NoFormattingRule
            if (formattingIsIgnored()) {
                int processedChars = activeNoFormattingRule.applies(uncheckedLinePart);
                if (processedChars > 0) {
                    i = i + deactivateNoFormattingRule(activeNoFormattingRule);
                } else {
                    output.append(line.charAt(i));
                }
                continue;
            }

            // check for NoFormattingRule's start tags
            boolean activatedNoFormatting = false;
            for (NoFormattingRule rule : noFormattingRules) {
                int processedChars = rule.applies(uncheckedLinePart);
                if (processedChars > 0) {
                    activateNoFormattingRule(rule);
                    i += processedChars - 1;
                    activatedNoFormatting = true;
                    break;
                }
            }
            if (activatedNoFormatting) {
                continue;
            }

            // handle links
            LinkRule.LinkRuleResult linkRuleResult = linkRule.convert(uncheckedLinePart, pageName);
            if (linkRuleResult.processedCharacters > 0) {
                i = i + linkRuleResult.processedCharacters - 1;
                output.append(linkRuleResult.resultingLink);
                continue;
            }

            // handle tables
            TableRule.TableRuleResult tableRuleResult = tableRule.convert(uncheckedLinePart);
            if (tableRuleResult.processedCharacters > 0) {
                i = i + tableRuleResult.processedCharacters - 1;
                output.append(tableRuleResult.resultingSeparators);
                continue;
            }

            // handle regular rules
            for (LineContentRule rule : lineContentRules) {
                if (rule.applies(uncheckedLinePart)) {
                    isRegularContent = false;
                    if (rule.isActive()) {
                        i = i + deactivateRule(rule);
                    } else {
                        i = i + activateRule(rule);
                    }
                    break;
                }
            }

            // just append if character is not part of a token
            if (isRegularContent) {
                output.append(line.charAt(i));
            }
        }
    }

    private void activateNoFormattingRule(NoFormattingRule noFormattingRule) {
        activeNoFormattingRule = noFormattingRule;
        noFormattingRule.setActive(true);

        for (int ruleIndex = activeLineContentRules.size(); ruleIndex > 0; ruleIndex--) {
            output.append(activeLineContentRules.get(ruleIndex - 1).getOutputEndToken());
        }

        output.append(noFormattingRule.getOutputStartToken());
    }

    private int deactivateNoFormattingRule(NoFormattingRule noFormattingRule) {
        activeNoFormattingRule = null;
        noFormattingRule.setActive(false);

        output.append(noFormattingRule.getOutputEndToken());

        for (LineContentRule rule : activeLineContentRules) {
            output.append(rule.getOutputStartToken());
        }

        return noFormattingRule.getInputEndToken().length() - 1;  // -1 as i is also increased in loop
    }

    private int activateRule(LineContentRule rule) {
        activeLineContentRules.add(rule);
        rule.setActive(true);

        output.append(rule.getOutputStartToken());
        return rule.getInputStartToken().length() - 1;  // -1 as i is also increased in loop
    }

    private int deactivateRule(LineContentRule rule) {
        final StringBuilder reactivateRules = new StringBuilder();
        for (int activeRuleIndex = activeLineContentRules.size() - 1; activeRuleIndex >= 0; activeRuleIndex--) {
            if (rule == activeLineContentRules.get(activeRuleIndex)) {
                activeLineContentRules.remove(activeRuleIndex);
                rule.setActive(false);

                output.append(rule.getOutputEndToken());
                output.append(reactivateRules.toString());
                return rule.getInputEndToken().length() - 1;  // -1 as i is also increased in loop
            } else {
                LineContentRule otherRule = activeLineContentRules.get(activeRuleIndex);
                output.append(otherRule.getOutputEndToken());
                reactivateRules.insert(0, otherRule.getOutputStartToken());
            }
        }
        output.append(reactivateRules.toString());
        return 0;
    }

    /**
     * In Dokuwiki nowiki-tags prevent linebreaks, but code-tags don't. Other formats will only be
     * recognized when they are closed in the same line.
     */
    private void writeEndTagsForCurrentFormats() {
        if (formattingIsIgnored() && activeNoFormattingRule.needsCloseAtEnd()) {
            output.append(activeNoFormattingRule.getOutputEndToken());
        } else {
            for (int index = activeLineContentRules.size() - 1; index >= 0; index--) {
                output.append(activeLineContentRules.get(index).getOutputEndToken());
            }
        }
    }
}
