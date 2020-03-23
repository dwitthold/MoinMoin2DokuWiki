package net.unterwelt;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

public class WeirdEndTagRuleTest {

    @ParameterizedTest
    @MethodSource("provideForStrikeThrough")
    void testStrikeThrough(String original, String expected) {
        // given
        Parser parser = new Parser();

        // when
        String converted = parser.parse(original, "pageName");

        // then
        assertThat(converted).isEqualTo(expected);
    }

    private static Stream<Arguments> provideForStrikeThrough() {
        return Stream.of(
                // regular cases
                Arguments.of("normal --(strike through)-- normal",
                        "normal <del>strike through</del> normal"),
                Arguments.of("normal )--switched tags --(strikethrough started",
                        "normal )--switched tags <del>strikethrough started</del>"),

                // weird cases
                Arguments.of("normal --(strike through\n"
                        + "new line: regular again)-- end tag ignored",
                        "normal <del>strike through</del>\n"
                        + "new line: regular again end tag ignored"),
                Arguments.of("normal --(strike through\n"
                        + "new line: regular again --(start tag displayed)-- end tag ignored",
                        "normal <del>strike through</del>\n"
                        + "new line: regular again --(start tag displayed end tag ignored"),
                Arguments.of("normal --(strike through\n"
                        + "regular --(start tag displayed--(2nd start tag displayed)-- end tag ignored",
                        "normal <del>strike through</del>\n"
                        + "regular --(start tag displayed--(2nd start tag displayed end tag ignored"),
                Arguments.of("normal --(strike through\n"
                        + "regular --(start tag displayed)-- end tag ignored"
                                + "--(strike through again)--regular",
                        "normal <del>strike through</del>\n"
                        + "regular --(start tag displayed end tag ignored"
                                + "<del>strike through again</del>regular")
                );
    }

    @ParameterizedTest
    @MethodSource("provideForUnderlined")
    void testUnderlined(String original, String expected) {
        // given
        Parser parser = new Parser();

        // when
        String converted = parser.parse(original, "pageName");

        // then
        assertThat(converted).isEqualTo(expected);
    }

    private static Stream<Arguments> provideForUnderlined() {
        return Stream.of(
                // regular cases
                Arguments.of("normal __underlined__ normal",
                        "normal __underlined__ normal"),

                // weird cases
                Arguments.of("normal __underlined\n"
                        + "new line: regular again__ end tag ignored",
                        "normal __underlined__\n"
                        + "new line: regular again end tag ignored"),
                Arguments.of("normal __strike through\n"
                        + "new line: regular again __tag ignored __strikethrough again",
                        "normal __strike through__\n"
                        + "new line: regular again tag ignored __strikethrough again__"),
                Arguments.of("normal __strike through\n"
                        + "regular __1st tag ignored__2nd tag: strikethrough__ regular",
                        "normal __strike through__\n"
                        + "regular 1st tag ignored__2nd tag: strikethrough__ regular")
                );
    }
}
