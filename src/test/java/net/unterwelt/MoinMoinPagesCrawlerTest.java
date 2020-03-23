package net.unterwelt;

import static org.assertj.core.api.Assertions.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.*;

class MoinMoinPagesCrawlerTest {
    Path testDir = Paths.get("testDirForCrawler");
    Path pathToMoinMoinPages = testDir.resolve(Paths.get("MoinMoin", "wiki", "data", "pages"));
    Charset inputCharset = Charset.defaultCharset();

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
    void testBasicPage() {
        // given
        PageDirBuilder.create(pathToMoinMoinPages, "basic_page1").build();
        PageDirBuilder.create(pathToMoinMoinPages, "basic_page2").build();

        // when
        List<Page> readPages = new MoinMoinPagesCrawler(pathToMoinMoinPages, inputCharset).crawl();

        // then
        assertThat(readPages.size()).isEqualTo(2);

        Page page1 = readPages.get(0);
        assertThat(page1.getName()).isEqualTo("basic_page1");
        assertThat(page1.getSuperPages().size()).isEqualTo(0);
        assertThat(page1.getContent().size()).isEqualTo(2);
        assertThat(page1.getContent().get(0)).isEqualTo("test line 1\n");
        assertThat(page1.getContent().get(1)).isEqualTo("test line 2\n");

        Page page2 = readPages.get(1);
        assertThat(page2.getName()).isEqualTo("basic_page2");
        assertThat(page2.getSuperPages().size()).isEqualTo(0);
        assertThat(page2.getContent().size()).isEqualTo(2);
        assertThat(page2.getContent().get(0)).isEqualTo("test line 1\n");
        assertThat(page2.getContent().get(1)).isEqualTo("test line 2\n");
    }

    @Test
    void testRevisions() {
        // given
        PageDirBuilder.create(pathToMoinMoinPages, "revision_page1").withRevisionNumber(2)
                .withContent("revision 1 line A\nrevision 1 line B").build();
        PageDirBuilder.create(pathToMoinMoinPages, "revision_page2").withRevisionNumber(11)
                .withContent("revision 2 line A\nrevision 2 line B").build();

        // when
        List<Page> readPages = new MoinMoinPagesCrawler(pathToMoinMoinPages, inputCharset).crawl();

        // then
        assertThat(readPages.size()).isEqualTo(2);

        Page page1 = readPages.get(0);
        assertThat(page1.getName()).isEqualTo("revision_page1");
        assertThat(page1.getSuperPages().size()).isEqualTo(0);
        assertThat(page1.getContent().size()).isEqualTo(2);
        assertThat(page1.getContent().get(0)).isEqualTo("revision 1 line A\n");
        assertThat(page1.getContent().get(1)).isEqualTo("revision 1 line B\n");

        Page page2 = readPages.get(1);
        assertThat(page2.getName()).isEqualTo("revision_page2");
        assertThat(page2.getSuperPages().size()).isEqualTo(0);
        assertThat(page2.getContent().size()).isEqualTo(2);
        assertThat(page2.getContent().get(0)).isEqualTo("revision 2 line A\n");
        assertThat(page2.getContent().get(1)).isEqualTo("revision 2 line B\n");
    }

    @Test
    void testSuperPages() {
        // given
        PageDirBuilder.create(pathToMoinMoinPages, "sub_page1")
                .withSuperPages("super_page1").build();
        PageDirBuilder.create(pathToMoinMoinPages, "sub_page2")
                .withSuperPages("super_page1").build();
        PageDirBuilder.create(pathToMoinMoinPages, "sub_sub_page")
                .withSuperPages("super_page1", "middle(20)page").build();

        // when
        List<Page> readPages = new MoinMoinPagesCrawler(pathToMoinMoinPages, inputCharset).crawl();

        // then
        assertThat(readPages.size()).isEqualTo(3);

        Page page1 = readPages.get(1);
        assertThat(page1.getName()).isEqualTo("sub_page1");
        assertThat(page1.getSuperPages().size()).isEqualTo(1);
        assertThat(page1.getSuperPages().get(0)).isEqualTo("super_page1");
        assertThat(page1.getContent().size()).isEqualTo(2);
        assertThat(page1.getContent().get(0)).isEqualTo("test line 1\n");
        assertThat(page1.getContent().get(1)).isEqualTo("test line 2\n");

        Page page2 = readPages.get(2);
        assertThat(page2.getName()).isEqualTo("sub_page2");
        assertThat(page2.getSuperPages().size()).isEqualTo(1);
        assertThat(page2.getSuperPages().get(0)).isEqualTo("super_page1");
        assertThat(page2.getContent().size()).isEqualTo(2);
        assertThat(page2.getContent().get(0)).isEqualTo("test line 1\n");
        assertThat(page2.getContent().get(1)).isEqualTo("test line 2\n");

        Page page3 = readPages.get(0);
        assertThat(page3.getName()).isEqualTo("sub_sub_page");
        assertThat(page3.getSuperPages().size()).isEqualTo(2);
        assertThat(page3.getSuperPages().get(0)).isEqualTo("super_page1");
        assertThat(page3.getSuperPages().get(1)).isEqualTo("middle page");
        assertThat(page3.getContent().size()).isEqualTo(2);
        assertThat(page3.getContent().get(0)).isEqualTo("test line 1\n");
        assertThat(page3.getContent().get(1)).isEqualTo("test line 2\n");
    }

    @Test
    void testEncoding() {
        // given
        PageDirBuilder.create(pathToMoinMoinPages,
                "Guts(20)Pie(20)Earshot(202d20)Smart(20)Desert").build();
        PageDirBuilder.create(pathToMoinMoinPages,
                "Die(20c384)rzte(202d20)Rock(27)n(27)Roll(2dc39c)bermensch").build();
        PageDirBuilder.create(pathToMoinMoinPages, "regular(28)inParentheses(29)regular2").build();
        PageDirBuilder.create(pathToMoinMoinPages, "The(20)Electric(20)Caf(c3a92f)Invitation").build();

        // when
        List<Page> readPages = new MoinMoinPagesCrawler(pathToMoinMoinPages, inputCharset).crawl();

        // then
        assertThat(readPages.size()).isEqualTo(4);

        Page aerztePage = readPages.get(0);
        assertThat(aerztePage.getSuperPages().size()).isEqualTo(0);
        assertThat(aerztePage.getName()).isEqualTo("Die Ärzte - Rock'n'Roll-Übermensch");

        Page gutsPieEarshotPage = readPages.get(1);
        assertThat(gutsPieEarshotPage.getSuperPages().size()).isEqualTo(0);
        assertThat(gutsPieEarshotPage.getName()).isEqualTo("Guts Pie Earshot - Smart Desert");

        Page electricCafePage = readPages.get(2);
        assertThat(electricCafePage.getSuperPages().size()).isEqualTo(1);
        assertThat(electricCafePage.getName()).isEqualTo("Invitation");

        Page parenthesesPage = readPages.get(3);
        assertThat(parenthesesPage.getSuperPages().size()).isEqualTo(0);
        assertThat(parenthesesPage.getName()).isEqualTo("regular(inParentheses)regular2");
    }
}