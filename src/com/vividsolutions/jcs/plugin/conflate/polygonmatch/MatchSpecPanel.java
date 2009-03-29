package com.vividsolutions.jcs.plugin.conflate.polygonmatch;

import com.vividsolutions.jcs.conflate.polygonmatch.FeatureMatcher;

/**
 * Enables the user to specify the combination of match algorithms to use.
 */
public interface MatchSpecPanel {
    public abstract FeatureMatcher getFeatureMatcher();
}