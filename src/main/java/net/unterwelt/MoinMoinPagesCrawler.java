package net.unterwelt;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * Read the current versions of all the MoinMoin pages found in the given directory.
 *
 * <p>The page names will be converted to UTF-8. The page contents however are stored "as is", i.e.
 * the content will not be parsed. Line breaks will be preserved.
 */
public class MoinMoinPagesCrawler {

    private Charset inputCharset;

    private final Path moinMoinDir;

    /**
     * Create a MoinMoinPagesCrawler.
     *
     * @param moinMoinDir the MoinMoin pages directory
     * @param inputCharset the charset for the file contents
     */
    MoinMoinPagesCrawler(Path moinMoinDir, Charset inputCharset) {
        this.moinMoinDir = moinMoinDir;
        this.inputCharset = inputCharset;
    }

    /**
     * Create a Page for each MoinMoin page that has content.
     *
     * @return the list of Pages
     */
    List<Page> crawl() {
        File moinMoinFile = moinMoinDir.toFile();
        if (!moinMoinFile.exists()) {
            System.out.println("Error: Could not find " + moinMoinFile.getAbsolutePath());
        }

        List<File> subDirs = getSubDirs(moinMoinFile);
        return createPagesFromSubDirs(subDirs);
    }

    private List<File> getSubDirs(File currentDir) {
        File[] subDirs = currentDir.listFiles();
        if (subDirs == null) {
            return Collections.emptyList();
        }

        List<File> subDirsSorted = Arrays.asList(subDirs);
        subDirsSorted.sort(Comparator.comparing(Object::toString));
        return subDirsSorted;
    }

    private List<Page> createPagesFromSubDirs(List<File> subDirs) {
        List<Page> pages = new ArrayList<>();

        for (File subDir : subDirs) {
            if (!subDir.isDirectory()) {
                System.out.println(
                        String.format("Skipped (not a directory): %s", subDir.getName()));
                continue;
            }

            List<String> pageContentWithLineBreaks = getPageContent(subDir);
            if (pageContentWithLineBreaks.size() == 0) {
                System.out.println(String.format("Skipped (no content): %s", subDir.getName()));
                continue;
            }
            List<String> pathElements = getElementsFromPath(subDir);

            pages.add(createPageFromPathElements(pathElements, pageContentWithLineBreaks));
        }

        return pages;
    }

    private List<String> getPageContent(File subDir) {
        try {
            Path currentRevision = getPathToCurrentRevision(subDir);
            if (currentRevision == null || !currentRevision.toFile().exists()) {
                System.out.println(subDir.toString() + " NOT FOUND (page might be deleted)");
                return Collections.emptyList();
            }

            return Files.readAllLines(currentRevision, inputCharset)
                    .stream().map(s -> s + "\n").collect(Collectors.toList());  // add line break
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            System.out.println("File 'current' in " + subDir + " has no content.");
        }

        return Collections.emptyList();
    }

    private Path getPathToCurrentRevision(File subDir) throws IOException {
        try {
            String revisionNumber = Files.readAllLines(subDir.toPath().resolve("current")).get(0);
            return Paths.get(subDir.getAbsolutePath(), "revisions", revisionNumber);
        } catch (NoSuchFileException e) {
            System.out.println(subDir.toString() + " NOT FOUND (page might be deleted)");
            return null;
        }
    }

    private List<String> getElementsFromPath(File subDir) {
        String decodedLongName = decodeParentheses(subDir.getName());
        String[] elements = decodedLongName.split("/");
        return Arrays.asList(elements);
    }

    /**
     * Decode the elements characters between parentheses.
     *
     * <p>MoinMoin unfortunately puts adjoining UTF-8 encodings within the same parentheses so
     * simple replacing and decoding won't work.
     *
     * @param element the element in MoinMoin's format
     * @return the element's content as a regular string
     */
    private String decodeParentheses(String element) {
        StringBuilder decodedName = new StringBuilder();

        for (int i = 0; i < element.length(); i++) {
            int parenthesisStart = element.indexOf('(', i);
            if (parenthesisStart < 0) {
                decodedName.append(element.substring(i));
                break;  // no more ( in element
            }

            int parenthesisEnd = element.indexOf(')', parenthesisStart);
            if (parenthesisEnd < 0) {
                throw new IllegalArgumentException("Opening parenthesis without closing " +
                        "parenthesis found in '" + element + "'");
            }

            String beforeParenthesis = element.substring(i, parenthesisStart);
            decodedName.append(beforeParenthesis);
            String inParentheses = element.substring(parenthesisStart + 1, parenthesisEnd);
            decodedName.append(decode(inParentheses));

            i = parenthesisEnd;
        }
        return decodedName.toString();
    }

    private String decode(String element) {
        StringBuilder urlEncoded = new StringBuilder();
        for (int i = 0; i < element.length(); i += 2) {
            urlEncoded.append("%");
            urlEncoded.append(element, i, i + 2);
        }
        return URLDecoder.decode(urlEncoded.toString(), inputCharset);
    }

    private Page createPageFromPathElements(List<String> pathElements, List<String> pageContent) {
        String pageName = pathElements.get(pathElements.size() - 1);
        List<String> superPages = pathElements.subList(0, pathElements.size() - 1);
        Page page = new Page(superPages, pageName, pageContent);

        System.out.println(
                String.format("Created: %s - %s",page.getSuperPages(), page.getName()));

        return page;
    }
}

