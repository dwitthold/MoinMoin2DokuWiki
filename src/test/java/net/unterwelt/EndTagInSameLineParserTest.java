package net.unterwelt;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

public class EndTagInSameLineParserTest {

    @ParameterizedTest
    @MethodSource("provideForMonospaced")
    void testMonospaced(String original, String expected) {
        // given
        Parser parser = new Parser();

        // when
        String converted = parser.parse(original, "pageName");

        // then
        assertThat(converted).isEqualTo(expected);
    }

    private static Stream<Arguments> provideForMonospaced() {
        return Stream.of(
                Arguments.of("normal `monospaced` normal", "normal ''monospaced'' normal"),
                Arguments.of("normal` monospaced `normal", "normal'' monospaced ''normal"),
                Arguments.of("normal `monospaced` normal again `second monospaced`",
                        "normal ''monospaced'' normal again ''second monospaced''"),

                Arguments.of("normal `missing end tag", "normal `missing end tag"),
                Arguments.of("normal `missing end tag\nnew line`dangling tag",
                        "normal `missing end tag\nnew line`dangling tag")
        );
    }

    @ParameterizedTest
    @MethodSource("provideForSubscript")
    void testSubscript(String original, String expected) {
        // given
        Parser parser = new Parser();

        // when
        String converted = parser.parse(original, "pageName");

        // then
        assertThat(converted).isEqualTo(expected);
    }

    private static Stream<Arguments> provideForSubscript() {
        return Stream.of(
                Arguments.of("normal ,,subscript,, normal", "normal <sub>subscript</sub> normal"),
                Arguments.of("normal,, subscript ,,normal", "normal<sub> subscript </sub>normal"),
                Arguments.of("normal ,,subscript,, normal again ,,second subscript,,",
                        "normal <sub>subscript</sub> normal again <sub>second subscript</sub>"),

                Arguments.of("normal ,,missing end tag", "normal ,,missing end tag"),
                Arguments.of("normal ,,missing end tag\nnew line,,dangling tag",
                        "normal ,,missing end tag\nnew line,,dangling tag")
        );
    }

    @ParameterizedTest
    @MethodSource("provideForSuperscript")
    void testSuperscript(String original, String expected) {
        // given
        Parser parser = new Parser();

        // when
        String converted = parser.parse(original, "pageName");

        // then
        assertThat(converted).isEqualTo(expected);
    }

    private static Stream<Arguments> provideForSuperscript() {
        return Stream.of(
                Arguments.of("normal ^superscript^ normal", "normal <sup>superscript</sup> normal"),
                Arguments.of("normal^ superscript ^normal", "normal<sup> superscript </sup>normal"),
                Arguments.of("normal ^superscript^ normal again ^second monospace^",
                        "normal <sup>superscript</sup> normal again <sup>second monospace</sup>"),

                Arguments.of("normal ^missing end tag", "normal ^missing end tag"),
                Arguments.of("normal ^missing end tag\nnew line^dangling tag",
                        "normal ^missing end tag\nnew line^dangling tag")
        );
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("provideForMixedIgnored")
    void testMixed(String original, String expected) {
        // given
        Parser parser = new Parser();

        // when
        String converted = parser.parse(original, "pageName");

        // then
        assertThat(converted).isEqualTo(expected);
    }

    private static Stream<Arguments> provideForMixedIgnored() {
        return Stream.of(
                // Mixing results in completely weird behavior
                Arguments.of("normal`should be mono^should be mono and super^`",
                        "normal''<sub>should be mono^should be mono and super^</sub>''"),
                Arguments.of("normal^super`should be mono and super`^",
                        "normal<sup>super`should be mono and super`</sup>"),
                Arguments.of("normal`should be mono,,mono and sub,,`",
                        "normal''<sub>should be mono,,mono and sub,,</sub>''"),
                Arguments.of("normal^super,,should be super and sub,,^",
                        "normal<sup>super,,should be super and sub,,</sup>")
        );
    }
}