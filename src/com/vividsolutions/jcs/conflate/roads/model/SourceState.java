package com.vividsolutions.jcs.conflate.roads.model;

import java.io.Serializable;
import java.util.HashMap;

public class SourceState implements Serializable {

    private static HashMap nameToSourceStateMap = new HashMap();

    private SourceState(String name) {
        this.name = name;
        nameToSourceStateMap.put(name, this);
    }

    public String getName() {
        return name;
    }

    public boolean indicates(SourceState[] states) {
        for (int i = 0; i < states.length; i++) {
            if (states[i] == this) { return true; }
        }
        return false;
    }

    /**
     * @see http://www.javaworld.com/javaworld/javatips/jw-javatip122.html
     */
    private Object readResolve() {
        return nameToSourceStateMap.get(name);
    }

    public String toString() {
        return getName();
    }

    private String name;

    public static final SourceState MATCHED_NON_REFERENCE = new SourceState(
            "Matched (Non-Reference)");

    public static final SourceState MATCHED_REFERENCE = new SourceState(
            "Matched (Reference)");

    public static final SourceState RETIRED = new SourceState("Retired");

    public static final SourceState STANDALONE = new SourceState("Standalone");

    public static final SourceState UNKNOWN = new SourceState("Unknown");

    public static final SourceState[] COMMITTED = new SourceState[]{
            MATCHED_NON_REFERENCE, MATCHED_REFERENCE, STANDALONE};

    public static final SourceState[] INCLUDED = new SourceState[]{
            STANDALONE, MATCHED_REFERENCE};

    public static final SourceState[] MATCHED = new SourceState[]{
            MATCHED_NON_REFERENCE, MATCHED_REFERENCE};
}