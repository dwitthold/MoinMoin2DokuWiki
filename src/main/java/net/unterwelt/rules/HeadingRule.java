package net.unterwelt.rules;

public class HeadingRule implements WholeLineRule {

    private static final int DOKUWIKI_MAX_HEADING_LEVEL = 6;

    @Override
    public String convert(String line) {
        if (!line.startsWith("==") && !line.endsWith("==")) {
            return line;
        }

        ValidPart startValidity = verifyStart(line);
        if (!startValidity.isValid) {
            return line;
        }

        ValidPart endValidity = verifyEnd(line, startValidity.signifierCount);
        if (!endValidity.isValid) {
            return line;
        }

        return convertToHeading(line, startValidity.signifierCount);
    }

    private ValidPart verifyStart(String line) {
        int startCount = 2;
        for (int i = startCount; i <= (line.length() / 2); i++) {
            if (!"=".equals(line.substring(i, i + 1))) {
                if (" ".equals(line.substring(i, i + 1))) {
                    return new ValidPart(true, i);
                } else {
                    return new ValidPart(false, -1);
                }
            }
        }
        return new ValidPart(false, -1);
    }

    private ValidPart verifyEnd(String line, int signifierCount) {
        int endCount = 2;
        for (int i = endCount; i <= (line.length() / 2); i++) {
            int index = line.length() - i - 1;
            String character = line.substring(index, index + 1);
            if (!"=".equals(character)) {
                if (" ".equals(character) && signifierCount == i) {
                    return new ValidPart(true, index);
                } else {
                    return new ValidPart(false, -1);
                }
            }
        }
        return new ValidPart(false, -1);
    }

    private String convertToHeading(String line, int signifierCount) {
        String content = line.substring(signifierCount + 1, line.length() - (signifierCount + 1));

        int newSignifierCount = DOKUWIKI_MAX_HEADING_LEVEL + 2 - signifierCount;
        if (newSignifierCount < 2) {
            newSignifierCount = 2;
        }
        String signifiers = "=".repeat(newSignifierCount);

        return signifiers + " " + content + " " + signifiers;
    }

    private static class ValidPart {
        boolean isValid;
        int signifierCount;

        private ValidPart(boolean isValid, int signifierCount) {
            this.isValid = isValid;
            this.signifierCount = signifierCount;
        }
    }
}
