package net.unterwelt;

import static net.unterwelt.MoinMoin2DokuWiki.*;

import java.nio.charset.*;
import java.util.*;
import java.util.function.*;
import javax.json.*;

public enum MainOption {
    CHARSET("-c", "--charset", "use specific charset for input\n\t\t"
            + "(see https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/charset/Charset.html)",
            MainOption::handleCharSet),
    FIXME("-f", "--fixme", "add a fixme message at the start of each generated page",
            MainOption::handleFixMe),
    INCOMPATIBILITIES("-i", "--incompatibilities", "json file defining when to "
            + "log warnings", MainOption::handleIncompatibilities),
    MAPPING_FILE("-m", "--mappingfile", "file with mappings for non-ASCII characters to use when "
            + "creating DokuWiki files.", MainOption::handleMappingFile),
    DOKUWIKI_DIR("-o", "--outputdir", "path to the DokuWiki pages directory (default is "
            + "dokuwiki/data/pages; will be created if missing)", MainOption::handleDokuWikiDir),
    MOINMOIN_DIR("-p", "--inputdir", "path to the MoinMoin pages directory (default is "
            + "MoinMoin/wiki/data/pages))", MainOption::handleMoinMoinDir),
    TAG_CLEANER("-t", "--tagcleaner", "activate to automatically remove tags without content (e.g"
            + ". ****)", MainOption::handleTagCleaner),

    HELP("-h", "--help", "show this help message",
            MainOption::handleHelp),
    LIST_INCOMPATIBILITIES("-l", "--list-incompatibilities", "list the default incompatibilities",
            MainOption::handleListIncompatibilities),
    VERSION("-v", "--version", "show version info",
            MainOption::handleVersion)
    ;

    private static final String CHARSET_JAVADOC =
            "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/charset/Charset.html";

    private final String shortFlag;

    private final String longFlag;
    private final String helpText;
    private final Function<PropertiesAndArgs, Integer> handler;
    MainOption(String shortFlag, String longFlag, String description,
               Function<PropertiesAndArgs, Integer> handler) {
        this.shortFlag = shortFlag;
        this.longFlag = longFlag;
        this.helpText = description;
        this.handler = handler;
    }

    int handle(PropertiesAndArgs propertiesAndArgs) {
        return handler.apply(propertiesAndArgs);
    }

    private static int handleCharSet(PropertiesAndArgs propertiesAndArgs) {
        Properties properties = propertiesAndArgs.getProperties();
        String charsetName = propertiesAndArgs.getArgs()[1];
        properties.setProperty(CHARSET_PROPERTY, charsetName);
        if (!Charset.isSupported(charsetName)) {
            throw new IllegalArgumentException("Unknown charset: " + charsetName + "\n" + "see "
                    + CHARSET_JAVADOC);
        }

        return 1;
    }
    private static int handleDokuWikiDir(PropertiesAndArgs propertiesAndArgs) {
        Properties properties = propertiesAndArgs.getProperties();
        String dokuWikiPath = propertiesAndArgs.getArgs()[1];
        properties.setProperty(DOKUWIKI_DIR_PROPERTY, dokuWikiPath);

        return 1;
    }
    static int handleFixMe(PropertiesAndArgs propertiesAndArgs) {
        Properties properties = propertiesAndArgs.getProperties();
        properties.setProperty(FIXME_PROPERTY, "true");

        return 0;
    }

    private static int handleIncompatibilities(PropertiesAndArgs propertiesAndArgs) {
        Properties properties = propertiesAndArgs.getProperties();
        String incompatibilitiesFile = propertiesAndArgs.getArgs()[0];
        properties.setProperty(INCOMPATIBILITIES_PROPERTY, incompatibilitiesFile);

        return 1;
    }

    private static int handleMappingFile(PropertiesAndArgs propertiesAndArgs) {
        Properties properties = propertiesAndArgs.getProperties();
        String mappingFile = propertiesAndArgs.getArgs()[1];
        properties.setProperty(MAPPING_FILE_PROPERTY, mappingFile);

        return 1;
    }

    private static int handleMoinMoinDir(PropertiesAndArgs propertiesAndArgs) {
        Properties properties = propertiesAndArgs.getProperties();
        String moinMoinPath = propertiesAndArgs.getArgs()[1];
        properties.setProperty(MOINMOIN_DIR_PROPERTY, moinMoinPath);

        return 1;
    }

