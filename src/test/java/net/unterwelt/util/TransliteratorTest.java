package net.unterwelt.util;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.*;

class TransliteratorTest {

    private Path testDirPath = Paths.get("testDir");

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @AfterEach
    void tearDown() {
        try {
            Files.walk(testDirPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (NoSuchFileException e) {
            // ignore
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testMappingsFile() {
        // given
        String mappings = "ä\na\né\ne";
        Path mappingsPath = testDirPath.resolve("mappings.txt");
        Transliterator transliterator;
        try {
            Files.createDirectories(testDirPath);
            Files.write(mappingsPath, mappings.getBytes());
            transliterator = new Transliterator(mappingsPath);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
            return;
        }

        // when
        String resultA = transliterator.get("ä");
        String resultE = transliterator.get("é");

        // then
        assertThat(resultA).as("default value 'ae' must have been overwritten").isEqualTo("a");
        assertThat(resultE).isEqualTo("e");
    }

    @Test
    void testMappingsFileWithBlankLine() {
        // given
        String mappings = "ä\na\né\ne\n";
        Path mappingsPath = testDirPath.resolve("mappings.txt");
        Transliterator transliterator;
        try {
            Files.createDirectories(testDirPath);
            Files.write(mappingsPath, mappings.getBytes());
            transliterator = new Transliterator(mappingsPath);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
            return;
        }

        // when
        String resultA = transliterator.get("ä");
        String resultE = transliterator.get("é");

        // then
        assertThat(resultA).as("default value 'ae' must have been overwritten").isEqualTo("a");
        assertThat(resultE).isEqualTo("e");
    }

    @Test
    void testBrokenMappingsFile() {
        // given
        String mappings = "ä\na\né\n";
        Path mappingsPath = testDirPath.resolve("mappings.txt");
        try {
            Files.createDirectories(testDirPath);
            Files.write(mappingsPath, mappings.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
            return;
        }

        // when
        assertThatThrownBy(() -> new Transliterator(mappingsPath))

        // then
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mapping file must have an even number of lines");
    }

    @Test
    void testBasicTransliterate() {
        // given
        String original = "Hello world. ça va?&Liebe Grüße!";

        // when
        String result = new Transliterator().transliterate(original);

        // then
        assertThat(result).isEqualTo("Hello_world._ca_va?_Liebe_Gruesse");
    }

    @Test
    void testTransliterateCollapse() {
        // given
        String original = "Many underscores!!! Are collapsed to one";

        // when
        String result = new Transliterator().transliterate(original);

        // then
        assertThat(result).isEqualTo("Many_underscores_Are_collapsed_to_one");
    }

    @Test
    void testTransliterateTrimmed() {
        // given
        String original = "!Leading underscore removed and trailing underscore removed!";

        // when
        String result = new Transliterator().transliterate(original);

        // then
        assertThat(result).isEqualTo("Leading_underscore_removed_and_trailing_underscore_removed");
    }
}