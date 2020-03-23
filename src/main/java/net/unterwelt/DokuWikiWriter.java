package net.unterwelt;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import net.unterwelt.util.*;

public class DokuWikiWriter {

    private static final String SUFFIX = ".txt";

    private final Path dokuWikiPath;
    private final boolean addFixme;
    private final boolean cleanTags;
    private final Transliterator transliterator;

    /**
     * Create a DokuWikiWriter.
     *
     * @param dokuWikiPath the complete path to the output directory, e.g. dokuwiki/data/pages
     * @param addFixme true if "fix me" warning should be added to the pages
     */
    DokuWikiWriter(Path dokuWikiPath, boolean addFixme, boolean cleanTags) {
        this.dokuWikiPath = dokuWikiPath;
        this.addFixme = addFixme;
        this.cleanTags = cleanTags;

        this.transliterator = new Transliterator();
    }

    /**
     * Create a DokuWikiWriter.
     *
     * @param dokuWikiPath the complete path to the output directory, e.g. dokuwiki/data/pages
     * @param addFixme true if "fix me" warning should be added to the pages
     * @param newMappings Path to a file with new mappings that are added to the existing mappings
     * @throws IOException due to reading from file
     */
    DokuWikiWriter(Path dokuWikiPath, boolean addFixme, boolean cleanTags, Path newMappings) throws IOException {
        this.dokuWikiPath = dokuWikiPath;
        this.addFixme = addFixme;
        this.cleanTags = cleanTags;

        this.transliterator = new Transliterator(newMappings);
    }

    int write(List<Page> pages) throws IOException {
        int converted = 0;
        Parser parser = new Parser(addFixme, cleanTags);
        for (Page page : pages) {
            Path outputPath = createOutputPath(page);
            Charset charset = StandardCharsets.UTF_8;

            try (BufferedWriter writer = Files.newBufferedWriter(outputPath, charset)) {
                writePage(writer, page, parser);
                converted++;
            } catch (IOException x) {
                System.err.format("IOException: %s%n", x);
            }
        }

        return converted;
    }

    private Path createOutputPath(Page originalPage) throws IOException {
        StringBuilder outputPathBuilder = new StringBuilder();

        for (String superPageOriginal : originalPage.getSuperPages()) {
            String superPage = transliterator.transliterate(superPageOriginal);
            outputPathBuilder.append(superPage.toLowerCase()).append(File.separator);
        }

        String pageName = transliterator.transliterate(originalPage.getName());
        outputPathBuilder.append(pageName.toLowerCase());
        outputPathBuilder.append(SUFFIX);

        Path pagePath = dokuWikiPath.resolve(outputPathBuilder.toString());
        Files.createDirectories(pagePath.getParent());
        return Files.createFile(pagePath);
    }

    private void writePage(BufferedWriter writer, Page page, Parser parser) throws IOException {
        String parsedContent = parser.parse(page);
        writer.write(parsedContent);
        System.out.println("Written: " + page.getSuperPages() + " - " + page.getName());
    }
}