    private static int handleTagCleaner(PropertiesAndArgs propertiesAndArgs) {
        Properties properties = propertiesAndArgs.getProperties();
        properties.setProperty(TAG_CLEANER_PROPERTY, "true");

        return 0;
    }

    static int handleHelp(PropertiesAndArgs propertiesAndArgs) {
        String messageHeader = "Usage: moinmoin2dokuwiki [OPTION [PARAMETER]]\n"
                + "Convert MoinMoin wiki pages to DokuWiki pages\n"
                + "Not all of MoinMoin's features are recognized - see README.md for "
                + "restrictions!\n"
                + "\n"
                + "MoinMoin's main directory is expected to be a direct subdirectory of the "
                + "current directory called 'MoinMoin', with its pages in "
                + "MoinMoin/wiki/data/pages. Use the input directory option (" + MOINMOIN_DIR.shortFlag
                + ") to designate another input directory.\n"
                + "The resulting pages will be put in a DokuWiki compatible directory structure "
                + "(dokuwiki/data/pages) with dokuwiki being a direct subdirectory of the current"
                + " directory. Use the input directory option (" + DOKUWIKI_DIR.shortFlag + ") to"
                + " designate another output directory.\n"
                + "You might have to change the file permissions of the resulting files to be "
                + "able to edit the pages.\n\n";
        StringBuilder messageBody = new StringBuilder();
        for (MainOption option : MainOption.values()) {
            messageBody.append(String.format("%s, %-15s %s\n",option.getShortFlag(),
                    option.getLongFlag(), option.getHelpText()));
        }
        System.out.println(messageHeader + messageBody.toString());

        propertiesAndArgs.getProperties().setProperty(RUN_PROPERTY, "false");
        return 0;
    }

    private static int handleListIncompatibilities(PropertiesAndArgs propertiesAndArgs) {
        final JsonObject json = readJson(propertiesAndArgs);
        if (json == null) {
            System.out.println("JSON file not found");
            return 0;
        }
        IncompatibilityLogger logger = new IncompatibilityLogger(json);

        String headerMessage = "Known sequences that might appear in MoinMoin pages and are not "
                + "compatible with DokuWiki (or this converter). Warnings will be logged to a "
                + "text file if these sequences are found in a MoinMoin page:\n\n";

        List<Incompatibility> pageNameIncompatibilities = logger.readPageNameIncompatibilities();
        String pageNameMessage = createMessage(pageNameIncompatibilities);

        List<Incompatibility> contentIncompatibilities = logger.readContentIncompatibilities();
        String contentMessage = createMessage(contentIncompatibilities);

        System.out.println(headerMessage);
        System.out.println("in page names:\n" + pageNameMessage);
        System.out.println("\nin page content:\n" + contentMessage);

        propertiesAndArgs.getProperties().setProperty(RUN_PROPERTY, "false");
        return 0;
    }

    private static String createMessage(List<Incompatibility> pageNameIncompatibilities) {
        StringBuilder result = new StringBuilder();

        for (Incompatibility incompatibility : pageNameIncompatibilities) {
            result.append(String.format("%-15s\n",
                    incompatibility.getTrigger()));
            if (!incompatibility.getComment().isBlank()) {
                result.append(String.format("%-15s %s\n", "->", incompatibility.getComment()));
            }
        }

        return result.toString();
    }

    private static JsonObject readJson(PropertiesAndArgs propertiesAndArgs) {
        String givenIncompatibilities = propertiesAndArgs.getProperties().getProperty(INCOMPATIBILITIES_PROPERTY);
        return IncompatibilityLogger.readIncompatibilities(givenIncompatibilities);
    }

    static int handleVersion(PropertiesAndArgs propertiesAndArgs) {
        String message = "moinmoin2dokuwiki %s\n"
                + "Copyright © Dag Witthold\n"
                + "License Creative Commons Attribution-ShareAlike 4.0\n"
                + "There is NO WARRANTY, to the extent permitted by law.\n";
        System.out.println(String.format(message, VERSION_NUMBER));

        propertiesAndArgs.getProperties().setProperty(RUN_PROPERTY, "false");
        return 0;
    }

    String getShortFlag() {
        return shortFlag;
    }

    String getLongFlag() {
        return longFlag;
    }

    String getHelpText() {
        return helpText;
    }
}
