package net.unterwelt;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.*;

class DokuWikiWriterTest {

    private static final Path testDir = Paths.get("testDirForWriter");
    private static final Path pathToDokuWiki = testDir.resolve(
            Paths.get("dokuwiki", "data", "pages"));

    @BeforeEach
    void setUp() {
        deleteTestDir();
    }

    @AfterEach
    void tearDown() {
        deleteTestDir();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteTestDir() {
        try {
            Files.walk(testDir)
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
    void testWriteBasic() throws IOException {
        // given
        Page page1 = new Page(new ArrayList<>(), "testpage1", List.of("line1\n", "line2\n"));
        Page page2 = new Page(new ArrayList<>(), "testpage2", List.of("line A\n", "line b\n"));
        List<Page> pages = List.of(page1, page2);

        // when
        DokuWikiWriter writer = new DokuWikiWriter(pathToDokuWiki, false, false);
        writer.write(pages);

        // then
        Path dokuWikiPages = pathToDokuWiki;
        assertTrue(dokuWikiPages.toFile().exists());
        File[] files = dokuWikiPages.toFile().listFiles();
        assertNotNull(files);
        assertThat(files.length).isEqualTo(2);

        Path file1 = dokuWikiPages.resolve("testpage1.txt");
        assertTrue(file1.toFile().exists());
        List<String> content1 = Files.readAllLines(file1);
        assertThat(content1.size()).isEqualTo(2);
        assertThat(content1.get(0)).isEqualTo("line1");
        assertThat(content1.get(1)).isEqualTo("line2");

        Path file2 = dokuWikiPages.resolve("testpage2.txt");
        assertTrue(file2.toFile().exists());
        List<String> content2 = Files.readAllLines(file2);
        assertThat(content2.size()).isEqualTo(2);
        assertThat(content2.get(0)).isEqualTo("line A");
        assertThat(content2.get(1)).isEqualTo("line b");
    }

    @Test
    void testWriteBasicWithFixme() throws IOException {
        // given
        Page page1 = new Page(new ArrayList<>(), "testpage1", List.of("line1\n", "line2\n"));
        Page page2 = new Page(new ArrayList<>(), "testpage2", List.of("line A\n", "line b\n"));
        List<Page> pages = List.of(page1, page2);

        // when
        DokuWikiWriter writer = new DokuWikiWriter(pathToDokuWiki, true, false);
        writer.write(pages);

        // then
        Path dokuWikiPages = pathToDokuWiki;
        assertTrue(dokuWikiPages.toFile().exists());
        File[] files = dokuWikiPages.toFile().listFiles();
        assertNotNull(files);
        assertThat(files.length).isEqualTo(2);

        Path file1 = dokuWikiPages.resolve("testpage1.txt");
        assertTrue(file1.toFile().exists());
        List<String> content1 = Files.readAllLines(file1);
        assertThat(content1.size()).isEqualTo(4);
        assertThat(content1.get(0)).isEqualTo("FIXME **//imported from MoinMoin//**");
        assertThat(content1.get(1)).isEqualTo("");
        assertThat(content1.get(2)).isEqualTo("line1");
        assertThat(content1.get(3)).isEqualTo("line2");

        Path file2 = dokuWikiPages.resolve("testpage2.txt");
        assertTrue(file2.toFile().exists());
        List<String> content2 = Files.readAllLines(file2);
        assertThat(content2.size()).isEqualTo(4);
        assertThat(content2.get(0)).isEqualTo("FIXME **//imported from MoinMoin//**");
        assertThat(content1.get(1)).isEqualTo("");
        assertThat(content2.get(2)).isEqualTo("line A");
        assertThat(content2.get(3)).isEqualTo("line b");
    }

    @Test
    void testWriteWithSuperpages() throws IOException {
        // given
        Page page1 = new Page(List.of("super"), "testpage1", List.of("line1\n", "line2\n"));
        Page page2 = new Page(List.of("super", "middle"),
                "testpage2", List.of("line A\n", "line b\n"));
        List<Page> pages = List.of(page1, page2);

        // when
        DokuWikiWriter writer = new DokuWikiWriter(pathToDokuWiki, false, false);
        writer.write(pages);

        // then
        Path dokuWikiPages = pathToDokuWiki;
        assertTrue(dokuWikiPages.toFile().exists());
        File[] filesInPagesDir = dokuWikiPages.toFile().listFiles();
        assertNotNull(filesInPagesDir);
        assertThat(filesInPagesDir.length).as("Only directory called 'super' should be found").isEqualTo(1);

        Path superPage = dokuWikiPages.resolve("super");
        File[] filesInSuperPageDir = superPage.toFile().listFiles();
        assertNotNull(filesInSuperPageDir);
        assertThat(filesInSuperPageDir.length)
                .as("File testpage1 and directory 'middle' should be found").isEqualTo(2);

        Path file1 = superPage.resolve("testpage1.txt");
        assertTrue(file1.toFile().exists());
        List<String> content1 = Files.readAllLines(file1);
        assertThat(content1.size()).isEqualTo(2);
        assertThat(content1.get(0)).isEqualTo("line1");
        assertThat(content1.get(1)).isEqualTo("line2");

        Path file2 = superPage.resolve("middle").resolve("testpage2.txt");
        assertTrue(file2.toFile().exists());
        List<String> content2 = Files.readAllLines(file2);
        assertThat(content2.size()).isEqualTo(2);
        assertThat(content2.get(0)).isEqualTo("line A");
        assertThat(content2.get(1)).isEqualTo("line b");
    }

    @Test
    void testWriteWithUmlautsAndSuperPages() throws IOException {
        // given
        Page page1 = new Page(List.of("süper"), "Ä test page",
                List.of("line1\n", "line2\n", "Änother line\n"));
        Page page2 = new Page(List.of("süper", "middle"), "Anöther test page",
                List.of("line A\n", "line b\n"));
        List<Page> pages = List.of(page1, page2);

        // when
        DokuWikiWriter writer = new DokuWikiWriter(pathToDokuWiki, false, false);
        writer.write(pages);

        // then
        Path dokuWikiPages = pathToDokuWiki;
        assertTrue(dokuWikiPages.toFile().exists());
        File[] filesInPagesDir = dokuWikiPages.toFile().listFiles();
        assertNotNull(filesInPagesDir);
        assertThat(filesInPagesDir.length).as("Only directory called 'super' should be found").isEqualTo(1);

        Path sueperPage = dokuWikiPages.resolve("sueper");
        File[] filesInSuperPageDir = sueperPage.toFile().listFiles();
        assertNotNull(filesInSuperPageDir);
        assertThat(filesInSuperPageDir.length)
                .as("File testpage1 and directory 'middle' should be found").isEqualTo(2);

        Path file1 = sueperPage.resolve("Ae_test_page.txt".toLowerCase());
        assertTrue(file1.toFile().exists());
        List<String> content1 = Files.readAllLines(file1);
        assertThat(content1.size()).isEqualTo(3);
        assertThat(content1.get(0)).isEqualTo("line1");
        assertThat(content1.get(1)).isEqualTo("line2");
        assertThat(content1.get(2)).isEqualTo("Änother line");

        Path file2 = sueperPage.resolve("middle").resolve("Anoether_test_page.txt".toLowerCase());
        assertTrue(file2.toFile().exists());
        List<String> content2 = Files.readAllLines(file2);
        assertThat(content2.size()).isEqualTo(2);
        assertThat(content2.get(0)).isEqualTo("line A");
        assertThat(content2.get(1)).isEqualTo("line b");
    }
}