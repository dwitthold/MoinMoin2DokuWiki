package net.unterwelt;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

public class MoinMoin2DokuWiki {

    // general info
    static final String VERSION_NUMBER = "0.1";

    // runtime properties
    private final Properties properties = new Properties();

    private static final Path DEFAULT_DOKUWIKI_PAGES_DIR = Paths.get("dokuwiki", "data", "pages");
    private static final Path DEFAULT_MOINMOIN_DIR = Paths.get("MoinMoin", "wiki", "data", "pages");

    static final String CHARSET_PROPERTY = MainOption.CHARSET.name();
    static final String DOKUWIKI_DIR_PROPERTY = MainOption.MOINMOIN_DIR.name();
    static final String FIXME_PROPERTY = MainOption.FIXME.name();
    static final String INCOMPATIBILITIES_PROPERTY = MainOption.INCOMPATIBILITIES.name();
    static final String MAPPING_FILE_PROPERTY = MainOption.MAPPING_FILE.name();
    static final String MOINMOIN_DIR_PROPERTY = MainOption.MOINMOIN_DIR.name();
    static final String TAG_CLEANER_PROPERTY = MainOption.TAG_CLEANER.name();
    static final String RUN_PROPERTY = "RUN";

    private HashMap<String, MainOption> flags;


    MoinMoin2DokuWiki() {
        flags = new HashMap<>();
        for (MainOption option : MainOption.values()) {
            flags.put(option.getShortFlag(), option);
            flags.put(option.getLongFlag(), option);
        }

        properties.setProperty(RUN_PROPERTY, "true");
    }

    public static void main(String[] args) {
        MoinMoin2DokuWiki converter = new MoinMoin2DokuWiki();
        converter.run(args);
    }

    private void run(String[] args) {
        handleVarArgs(args);

        boolean runnable = Boolean.parseBoolean(properties.getProperty(RUN_PROPERTY));
        if (runnable) {
            convertPages();
        }
    }

    private void handleVarArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            MainOption mainOption = flags.get(arg);
            if (mainOption == null) {
                properties.setProperty(RUN_PROPERTY, "false");
                showIllegalOption(arg);
                break;
            }

            try {
                String[] followingArgs = Arrays.copyOfRange(args, i + 1, args.length);
                i = i + mainOption.handle(new PropertiesAndArgs(properties, followingArgs));
            } catch (IllegalArgumentException e) {
                showIllegalParameter(e.getMessage());
                properties.setProperty(RUN_PROPERTY, "false");
                break;
            }
        }
    }

    private void showIllegalOption(String option) {
        System.out.println("Invalid option: " + option);
        MainOption.HELP.handle(null);
    }

    private void showIllegalParameter(String message) {
        System.out.println(message);
        MainOption.HELP.handle(null);
    }

    private void convertPages() {
        System.out.println("== Starting conversion ==");
        List<Page> pages = readPages();
        checkIncompatibilities(pages);
        int converted = writePages(pages);
        System.out.println("== Converted " + converted + " pages ==");
    }

    private List<Page> readPages() {
        Path moinMoinPath = getMoinMoinPath();
        Charset inputCharset = getInputCharset();
        List<Page> pages = new MoinMoinPagesCrawler(moinMoinPath, inputCharset).crawl();
        if (pages.size() == 0) {
            System.out.println("No pages found in " + moinMoinPath);
        }
        return pages;
    }

    private Path getMoinMoinPath() {
        String pathName = properties.getProperty(MOINMOIN_DIR_PROPERTY);
        return pathName != null ? Paths.get(pathName) : DEFAULT_MOINMOIN_DIR;
    }

    private Charset getInputCharset() {
        String charsetName = properties.getProperty(CHARSET_PROPERTY, null);
        return charsetName == null ? Charset.defaultCharset() : Charset.forName(charsetName);
    }

    private void checkIncompatibilities(List<Page> pages) {
        String givenFileName = properties.getProperty(INCOMPATIBILITIES_PROPERTY);
        new IncompatibilityLogger(IncompatibilityLogger.readIncompatibilities(givenFileName)).check(pages, VERSION_NUMBER);
    }

    private int writePages(List<Page> pages) {
        int written = 0;
        try {
            DokuWikiWriter dokuWikiWriter = getDokuWikiWriter();
            written = dokuWikiWriter.write(pages);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return written;
    }

    private DokuWikiWriter getDokuWikiWriter() throws IOException {
        String dokuwikiDir = properties.getProperty(DOKUWIKI_DIR_PROPERTY);
        Path dokuWikiPath = dokuwikiDir != null ? Paths.get(dokuwikiDir) :
                DEFAULT_DOKUWIKI_PAGES_DIR;
        boolean addFixMe = Boolean.parseBoolean(properties.getProperty(FIXME_PROPERTY, "false"));
        boolean cleanTags = Boolean.parseBoolean(
                properties.getProperty(TAG_CLEANER_PROPERTY, "false"));
        String mappingFile = properties.getProperty(MAPPING_FILE_PROPERTY, null);

        if (mappingFile != null) {
            return new DokuWikiWriter(dokuWikiPath, addFixMe, cleanTags, Paths.get(mappingFile));
        }

        return new DokuWikiWriter(dokuWikiPath, addFixMe, cleanTags);
    }


    static class PropertiesAndArgs {
        private final Properties properties;
        private final String[] args;

        PropertiesAndArgs(Properties properties, String[] args) {
            this.properties = properties;
            this.args = args;
        }

        Properties getProperties() {
            return properties;
        }

        String[] getArgs() {
            return args;
        }
    }
}
