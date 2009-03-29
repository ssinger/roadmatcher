package com.vividsolutions.jcs.plugin.conflate.polygonmatch;

import java.util.*;

import com.vividsolutions.jcs.conflate.polygonmatch.*;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.tools.AttributeMapping;
import com.vividsolutions.jump.util.CollectionUtil;

/**
 * Calls the polygon-matching API. Works at the FeatureCollection level, 
 * whereas MatchPlugIn works at the Layer level.
 * @see MatchPlugIn
 */
public class MatchEngine {

    public FeatureCollection getCandidateFeatureCollection() {
        return candidateFeatureCollection;
    }

    public FeatureCollection getTargetFeatureCollection() {
        return targetFeatureCollection;
    }

    private FeatureCollection createMatchedFeatureCollection(boolean fromTarget, TargetUnioningFCMatchFinder targetUnioningFCMatchFinder) {
        AttributeMapping mapping =
            new AttributeMapping(
                (fromTarget ? targetFeatureCollection : candidateFeatureCollection)
                    .getFeatureSchema(),
                new FeatureSchema());
        //Feature may appear more than once because of unioning. So remove duplicates. [Jon Aquino]                        
        TreeSet featureSet = new TreeSet();                
        FeatureCollection newFC = new FeatureDataset(createSchema(mapping, targetUnioningFCMatchFinder != null));
        for (Iterator i = targetFeatureToMatchesMap.keySet().iterator(); i.hasNext();) {
            Feature target = (Feature) i.next();
            Matches matches = (Matches) targetFeatureToMatchesMap.get(target);
            Feature topCandidate = matches.getTopMatch();
            if (topCandidate == null) {
                continue;
            }
            Feature originalFeature = fromTarget ? target : topCandidate;
            Feature newFeature = new BasicFeature(newFC.getFeatureSchema());
            //2nd arg doesn't matter. [Jon Aquino]
            mapping.transferAttributes(originalFeature, originalFeature, newFeature);
            newFeature.setAttribute(SCORE_ATTRIBUTE, new Double(matches.getTopScore()));
            if (targetUnioningFCMatchFinder != null) {
                newFeature.setAttribute(UNION_ID_ATTRIBUTE, targetUnioningFCMatchFinder.getUnionID(target));
            }
            newFeature.setGeometry((Geometry) originalFeature.getGeometry().clone());
            featureSet.add(newFeature);
        }
        newFC.addAll(featureSet);
        return newFC;
    }

    private FeatureCollection candidateFeatureCollection;
    private FeatureCollection targetFeatureCollection;
    public static final String SCORE_ATTRIBUTE = "SCORE";
    public static final String UNION_ID_ATTRIBUTE = "UNION ID";
    private Map targetFeatureToMatchesMap;
    private FeatureCollection matchedTargetsFeatureCollection;
    private FeatureCollection unmatchedTargetsFeatureCollection;
    private FeatureCollection matchedCandidatesFeatureCollection;
    private FeatureCollection unmatchedCandidatesFeatureCollection;
    private FeatureCollection matchPairFeatureCollection;

    public void match(
        FeatureCollection targetFeatureCollection,
        FeatureCollection candidateFeatureCollection,
        FeatureMatcher featureMatcher,
        boolean filteringByWindow,
        double windowBuffer,
        boolean filteringByArea,
        double minArea,
        double maxArea,
        boolean unioningCandidates,
        int maxUnionMembers,
        TaskMonitor taskMonitor) {
        this.targetFeatureCollection = targetFeatureCollection;
        this.candidateFeatureCollection = candidateFeatureCollection;
        FeatureMatcher actualFeatureMatcher = featureMatcher;
        if (filteringByWindow) {
            actualFeatureMatcher =
                new ChainMatcher(
                    new FeatureMatcher[] { new WindowFilter(50), actualFeatureMatcher });
        }
        FCMatchFinder matchFinder = new BasicFCMatchFinder(actualFeatureMatcher);
        //We definitely want to one-to-one before union (combinatorial) -- if after, we'll
        //wipe out some union members! [Jon Aquino]        
        matchFinder = new DisambiguatingFCMatchFinder(matchFinder);
        TargetUnioningFCMatchFinder targetUnioningFCMatchFinder = null;
        if (unioningCandidates) {            
            targetUnioningFCMatchFinder = new TargetUnioningFCMatchFinder(maxUnionMembers, matchFinder);
            matchFinder = targetUnioningFCMatchFinder;
        }
        if (filteringByArea) {
            matchFinder = new AreaFilterFCMatchFinder(minArea, maxArea, matchFinder);
        }
        targetFeatureToMatchesMap =
            matchFinder.match(
                targetFeatureCollection,
                candidateFeatureCollection,
                taskMonitor);
        matchPairFeatureCollection = createMatchPairFeatureCollection(targetUnioningFCMatchFinder);                
        matchedTargetsFeatureCollection = createMatchedFeatureCollection(true, targetUnioningFCMatchFinder);        
        unmatchedTargetsFeatureCollection = createUnmatchedFeatureCollection(true);
        matchedCandidatesFeatureCollection = createMatchedFeatureCollection(false, targetUnioningFCMatchFinder);
        unmatchedCandidatesFeatureCollection = createUnmatchedFeatureCollection(false);        
    }

