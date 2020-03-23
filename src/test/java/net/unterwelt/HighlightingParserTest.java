package net.unterwelt;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

public class HighlightingParserTest {

    @ParameterizedTest
    @MethodSource("provideForBasic")
    void testHighlighting(String original, String expected) {
        // given
        Parser parser = new Parser();

        // when
        String converted = parser.parse(original, "pageName");

        // then
        assertThat(converted).isEqualTo(expected);
    }

    private static Stream<Arguments> provideForBasic() {
        return Stream.of(
                // basic
                Arguments.of("{{{#!highlight java\n"
                                + "public enum MainOption {\n"
                                + "   CHARSET, MAPPING_FILE\n"
                                + "}\n"
                                + "}}}",
                        "<code java>\n"
                                + "public enum MainOption {\n"
                                + "   CHARSET, MAPPING_FILE\n"
                                + "}\n"
                                + "</code>"),
                Arguments.of("{{{#!highlight python\n"
                                + "def hello():\n"
                                + "    print \"Hello World!\"\n"
                                + "}}}",
                        "<code python>\n"
                                + "def hello():\n"
                                + "    print \"Hello World!\"\n"
                                + "</code>"),

                // ignored
                Arguments.of("{{{ #!highlight python\n"  // intermittent space
                                + "def hello():\n"
                                + "    print \"Hello World!\"\n"
                                + "}}}",
                        "{{{ #!highlight python\n"
                                + "def hello():\n"
                                + "    print \"Hello World!\"\n"
                                + "}}}"),

                // weird
                Arguments.of("{{{#!highlight python this is ignored\n"
                                + "def hello():\n"
                                + "    print \"Hello World!\"\n"
                                + "}}}",
                        "<code python>\n"
                                + "def hello():\n"
                                + "    print \"Hello World!\"\n"
                                + "</code>"),

                // long
                Arguments.of("normal line 1\n"
                                + "{{{#!highlight java\n"
                                + "public enum MainOption {\n"
                                + "   CHARSET, MAPPING_FILE\n"
                                + "}\n"
                                + "}}}\n"
                                + "normal line 2}}}\""
                                + "{{{#!highlight python\n"
                                + "def hello():\n"
                                + "    print \"Hello World!\"\n"
                                + "}}}"
                                + "normal line 3\"",
                        "normal line 1\n"
                                + "<code java>\n"
                                + "public enum MainOption {\n"
                                + "   CHARSET, MAPPING_FILE\n"
                                + "}\n"
                                + "</code>\n"
                                + "normal line 2}}}\""
                                + "<code python>\n"
                                + "def hello():\n"
                                + "    print \"Hello World!\"\n"
                                + "</code>"
                                + "normal line 3\"")
        );
    }
}
