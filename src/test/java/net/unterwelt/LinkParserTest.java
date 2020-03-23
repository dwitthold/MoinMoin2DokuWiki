package net.unterwelt;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

public class LinkParserTest {

    @ParameterizedTest
    @MethodSource("provideForLinks")
    void testLinks(String original, String expected) {
        // given
        Parser parser = new Parser();

        // when
        String converted = parser.parse(original, "pageName");

        // then
        assertThat(converted).isEqualTo(expected);
    }

    private static Stream<Arguments> provideForLinks() {
        return Stream.of(
                // INTERNAL
                Arguments.of("[[My Page]]", "[[:My Page]]"),
                Arguments.of("some text [[My Page]] more text", "some text [[:My Page]] more text"),
                Arguments.of("some text[[My Page]]more text", "some text[[:My Page]]more text"),
                Arguments.of("[[My Page|named link]]", "[[:My Page|named link]]"),
                Arguments.of("[[My Page | named link]]", "[[:My Page | named link]]"),

                Arguments.of("[[/My Subpage]]", "[[.:pageName:My Subpage]]"),
                Arguments.of("[[/Subpage/Sub-Subpage]]", "[[.:pageName:Subpage:Sub-Subpage]]"),
                Arguments.of("[[../My sibling page]]", "[[.:pageName:..:My sibling page]]"),
                Arguments.of("[[../sibling/My niece page]]",
                        "[[.:pageName:..:sibling:My niece page]]"),
                Arguments.of(
                        "some text [[SomePage#subsection|subsection of Some Page]] more text",
                        "some text [[:SomePage#subsection|subsection of Some Page]] more text"
                ),

                Arguments.of("The ]]tags are[[ switched.",
                        "The ]]tags are[[ switched."),

                Arguments.of("[[I am a link with a - character]]",
                        "[[:I am a link with a - character]]"),
                Arguments.of("[[I am a link with a .character]]",
                        "[[:I am a link with a .character]]"),

                // not possible in DokuWiki
                Arguments.of("[[I am a link with a ?character]]",
                        "[[:I am a link with a _character]]"),
                Arguments.of("[[I am a link with a :character]]",
                        "[[:I am a link with a _character]]"),

                // no corresponding concept in Dokuwiki: anchors -> keep anchor as hint for fixing
                Arguments.of(
                        "some text [[SomePage#anchor|subsection of Some Page]] more text",
                        "some text [[:SomePage#anchor|subsection of Some Page]] more text"
                ),

                // EXTERNAL
                Arguments.of("http://example.net/", "http://example.net/"),
                Arguments.of("[[http://example.net/]]", "[[http://example.net/]]"),

                Arguments.of("[[http://example.net|named external link]]",
                        "[[http://example.net|named external link]]"),

                // include external image
                Arguments.of("{{http://static.moinmo.in/logos/moinmoin.png}}",
                        "{{http://static.moinmo.in/logos/moinmoin.png}}"),
                Arguments.of("{{http://static.moinmo.in/logos/moinmoin.png|with alt text}}",
                        "{{http://static.moinmo.in/logos/moinmoin.png|with alt text}}"),

                // should not result in a link (missing / at the end), but there seems to be no
                // easy way to prevent the linking
                Arguments.of("http://example.net", "http://example.net"),

                // MISC.
                Arguments.of("some '''bold text [[My Page]] more bold''' text",
                        "some **bold text [[:My Page]] more bold** text"),
                Arguments.of("some '''bold text [[My '''normal''' Page]] more bold''' text",
                        "some **bold text [[:My '''normal''' Page]] more bold** text"),

                Arguments.of("some {{{unformatted text [[My Page]] more bold}}} text",
                        "some <nowiki>unformatted text [[My Page]] more bold</nowiki> text"),
                Arguments.of("some '''bold text [[My {{{normal}}} Page]] more bold''' text",
                        "some **bold text [[:My {{{normal}}} Page]] more bold** text")
        );
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("provideForLinksIgnored")
    void testLinksIgnored(String original, String expected) {
        // given
        Parser parser = new Parser();

        // when
        String converted = parser.parse(original, "pageName");

        // then
        assertThat(converted).isEqualTo(expected);
    }

    private static Stream<Arguments> provideForLinksIgnored() {
        return Stream.of(
                // INTERNAL
                // becomes a link in DokuWiki, but should just remove the line break
                Arguments.of("[[I am\n not a link]]", "[[I am not a link]]"),

                // becomes a link in DokuWiki, should preserve the line breaks
                Arguments.of("[[I am\n\n not a link]]", "[[I am\n\n not a link]]"),

                // MoinMoin's page/ file name will replace the '?' by '%3F', while the page title
                // will still display the '?'; the ? can be kept in the DokuWiki-link, but it and
                // everything afterwards will be missing from the page name (and title)
                Arguments.of("[[I am a link with a ? character]]",
                        "[[I am a link with a ? character]]"),

                // CamelCase links
                Arguments.of("some CamelCase link", "some [[CamelCase]] link"),
                Arguments.of("some !IgnoredCamelCase link", "some IgnoredCamelCase link"),
                Arguments.of("some /SubPageCamelCase link", "???"),
                Arguments.of("some ../SiblingPageCamelCase link", "???"),
                Arguments.of("some CamelCase''s link", "some [[CamelCase|CamelCases]] link"),

                // EXTERNAL
                // Possible solution might be to scan for mail-regex at each space (until the
                // following space)
                Arguments.of("info@example.net", "<info@example.net>"),

                // Seems to be working differently in DokuWiki
                Arguments.of("[[file://///server/share/filename%20with%20spaces.txt"
                        + "|link to filename.txt]]", "???"),

                // INTERWIKI
                Arguments.of("[[otherwiki:somepage]]", "[[otherwiki>somepage]]"),
                Arguments.of("Otherwiki:somepage", "[[otherwiki>somepage]]"),

                // MISC.
                // Triple '[': in MoinMoin the first [ becomes part of the page/ file name, but
                // is not displayed in the link; in DokuWiki [ and ] become part of the named
                // link, but not the link itself
                Arguments.of("text[[my link]]]", "???"),
                Arguments.of("text[[my link]]", "???")
        );
    }
}
