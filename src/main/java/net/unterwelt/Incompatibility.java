package net.unterwelt;

import java.util.*;

class Incompatibility {
    private final String trigger;
    private final String comment;
    private final List<String> exceptions;

    Incompatibility(String trigger, String comment, List<String> exceptions) {
        this.trigger = trigger;
        this.comment = comment;
        this.exceptions = exceptions;
    }

    String getTrigger() {
        return trigger;
    }

    String getComment() {
        return comment;
    }

    Collection<String> getExceptions() {
        return Collections.unmodifiableCollection(exceptions);
    }
}
