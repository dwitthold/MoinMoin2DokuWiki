package net.unterwelt;

import java.io.*;
import java.nio.file.*;

class PageDirBuilder {

    private static final String MOINMOIN_PATH_SEPARATOR = "(2f)";  // literal, not regex
    private Path pathToPagesDir;
    private String pageName;
    private int revisionNumber;
    private String content;
    private String[] superPages;

    private PageDirBuilder(Path pathToPagesDir, String pageName) {
        this.pathToPagesDir = pathToPagesDir;
        this.pageName = pageName;
        revisionNumber = 1;
        content = "test line 1\ntest line 2";
        superPages = new String[0];
    }

    static PageDirBuilder create(Path pathToPagesDir, String pageName) {
        return new PageDirBuilder(pathToPagesDir, pageName);
    }

    PageDirBuilder withRevisionNumber(int revisionNumber) {
        this.revisionNumber = revisionNumber;
        return this;
    }

    PageDirBuilder withContent(String content) {
        this.content = content;
        return this;
    }

    PageDirBuilder withSuperPages(String... superPages) {
        this.superPages = superPages;
        return this;
    }

    boolean build() {
        boolean success = false;
        try {
            StringBuilder superPagesPath = new StringBuilder();
            for (String superPage : superPages) {
                superPagesPath.append(superPage).append(MOINMOIN_PATH_SEPARATOR);
            }
            Path pageDir = pathToPagesDir.resolve(superPagesPath + pageName);
            createCurrent(pageDir);
            createRevisions(pageDir);
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    private void createCurrent(Path pageDir) throws IOException {
        Files.createDirectories(pageDir);
        if (!pageDir.toFile().exists()) {
            System.out.println("Could not create directory " + pageDir.toString());
        }

        Path current = pageDir.resolve("current");
        if (Files.notExists(current)) {
            Files.createFile(current);
        }
        String currentNumber = String.format("%08d", revisionNumber);
        Files.write(current, currentNumber.getBytes());
    }

    private void createRevisions(Path pageDir) throws IOException {
        Path revisionsDir = pageDir.resolve("revisions");
        if (Files.notExists(revisionsDir)) {
            Files.createDirectory(revisionsDir);
        }

        for (int i = 1; i <= revisionNumber; i++) {
            String revisionString = String.format("%08d", i);
            Path revisionFile = revisionsDir.resolve(revisionString);
            if (Files.notExists(revisionFile)) {
                Files.createFile(revisionFile);
            }

            String revisionContent;
            if (i < revisionNumber) {
                revisionContent = String.format("old revision content (%s)", revisionString);
            } else {
                revisionContent = content;
            }
            Files.write(revisionFile, revisionContent.getBytes());
        }
    }
}
