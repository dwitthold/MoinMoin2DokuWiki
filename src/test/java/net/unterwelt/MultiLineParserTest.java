package net.unterwelt;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

public class MultiLineParserTest {

    @ParameterizedTest
    @MethodSource("provideForBold")
    void testBold(String original, String expected) {
        // given
        Parser parser = new Parser();

        // when
        String converted = parser.parse(original, "pageName");

        // then
        assertThat(converted).isEqualTo(expected);
    }

    private static Stream<Arguments> provideForBold() {
        return Stream.of(
                Arguments.of("normal'normal, single quoted'",
                        "normal'normal, single quoted'"),
                Arguments.of("'''bold'''normal",
                        "**bold**normal"),
                Arguments.of("normal'''bold'''normal",
                        "normal**bold**normal"),
                Arguments.of("normal'''bold continued",
                        "normal**bold continued**"),
                Arguments.of("normal'''bold'''normal'''second bold'''normal",
                        "normal**bold**normal**second bold**normal"),
                Arguments.of("normal''''bold with single quote, continued",
                        "normal**'bold with single quote, continued**"),

                Arguments.of("'''bold'''normal\nnormal'''bold'''",
                        "**bold**normal\nnormal**bold**"),
                Arguments.of("normal'''bold continued\nstill continued'''normal",
                        "normal**bold continued**\n**still continued**normal")
        );
    }
    @ParameterizedTest
    @MethodSource("provideForItalics")
    void testItalics(String original, String expected) {
        // given
        Parser parser = new Parser();

        // when
        String converted = parser.parse(original, "pageName");

        // then
        assertThat(converted).isEqualTo(expected);
    }

    private static Stream<Arguments> provideForItalics() {
        return Stream.of(
                Arguments.of("normal'normal, single quoted'",
                        "normal'normal, single quoted'"),
                Arguments.of("''italics''normal",
                        "//italics//normal"),
                Arguments.of("normal''italics''normal",
                        "normal//italics//normal"),
                Arguments.of("normal''italics continued",
                        "normal//italics continued//"),
                Arguments.of("normal''italics''normal''second italics''normal",
                        "normal//italics//normal//second italics//normal"),
                Arguments.of("normal'''bold with single quote, continued",
                        "normal**bold with single quote, continued**"),

                Arguments.of("''italics''normal\nnormal''italics''",
                        "//italics//normal\nnormal//italics//"),
                Arguments.of("normal''italics continued\nstill continued''normal",
                        "normal//italics continued//\n//still continued//normal")
        );
    }

    @ParameterizedTest
    @MethodSource("provideForBoldAndItalics")
    void testBoldAndItalics(String original, String expected) {
        // given
        Parser parser = new Parser();

        // when
        String converted = parser.parse(original, "pageName");

        // then
        assertThat(converted).isEqualTo(expected);
    }

    private static Stream<Arguments> provideForBoldAndItalics() {
        return Stream.of(
                Arguments.of("normal''italics'''mixed'''italics''normal",
                        "normal//italics**mixed**italics//normal"),
                Arguments.of("normal'''bold''mixed''bold'''normal",
                        "normal**bold//mixed//bold**normal"),
                Arguments.of("normal''italics'''mixed''bold'''normal",
                        "normal//italics**mixed**//**bold**normal"),

                // bold is checked first, so should be activated first
                Arguments.of("normal'''''mixed''bold'''normal",
                        "normal**//mixed//bold**normal"),
                // bold is checked first, so should be activated first, ergo italics should need to
                // be de- and reactivated when turning off bold
                Arguments.of("normal'''''mixed'''italics''normal",
                        "normal**//mixed//**//italics//normal"),

                Arguments.of("normal'''bold''mixed''bold continued\nstill continued'''normal",
                        "normal**bold//mixed//bold continued**\n**still continued**normal"),
                Arguments.of("normal'''bold''mixed continued\nstill continued''bold'''normal",
                        "normal**bold//mixed continued//**\n**//still continued//bold**normal"),
                Arguments.of("normal''italics'''mixed''bold continued\nstill continued'''normal",
                        "normal//italics**mixed**//**bold continued**\n**still continued**normal"),
                Arguments.of("normal''italics'''mixed continued\nstill continued''bold'''normal",
                        "normal//italics**mixed continued**//\n//**still continued**//**bold**normal")
        );
    }
}
