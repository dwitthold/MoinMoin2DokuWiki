package net.unterwelt;

import static org.assertj.core.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import javax.json.*;
import org.assertj.core.util.*;
import org.junit.jupiter.api.*;

class IncompatibilityLoggerTest {

    @Test
    void testBasicLogging() {
        // given
        List<String> content = List.of("regular\n", "----\n", "below divider");
        Page page = new Page(Lists.emptyList(), "Page 1", content);

        IncompatibilityLogger logger = new IncompatibilityLogger(getJsonObject());

        // when
        String result = logger.check(List.of(page), "0.1.test", false);

        // then
        assertThat(result).isEqualTo("MoinMoin2DokuWikiWriter v0.1.test\n\n\n"
                + "'Page 1' contains the following problematic sequences:\n"
                + "  ----\n"
                + "\n\n(the sequences might not be problematic as they might e.g. be within "
                + "nowiki.tags)");
    }

    @Test
    void testPageNames() {
        // given
        List<String> content = List.of("regular\n",
                "||<rowspan=2> spanning rows||||<style=\"background-color: #E0E0FF;\"> styled -> "
                        + "warning||",
                "below table row");
        Page page = new Page(Lists.emptyList(), "Page? 2, with question", content);

        IncompatibilityLogger logger = new IncompatibilityLogger(getJsonObject());

        // when
        String result = logger.check(List.of(page), "0.1.test", false);

        // then
        assertThat(result).isEqualTo("MoinMoin2DokuWikiWriter v0.1.test\n\n\n"
                + "'Page? 2, with question' contains the following problematic sequences:\n"
                + "  ? (in page name or super pages)\n"
                + "  ||<style=\n"
                + "\n\n(the sequences might not be problematic as they might e.g. be within nowiki.tags)");
    }

    @Test
    void testSuperPages() {
        // given
        List<String> content = List.of("regular\n", "----\n", "below divider");
        Page page = new Page(List.of("super1", "with ? mark", "super3"), "Page 1", content);

        IncompatibilityLogger logger = new IncompatibilityLogger(getJsonObject());

        // when
        String result = logger.check(List.of(page), "0.1.test",false);

        // then
        assertThat(result).isEqualTo("MoinMoin2DokuWikiWriter v0.1.test\n\n\n"
                + "'super1.with ? mark.super3Page 1' contains the following problematic sequences:\n"
                + "  ? (in page name or super pages)\n"
                + "  ----\n"
                + "\n\n(the sequences might not be problematic as they might e.g. be within nowiki.tags)");
    }

    @Test
    void testExceptions() {
        // given
        List<String> content = List.of("regular\n",
                "||regular||||||<style=\"text-align: left;\">styled, but in "
                        + "exceptions -> no warning||", "below table row");
        Page page = new Page(Lists.emptyList(), "Page 3, with ümlaut", content);

        IncompatibilityLogger logger = new IncompatibilityLogger(getJsonObject());

        // when
        String result = logger.check(List.of(page), "0.1.test",false);

        // then
        assertThat(result).isEqualTo("MoinMoin2DokuWikiWriter v0.1.test\n"
                + "\n\n(the sequences might not be problematic as they might e.g. be within nowiki.tags)");
    }

    private JsonObject getJsonObject() {
        try (FileReader fileReader = new FileReader(getIncompatibilitiesFile())) {
            return Json.createReader(fileReader).readObject();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private File getIncompatibilitiesFile() {
        URL resource = this.getClass().getClassLoader().getResource("incompatibilities" + ".json");
        if (resource == null) {
            throw new IllegalStateException("incompatibilities.json not found in resources");
        }
        return Paths.get(resource.getPath()).toFile();
    }
}
