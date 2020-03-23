package net.unterwelt;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import java.util.stream.*;
import net.unterwelt.rules.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

class TagCleanerTest {

    @ParameterizedTest
    @MethodSource("provideForSimple")
    void testCleaning(String original, String expected) {
        // given
        List<LineContentRule> rules = List.of(new BoldRule(), new MonospacedRule());

        TagCleaner tagCleaner = new TagCleaner(rules);

        // when
        String result = tagCleaner.clean(original);

        // then
        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> provideForSimple() {
        return Stream.of(
                Arguments.of("****", ""),
                Arguments.of("foo''''bar", "foobar"),
                Arguments.of("text**''''**", "text"),
                Arguments.of("**bold** regular''''", "**bold** regular")
                );
    }
}