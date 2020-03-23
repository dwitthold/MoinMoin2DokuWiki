package net.unterwelt;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

public class LineStartParserTest {

    @ParameterizedTest
    @MethodSource("provideForSimpleList")
    void testSimpleList(String original, String expected) {
        // given
        Parser parser = new Parser();

        // when
        String converted = parser.parse(original, "pageName");

        // then
        assertThat(converted).isEqualTo(expected);
    }

    private static Stream<Arguments> provideForSimpleList() {
        return Stream.of(
                Arguments.of("line", "line"),
                Arguments.of(" * element1", "  * element1"),
                Arguments.of("  * element2", "    * element2"),
                Arguments.of("* line with asterisk", "* line with asterisk")
        );
    }

    @ParameterizedTest
    @MethodSource("provideForNumeratedList")
    void testNumeratedList(String original, String expected) {
        // given
        Parser parser = new Parser();

        // when
        String converted = parser.parse(original, "pageName");

        // then
        assertThat(converted).isEqualTo(expected);
    }

    private static Stream<Arguments> provideForNumeratedList() {
        return Stream.of(
                Arguments.of("line", "line"),
                Arguments.of(" 1. element1", "  - element1"),
                Arguments.of("  i. element2", "    - element2"),
                Arguments.of("  a. element2", "    - element2"),
                Arguments.of("1. line with number", "1. line with number"),
                Arguments.of(" 1 line without dot -> keep indentation",
                        " 1 line without dot -> keep indentation")
        );
    }

    @ParameterizedTest
    @MethodSource("provideForIndented")
    void testIndented(String original, String expected) {
        // given
        Parser parser = new Parser();

        // when
        String converted = parser.parse(original, "pageName");

        // then
        assertThat(converted).isEqualTo(expected);
    }

    private static Stream<Arguments> provideForIndented() {
        // indentation level of two or greater results in block quote, but is kept anyway
        return Stream.of(
                Arguments.of("line", "line"),
                Arguments.of(" indentation 1", " indentation 1"),
                Arguments.of("  indentation 2", "  indentation 2"),
                Arguments.of("   indentation 3", "   indentation 3")
        );
    }
}

