package net.unterwelt;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

public class TableParserTest {

    @ParameterizedTest
    @MethodSource("provideForTables")
    void testTables(String original, String expected) {
        // given
        Parser parser = new Parser();

        // when
        String converted = parser.parse(original, "pageName");

        // then
        assertThat(converted).isEqualTo(expected);
    }

    private static Stream<Arguments> provideForTables() {
        return Stream.of(
                // basic
                Arguments.of("||simple row|| second cell with spaces ||",
                        "|simple row| second cell with spaces |"),
                Arguments.of("||simple row|| second cell with spaces ||\n||second row||last cell||",
                        "|simple row| second cell with spaces |\n|second row|last cell|"),

                // alignment
                Arguments.of("||cell 1||<style=\"text-align: left;\">aligned left|| cell 3 ||",
                        "|cell 1|aligned left  | cell 3 |"),
                Arguments.of("||cell 1||<style=\"text-align: right;\">aligned right|| cell 3 ||",
                        "|cell 1|  aligned right| cell 3 |"),
                Arguments.of("||cell 1||<style=\"text-align: center;\">aligned center|| cell 3 ||",
                        "|cell 1|  aligned center  | cell 3 |"),

                // alignment ignored
                Arguments.of("||cell 1|| <style=\"text-align: left;\">leading space|| cell 3 ||",
                        "|cell 1| <style=\"text-align: left;\">leading space| cell 3 |"),
                Arguments.of(
                        "||cell 1||<style=\"vertical-align: bottom;\">vertical not possible|| cell 3 ||",
                        "|cell 1|<style=\"vertical-align: bottom;\">vertical not possible| cell 3 |"),

                // colSpan
                Arguments.of("||ab||cd||ef||gh||\n||12||||56||78||",
                        "|ab|cd|ef|gh|\n|12|56||78|"),
                Arguments.of("||<-3>col spanned|| cell 4 ||cell 5||\n"
                                + "||cell a||cell b||cell c||cell d||cell e||",
                        "|col spanned||| cell 4 |cell 5|\n"
                                + "|cell a|cell b|cell c|cell d|cell e|"),

                // colSpan ignored
                Arguments.of("|| <-3>leading space|| cell 4 ||cell 5||\n"
                                + "||cell a||cell b||cell c||cell d||cell e||",
                        "| <-3>leading space| cell 4 |cell 5|\n"
                                + "|cell a|cell b|cell c|cell d|cell e|"),

                // mixed colSpan and alignment
                Arguments.of(
                        "||<style=\"text-align: center;\"><-3>centered w/o colspan|| cell 4 ||cell 5||\n"
                                + "||cell a||cell b||cell c||cell d||cell e||",
                        "|  <-3>centered w/o colspan  | cell 4 |cell 5|\n"
                                + "|cell a|cell b|cell c|cell d|cell e|"),
                Arguments.of(  // does not comply with real MoinMoin behavior (see below)
                        "||<-3><style=\"text-align: center;\">col spanned centered|| cell 4 ||cell 5||\n"
                                + "||cell a||cell b||cell c||cell d||cell e||",
                        "|  col spanned centered  ||| cell 4 |cell 5|\n"
                                + "|cell a|cell b|cell c|cell d|cell e|"),

                // mixed with other rules
                Arguments.of("||'''bold'''|| second cell with spaces ||",
                        "|**bold**| second cell with spaces |"),

                // not/ partly converted
                Arguments.of("||first cell|| trailing space || \n||second row|| trailing space 2 || ",
                        "||first cell|| trailing space || \n||second row|| trailing space 2 || "),
                Arguments.of("a||leading letter|| not a cell ||\n||cell 1|| cell 2 ||",
                        "a||leading letter|| not a cell ||\n|cell 1| cell 2 |"),
                Arguments.of("||first part|| trailing space || \n"
                                + "||cell 1|| cell 2 ||\n"
                                + "a||leading letter|| not a cell ||\n"
                                + "||cell 3|| cell 4 ||\n",
                        "||first part|| trailing space || \n"
                                + "|cell 1| cell 2 |\n"
                                + "a||leading letter|| not a cell ||\n"
                                + "|cell 3| cell 4 |\n"),

                // not converted, mixed with other rule
                Arguments.of("normal ''' ||bold||'''normal",
                        "normal ** ||bold||**normal"),

                // weird stuff: does not comply with actual behavior in MoinMoin (see disabled test)
                Arguments.of(" * ||not a cell 1|| not a cell 2 ||",
                        "  * ||not a cell 1|| not a cell 2 ||"),
                Arguments.of("||'''bold|| second cell''' with spaces ||",
                        "|**bold| second cell** with spaces |")
        );
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("provideForTablesIgnored")
    void testTablesIgnored(String original, String expected) {
        // given
        Parser parser = new Parser();

        // when
        String converted = parser.parse(original, "pageName");

        // then
        assertThat(converted).isEqualTo(expected);
    }

    private static Stream<Arguments> provideForTablesIgnored() {
        return Stream.of(
                // MoinMoin shows additional pipe symbols, so nowiki would be a solution (?)
                Arguments.of("|||cell 1||| cell 2 |||",
                        "|<nowiki>|</nowiki>cell 1|<nowiki>|</nowiki> cell 2 <nowiki>|</nowiki>|"),

                // there would actually be a line break after the first "||"
                Arguments.of(" * ||not a cell 1|| not a cell 2 ||",
                        "  * ||\nnot a cell 1|| not a cell 2 ||"),

                // end tag is not shown, second cell is not bold
                Arguments.of("||'''bold|| second cell''' with spaces ||",
                        "|**bold**| second cell with spaces |"),

                // row span is implemented yet
                Arguments.of("||<|2>row spanned|| cell 2 ||cell 3||\n||cell b||cell c||\n"
                                + "||cell x||cell y||cell z||",
                        "|row spanned| cell 2 |cell 3|\n|:::|cell b|cell c|\n"
                                + "|cell x|cell y|cell z|"),

                // only first tag is parsed in MoinMoin
                Arguments.of(
                        "||<-3><style=\"text-align: center;\">col spanned w/o center|| cell 4 ||cell 5||\n"
                                + "||cell a||cell b||cell c||cell d||cell e||",
                        "|<style=\"text-align: center;\">col spanned w/o center||| cell 4 |cell 5|\n"
                                + "|cell a|cell b|cell c|cell d|cell e|")
        );
    }

}
