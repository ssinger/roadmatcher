package com.vividsolutions.jcs.plugin.clean.coveragecleaningtoolbox;

import com.vividsolutions.jcs.qa.InternalMatchedSegmentFinder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.task.DummyTaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.SnapVerticesTool;

import java.awt.geom.NoninvertibleTransformException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.JComponent;


/**
 * Differences from SnapVerticesTool: (1) snaps to and snaps one layer: the
 * input layer specified in the toolbox (2) updates gap indicators
 */
public class UpdatingSnapVerticesTool extends SnapVerticesTool {
    private ToolboxPanel toolboxPanel;
    private WorkbenchContext context;

    public UpdatingSnapVerticesTool(WorkbenchContext context,
        ToolboxPanel toolboxPanel) {
        super(new EnableCheckFactory(context));
        this.context = context;
        this.toolboxPanel = toolboxPanel;
    }

    public String getName() {
        return "Snap Vertices Of Coverage";
    }

    protected void gestureFinished() throws Exception {
        reportNothingToUndoYet();

        if (!check(new EnableCheck() {
                    public String check(JComponent component) {
                        return toolboxPanel.getInputLayerComboBox()
                                               .getSelectedLayer() == null
                            ? "Input layer must be specified" : null;
                    }
                })
        ) {
            return;
        }

        if (!check(new EnableCheck() {
                    public String check(JComponent component) {
                        return !inputLayer().isEditable()
                            ? "Input layer must be editable" : null;
                    }
                })
        ) {
            return;
        }

        toolboxPanel.getLayerDependencyManager().setIgnoringLayerChanges(toolboxPanel
                                                                             .getGapsTab(),
            true);

        try {
            super.gestureFinished();
        } finally {
            toolboxPanel.getLayerDependencyManager().setIgnoringLayerChanges(toolboxPanel
                                                                                 .getGapsTab(),
                false);
        }
    }

    protected Iterator candidateLayersIterator() {
        return Collections.singleton(inputLayer()).iterator();
    }

    protected void snapVertices(Collection editableLayers,
        Coordinate suggestedTarget, Feature targetFeature)
        throws Exception, NoninvertibleTransformException {
        super.snapVertices(Collections.singleton(inputLayer()),
            suggestedTarget, targetFeature);

        InternalMatchedSegmentFinder finder = new InternalMatchedSegmentFinder(inputLayer()
                                                                                   .getFeatureCollectionWrapper(),
                new InternalMatchedSegmentFinder.Parameters(toolboxPanel.getGapToleranceTextField()
                                                                        .getDouble(),
                    toolboxPanel.getAngleToleranceTextField().getDouble()),
                new DummyTaskMonitor());
        finder.setFence(getBoxInModelCoordinates());
        finder.computeMatches();
        removeFromLayer(FindGapsPlugIn.GAP_SEGMENTS_LAYER_NAME,
            getBoxInModelCoordinates());
        removeFromLayer(FindGapsPlugIn.GAP_SIZES_LAYER_NAME,
            getBoxInModelCoordinates());
        addToLayer(FindGapsPlugIn.GAP_SEGMENTS_LAYER_NAME,
            finder.getMatchedSegments());
        addToLayer(FindGapsPlugIn.GAP_SIZES_LAYER_NAME,
            finder.getSizeIndicators());
    }

    private void removeFromLayer(String layerName, Envelope envelope) {
        if (context.getLayerManager().getLayer(layerName) == null) {
            return;
        }

        EditTransaction transaction = new EditTransaction(new ArrayList(),
                getName(), context.getLayerManager().getLayer(layerName),
                isRollingBackInvalidEdits(), true, context.getLayerViewPanel());

        for (Iterator i = transaction.getLayer().getFeatureCollectionWrapper()
                                     .query(envelope).iterator(); i.hasNext();) {
            Feature feature = (Feature) i.next();
            transaction.deleteFeature(feature);
        }

        transaction.commit();
    }

    private void addToLayer(String layerName,
        FeatureCollection featureCollection) {
        if (context.getLayerManager().getLayer(layerName) == null) {
            return;
        }

        EditTransaction transaction = new EditTransaction(new ArrayList(),
                getName(), context.getLayerManager().getLayer(layerName),
                isRollingBackInvalidEdits(), true, context.getLayerViewPanel());

        for (Iterator i = featureCollection.getFeatures().iterator();
                i.hasNext();) {
            Feature feature = (Feature) i.next();
            transaction.createFeature(feature);
        }

        transaction.commit();
    }

    private Layer inputLayer() {
        return toolboxPanel.getInputLayerComboBox().getSelectedLayer();
    }
    
    // HACK: I invented this because it doesn't exist to compile
	 // We think that this method was renamed to isRollingBackInvalidEdits which exists
	 // in a superclass AbstractCursorTool. 
	 // This rename broke our class
	 // We have implemented the following fix as a guess.
	 //  
    private boolean isRollingBackEdits() { 
    	return !isRollingBackInvalidEdits(); 
    }
    
}