    private FeatureCollection createUnmatchedFeatureCollection(boolean fromTarget) {
        FeatureCollection originalFC =
            fromTarget ? targetFeatureCollection : candidateFeatureCollection;
        FeatureCollection newFC = new FeatureDataset(originalFC.getFeatureSchema());
        newFC.addAll(originalFC.getFeatures());
        newFC.removeAll(
            fromTarget
                ? matchedTargets(targetFeatureToMatchesMap)
                : topCandidates(targetFeatureToMatchesMap));
        return clone(newFC);
    }

    private Collection topCandidates(Map targetFeatureToMatchesMap) {
        ArrayList topCandidates = new ArrayList();
        for (Iterator i = targetFeatureToMatchesMap.values().iterator(); i.hasNext();) {
            Matches matches = (Matches) i.next();
            CollectionUtil.addIfNotNull(matches.getTopMatch(), topCandidates);
        }
        return topCandidates;
    }

    private Collection matchedTargets(Map targetFeatureToMatchesMap) {
        ArrayList matchedTargets = new ArrayList();
        for (Iterator i = targetFeatureToMatchesMap.keySet().iterator(); i.hasNext();) {
            Feature target = (Feature) i.next();
            Matches matches = (Matches) targetFeatureToMatchesMap.get(target);
            if (matches.getTopMatch() != null) {
                matchedTargets.add(target);
            }
        }
        return matchedTargets;
    }

    private FeatureCollection clone(FeatureCollection fc) {
        FeatureCollection clone = new FeatureDataset(fc.getFeatureSchema());
        for (Iterator i = fc.iterator(); i.hasNext();) {
            Feature feature = (Feature) i.next();
            clone.add((Feature) feature.clone());
        }
        return clone;
    }

    public FeatureCollection getMatchedTargetsFeatureCollection() {
        return matchedTargetsFeatureCollection;
    }

    public FeatureCollection getUnmatchedTargetsFeatureCollection() {
        return unmatchedTargetsFeatureCollection;
    }

    public FeatureCollection getMatchedCandidatesFeatureCollection() {
        return matchedCandidatesFeatureCollection;
    }

    public FeatureCollection getUnmatchedCandidatesFeatureCollection() {
        return unmatchedCandidatesFeatureCollection;
    }

    /**
     * @return GeometryCollections containing the target and candidate geometries
     * for each match
     */
    public FeatureCollection getMatchPairFeatureCollection() {
        return matchPairFeatureCollection;
    }
    
    private FeatureCollection createMatchPairFeatureCollection(TargetUnioningFCMatchFinder targetUnioningFCMatchFinder) {
        GeometryFactory factory = new GeometryFactory();
        AttributeMapping mapping =
            new AttributeMapping(
                targetFeatureCollection.getFeatureSchema(),
                candidateFeatureCollection.getFeatureSchema());
        FeatureCollection newFC = new FeatureDataset(createSchema(mapping, targetUnioningFCMatchFinder != null));
        for (Iterator i = targetFeatureToMatchesMap.keySet().iterator(); i.hasNext();) {
            Feature target = (Feature) i.next();
            Matches matches = (Matches) targetFeatureToMatchesMap.get(target);
            Feature topCandidate = matches.getTopMatch();
            if (topCandidate == null) {
                continue;
            }
            Feature newFeature = new BasicFeature(newFC.getFeatureSchema());
            mapping.transferAttributes(target, topCandidate, newFeature);
            newFeature.setAttribute(SCORE_ATTRIBUTE, new Double(matches.getTopScore()));
            if (targetUnioningFCMatchFinder != null) {
                newFeature.setAttribute(UNION_ID_ATTRIBUTE, targetUnioningFCMatchFinder.getUnionID(target));
            }
            newFeature.setGeometry(
                factory.createGeometryCollection(
                    new Geometry[] {
                        (Geometry) target.getGeometry().clone(),
                        (Geometry) topCandidate.getGeometry().clone()}));
            newFC.add(newFeature);
        }
        return newFC;
    }

    private FeatureSchema createSchema(AttributeMapping mapping, boolean addUnionID) {
        //Want score as first attribute [Jon Aquino]
        FeatureSchema schema = new FeatureSchema();
        schema.addAttribute(SCORE_ATTRIBUTE, AttributeType.DOUBLE);
        if (addUnionID) {
            schema.addAttribute(UNION_ID_ATTRIBUTE, AttributeType.INTEGER);
        }
        FeatureSchema mappingSchema = mapping.createSchema("GEOMETRY");
        for (int i = 0; i < mappingSchema.getAttributeCount(); i++) {
            schema.addAttribute(
                mappingSchema.getAttributeName(i),
                mappingSchema.getAttributeType(i));
        }
        return schema;
    }

}
