package net.unterwelt;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

class ParserTest {

    @ParameterizedTest
    @MethodSource("provideForMixedLineContent")
    void testMixedLineContent(String original, String expected) {
        // given
        Parser parser = new Parser(false, true);

        // when
        String converted = parser.parse(original, "pageName");

        // then
        assertThat(converted).isEqualTo(expected);
    }

    private static Stream<Arguments> provideForMixedLineContent() {
        return Stream.of(
                Arguments.of("normal'''bold''bold and italics^plus super^'''''",
                        "normal**bold//bold and italics<sup>plus super</sup>//**"),  // cleaned
                Arguments.of("normal''italics^not super"
                                + "\n\nnext line still italics with lone super tag^'' normal",
                        "normal//italics^not super//"
                                + "\n\n//next line still italics with lone super tag^// normal")  // cleaned
        );
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("provideForMixedLineContentIgnored")
    void testMixedLineContentIgnored(String original, String expected) {
        // given
        Parser parser = new Parser(false, true);

        // when
        String converted = parser.parse(original, "pageName");

        // then
        assertThat(converted).isEqualTo(expected);
    }

    private static Stream<Arguments> provideForMixedLineContentIgnored() {
        return Stream.of(
                // bold and/ or italics are not (de-)activated while in superscript mode
                Arguments.of("normal''italics^italics and super'''should be plus bold'''"
                                + "italics and super again^italics''normal",
                        "normal//italics<sup>italics and super'''should be plus bold'''"
                                + "italics and super again</sup>italics//normal"),
                Arguments.of("normal^super''should add italics"
                        + "'''should also add bold'''''super^normal",
                        "normal<sup>super''should add italics"
                        + "'''should also add bold'''''super</sup>normal"),
                Arguments.of("normal'''bold''italics^sup''should be italics off^sup off"
                                + "'''still italics''normal",
                        "normal**bold//italics<sup>sup''should be italics off</sup>sup off"
                                + "//**//still italics//normal"),

                // table cells in link results in nowiki?
                Arguments.of("[[||ab||cd||]]", "<nowiki>[[||ab||cd||]]</nowiki>"),
                // but || in link result in link (with single | ?)
                Arguments.of("[[ab||cd]]", "[[ab|cd]]")
        );
    }

    @ParameterizedTest
    @MethodSource("provideForMisc")
    void testMisc(String original, String expected) {
        // given
        Parser parser = new Parser(false, true);

        // when
        String converted = parser.parse(original, "pageName");

        // then
        assertThat(converted).isEqualTo(expected);
    }

    private static Stream<Arguments> provideForMisc() {
        return Stream.of(
                Arguments.of("first line\n\nthird line", "first line\n\nthird line"),
                Arguments.of("first line\n\nthird line\n", "first line\n\nthird line\n")
        );
    }
}
