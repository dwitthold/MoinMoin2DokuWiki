package net.unterwelt;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

public class NoFormattingParserTest {

    @ParameterizedTest
    @MethodSource("provideForNoWiki")
    void testNoWiki(String original, String expected) {
        // given
        Parser parser = new Parser();

        // when
        String converted = parser.parse(original, "pageName");

        // then
        assertThat(converted).isEqualTo(expected);
    }

    private static Stream<Arguments> provideForNoWiki() {
        return Stream.of(
                // nowiki in single line
                Arguments.of("normal{{{unformatted'''tag is written as is''' unformatted}}}normal",
                        "normal<nowiki>unformatted'''tag is written as is''' unformatted</nowiki>normal"),

                // nowiki in single line with formatted text before and after
                Arguments.of("normal'''bold'''normal{{{unformatted}}}normal'''bold'''normal",
                        "normal**bold**normal<nowiki>unformatted</nowiki>normal**bold**normal"),

                // nowiki in single line with formatted text started before it
                Arguments.of("normal '''bold{{{unformatted'''still unformatted}}}still bold'''normal",
                        "normal **bold**<nowiki>unformatted'''still unformatted</nowiki>**still bold**normal"),

                // nowiki spread over two lines
                Arguments.of("normal'''bold''' normal {{{\n"
                                + "multiline started'''not bold'''}}} regular",
                        "normal**bold** normal <nowiki></nowiki>\n"
                                + "<nowiki>multiline started'''not bold'''</nowiki> regular"),

                // nowiki spread over several lines
                Arguments.of("normal'''bold''' normal {{{\n"
                                + "multiline started'''not bold'''\n"
                                + "multiline continued''not italics''\n"
                                + "multiline last line '''still not bold'''}}} regular''italics''",
                        "normal**bold** normal <nowiki></nowiki>\n"
                                + "<nowiki>multiline started'''not bold'''</nowiki>\n"
                                + "<nowiki>multiline continued''not italics''</nowiki>\n"
                                + "<nowiki>multiline last line '''still not bold'''</nowiki> "
                                + "regular//italics//"),

                // start tag and following characters are displayed regularly
                Arguments.of("normal{{{ start tags are displayed",
                        "normal{{{ start tags are displayed"),

                // start tag and following characters are displayed regularly, next line has nowiki
                Arguments.of("normal{{{ start tags are displayed\n"
                                + "normal{{{unformatted'''still unformatted'''}}}normal",
                        "normal{{{ start tags are displayed\n"
                                + "normal<nowiki>unformatted'''still unformatted'''</nowiki>normal"),

                Arguments.of("== {{{nowiki tags are ignored}}} ==",
                        "====== {{{nowiki tags are ignored}}} ======"),

                Arguments.of("{{{== heading tags are ignored ==}}}",
                        "<nowiki>== heading tags are ignored ==</nowiki>"),

                Arguments.of("normal text {{{\n"
                                + " * ignored simple list element 1\n"
                                + " * ignored simple list element 2\n"
                                + "}}} closing tag",
                        "normal text <nowiki></nowiki>\n"
                                + "<nowiki> * ignored simple list element 1</nowiki>\n"
                                + "<nowiki> * ignored simple list element 2</nowiki>\n"
                                + "<nowiki></nowiki> closing tag")
        );
    }
}
