package net.unterwelt;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;
import javax.json.*;

class IncompatibilityLogger {

    private static final String PAGE_MESSAGE =
            " contains the following problematic sequences:\n";
    private static final String OUTPUT_FILE_NAME = "moinmoin2dokuwiki-warnings-%tF-%tT.txt";
    public static final String START_MESSAGE = "MoinMoin2DokuWikiWriter v";
    private static final String END_MESSAGE = "(the sequences might not be problematic as they "
            + "might e.g. be within nowiki.tags)";

    private List<Incompatibility> pageNameIncompatibilities;
    private List<Incompatibility> contentIncompatibilities;
    private final JsonObject json;

    /**
     * Definitions in triggers file overwrite known values.
     *
     * @param json a JsonObject with incompatibilities for page titles and content
     */
    IncompatibilityLogger(JsonObject json) {
        this.json = json;
        pageNameIncompatibilities = readPageNameIncompatibilities();
        contentIncompatibilities = readContentIncompatibilities();
    }

    static JsonObject readIncompatibilities(String givenFileName) {
        if (givenFileName != null && !"".equals(givenFileName)) {
            Path givenPath = Paths.get(givenFileName);
            try (FileReader fileReader = new FileReader(givenPath.toFile())) {
                return Json.createReader(fileReader).readObject();
            } catch (IOException e) {
                throw new RuntimeException("Could not find given incompatibilities file: " + givenPath.toString(), e );
            }
        } else {
            InputStream jsonStream = MoinMoin2DokuWiki.class.getClassLoader().
                    getResourceAsStream("incompatibilities.json");
            if (jsonStream == null) {
                throw new RuntimeException("Could not find default incompatibility file");
            }
            return Json.createReader(jsonStream).readObject();
        }
    }

    List<Incompatibility> readPageNameIncompatibilities() {
        List<Incompatibility> pageNameIncompatibilities = new ArrayList<>();
        JsonArray pageNameArray = json.getJsonArray("page name incompatibilities");

        for (JsonValue jsonValue : pageNameArray) {
            pageNameIncompatibilities.add(createIncompatibility(jsonValue, Collections.emptyList()));
        }

        return pageNameIncompatibilities;
    }

    List<Incompatibility> readContentIncompatibilities() {
        List<Incompatibility> contentIncompatibilities = new ArrayList<>();
        JsonArray contentArray = json.getJsonArray("content incompatibilities");

        for (JsonValue jsonValue : contentArray) {
            List<String> exceptions = getStringsFromJson(jsonValue.asJsonObject().getJsonArray(
                    "exceptions"));
            contentIncompatibilities.add(createIncompatibility(jsonValue, exceptions));
        }

        return contentIncompatibilities;
    }

    private Incompatibility createIncompatibility(JsonValue jsonValue, List<String> exceptions) {
        String trigger = jsonValue.asJsonObject().getString("trigger");
        String comment;
        try {
            comment = jsonValue.asJsonObject().getString("comment");
        } catch (NullPointerException npe) {
            comment = "";
        }
        return new Incompatibility(trigger, comment, exceptions);
    }

    private  List<String> getStringsFromJson(JsonArray jsonValues) {
        if (jsonValues != null) {
            return jsonValues.stream()
                    .map(value -> ((JsonString) value).getString())
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    String check(List<Page> pages, String version) {
        return check(pages, version, true);
    }

    String check(List<Page> pages, String version, boolean writeFile) {
        StringBuilder result = new StringBuilder().append(START_MESSAGE);
        result.append(version).append("\n\n\n");

        for (Page page : pages) {
            boolean foundWarningsInPage = addPageNameWarnings(result, page);
            foundWarningsInPage = addContentWarnings(result, page, foundWarningsInPage);
            if (foundWarningsInPage) {
                result.append("\n\n");
            }
        }
        result.append(END_MESSAGE);

        if (writeFile) {
            writeToFile(result);
        }
        return result.toString();
    }

    private boolean addPageNameWarnings(StringBuilder result, Page page) {
        boolean foundWarnings = false;

        for (Incompatibility incompatibility : pageNameIncompatibilities) {
            String trigger = incompatibility.getTrigger();
            boolean foundTrigger = page.getName().contains(trigger);
            if (!foundTrigger) {
                foundTrigger =
                        page.getSuperPages().stream().anyMatch(superPage -> superPage.contains(trigger));
            }

            if (foundTrigger) {
                addPageHeading(result, foundWarnings, page);
                foundWarnings = true;
                result.append("  ").append(trigger).append(" (in page name or super pages)\n");
            }
        }

        return foundWarnings;
    }

    private boolean addContentWarnings(StringBuilder result, Page page,
                                       boolean hasPreviousWarnings) {
        for (Incompatibility incompatibility : contentIncompatibilities) {
            for (String line : page.getContent()) {
                boolean triggered = false;
                int index = line.indexOf(incompatibility.getTrigger());
                while (index >= 0) {
                    if (isInExceptions(line.substring(index), incompatibility)) {
                        index = line.indexOf(incompatibility.getTrigger(), index + 1);
                    } else {
                        addPageHeading(result, hasPreviousWarnings, page);
                        result.append("  ").append(incompatibility.getTrigger()).append("\n");
                        hasPreviousWarnings = true;
                        triggered = true;
                        break;
                    }
                }
                if (triggered) {
                    break;
                }
            }
        }

        return hasPreviousWarnings;
    }

    private boolean isInExceptions(String substring, Incompatibility incompatibility) {
        for (String exception : incompatibility.getExceptions()) {
            if (substring.startsWith(exception)) {
                return true;
            }
        }
        return false;
    }

    private void addPageHeading(StringBuilder result, boolean foundWarningsInPage, Page page) {
        if (!foundWarningsInPage) {
            result.append("'").append(page.getLongName(".")).append("'").append(PAGE_MESSAGE);
        }
    }

    private void writeToFile(StringBuilder result) {
        Path outputPath = Paths.get(String.format(OUTPUT_FILE_NAME, LocalDate.now(),
                LocalTime.now()));
        Charset charset = StandardCharsets.UTF_8;

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, charset)) {
            writer.write(result.toString());
            System.out.println("Written: " + outputPath);
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }


}
