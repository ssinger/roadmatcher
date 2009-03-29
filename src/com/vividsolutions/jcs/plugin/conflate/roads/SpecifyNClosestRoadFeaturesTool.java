package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Color;
import java.awt.geom.NoninvertibleTransformException;
import java.util.*;

import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.jump.FUTURE_StringUtil;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.CollectionMap;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;

public abstract class SpecifyNClosestRoadFeaturesTool extends
        SpecifyRoadFeaturesTool {

    public SpecifyNClosestRoadFeaturesTool(int n, boolean forLayer0,
            boolean forLayer1, String cursorImage, String buttonImage,
            Color color, WorkbenchContext context, GestureMode gestureMode) {
        this(n, n, forLayer0, forLayer1, cursorImage, buttonImage, color,
                context, gestureMode);
    }

    public SpecifyNClosestRoadFeaturesTool(int min, int max, boolean forLayer0,
            boolean forLayer1, String cursorImage, String buttonImage,
            Color color, WorkbenchContext context, GestureMode gestureMode) {
        super(forLayer0, forLayer1, cursorImage, buttonImage, color, context,
                gestureMode);
        Assert.isTrue(min <= max);
        //Can be large because we do proximity search [Jon Aquino 2004-01-19]
        setViewClickBuffer(12);
        this.min = min;
        this.max = max;
    }

    protected void gestureFinished() throws Exception {
        reportNothingToUndoYet();
        if (!checkConflationSessionStarted(getContext())) { return; }
        noRoadSegmentsWarning = getDefaultNoRoadSegmentsWarning();
        final Map layerToSpecifiedFeaturesMap = layerToSpecifiedFeaturesMap();
        if (layerToSpecifiedFeaturesMap.isEmpty() && min > 0) {
            warnUser(noRoadSegmentsWarning, getContext());
            return;
        }
        gestureFinished(layerToSpecifiedFeaturesMap);
    }

    protected abstract void gestureFinished(Map layerToSpecifiedFeaturesMap)
            throws Exception;

    protected String getDefaultNoRoadSegmentsWarning() {
        return FUTURE_StringUtil.substitute(
                ErrorMessages.specifyNClosestRoadFeaturesTool_noRoadSegments,
                new Object[] { sourceLayerDescription()});
    }

    protected boolean includeInProximitySearch(SourceFeature feature,
            Point clickPoint) {
        return true;
    }

    protected Map layerToSpecifiedFeaturesMap()
            throws NoninvertibleTransformException {
        Map layerToSpecifiedFeaturesMap = super.layerToSpecifiedFeaturesMap();
        if (layerToSpecifiedFeaturesMap.isEmpty()) { return layerToSpecifiedFeaturesMap; }
        SortedMap distanceToFeatureMap = new TreeMap();

        Point clickPoint = new GeometryFactory()
                .createPoint(getModelDestination());
        for (Iterator i = layerToSpecifiedFeaturesMap.keySet().iterator(); i
                .hasNext();) {
            Layer layer = (Layer) i.next();
            for (Iterator j = ((Collection) layerToSpecifiedFeaturesMap
                    .get(layer)).iterator(); j.hasNext();) {
                SourceFeature feature = (SourceFeature) j.next();
                if (!includeInProximitySearch(feature, clickPoint)) {
                    continue;
                }
                distanceToFeatureMap.put(new Double(feature.getGeometry()
                        .distance(clickPoint)), feature);
            }
        }
        if (0 < distanceToFeatureMap.size() && distanceToFeatureMap.size() < min) {
            if (noRoadSegmentsWarning.equals(getDefaultNoRoadSegmentsWarning())) {
                setNoRoadSegmentsWarning(getDefaultTooFewRoadSegmentsWarning());
            }
            return Collections.EMPTY_MAP;
        }
        CollectionMap newLayerToSpecifiedFeaturesMap = new CollectionMap();
        for (Iterator i = distanceToFeatureMap.values().iterator(); i.hasNext();) {
            SourceFeature feature = (SourceFeature) i.next();
            newLayerToSpecifiedFeaturesMap.addItem(ToolboxModel.instance(
                    getContext().getLayerManager(), getContext())
                    .getSourceLayer(feature.getRoadSegment().getNetworkID()),
                    feature);
            if (CollectionUtil.concatenate(
                    newLayerToSpecifiedFeaturesMap.values()).size() == max) {
                break;
            }
        }
        return newLayerToSpecifiedFeaturesMap;
    }

    private String getDefaultTooFewRoadSegmentsWarning() {
        return FUTURE_StringUtil
                .substitute(
                        ErrorMessages.specifyNClosestRoadFeaturesTool_tooFewRoadSegments,
                        new Object[] { min + "",
                                sourceLayerDescription()});
    }

    protected void setNoRoadSegmentsWarning(String noRoadSegmentsWarning) {
        this.noRoadSegmentsWarning = noRoadSegmentsWarning;
    }

    private String noRoadSegmentsWarning;

    private int min;

    private int max;
}
