package net.unterwelt;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

public class WholeLineParserTest {
    @ParameterizedTest
    @MethodSource("provideForHeading")
    void testHeading(String original, String expected) {
        // given
        Parser parser = new Parser();

        // when
        String converted = parser.parse(original, "pageName");

        // then
        assertThat(converted).isEqualTo(expected);
    }

    private static Stream<Arguments> provideForHeading() {
        return Stream.of(
                // syntax
                Arguments.of("line", "line"),
                Arguments.of("= regular =", "= regular ="),
                Arguments.of("== H1 ==", "====== H1 ======"),
                Arguments.of(" == space at start -> indentation ==",
                        " == space at start -> indentation =="),
                Arguments.of("a== letter at start ==", "a== letter at start =="),
                Arguments.of("== space at end == ", "== space at end == "),
                Arguments.of("== letter at end ==a", "== letter at end ==a"),
                Arguments.of("==missing space at start ==", "==missing space at start =="),
                Arguments.of("== missing space at end==", "== missing space at end=="),
                Arguments.of("== H1 == with equals signs ==", "====== H1 == with equals signs ======"),
                Arguments.of("=== missing equal at end ==", "=== missing equal at end =="),
                Arguments.of("== too many equals at end ===", "== too many equals at end ==="),

                // heading levels
                Arguments.of("== H1 ==", "====== H1 ======"),
                Arguments.of("=== H2 ===", "===== H2 ====="),
                Arguments.of("==== H3 ====", "==== H3 ===="),
                Arguments.of("===== H4 =====", "=== H4 ==="),
                Arguments.of("====== H5 ======", "== H5 =="),
                Arguments.of("======= H6 =======", "== H6 =="),
                Arguments.of("======== H7 ========", "== H7 ==")
        );
    }
}
