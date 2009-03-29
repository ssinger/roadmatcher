package com.vividsolutions.jcs.jump;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.*;
import com.vividsolutions.jump.workbench.ui.InfoModel;
import com.vividsolutions.jump.workbench.ui.TaskFrame;

/**
 * Displays and stays in sync with one or more whole Layers.
 */
public class FUTURE_LayerAttributeTab extends FUTURE_AttributeTab {

    public FUTURE_LayerAttributeTab(InfoModel infoModel, WorkbenchContext context,
            TaskFrame taskFrame, LayerManagerProxy layerManagerProxy) {
        super(infoModel, context, taskFrame, layerManagerProxy);
        context.getLayerManager().addLayerListener(new LayerListener() {

            public void categoryChanged(CategoryEvent e) {
            }

            public void featuresChanged(FeatureEvent e) {
                if (getModel().getLayerTableModels().isEmpty()) { 
                //Get here after attribute viewer window is closed [Jon
                // Aquino]
                return; }
                for (Iterator i = getModel().getLayers().iterator(); i
                        .hasNext(); ) {
                    Layer layer = (Layer) i.next();
                    if (e.getLayer() != layer) {
                        continue;
                    }
                    if (e.getType() == FeatureEventType.DELETED) {
                        //DELETED events are already handled in LayerTableModel
                        //[Jon Aquino 2004-02-03]
                        continue;
                    }
                    if (e.getType() == FeatureEventType.ADDED) {
                        add(layer, CollectionUtil.select(e.getFeatures(),
                                criterionForAddingFeatures));
                        continue;
                    }                    
                    
                    Collection featuresToAdd = new ArrayList(e.getFeatures());
                    featuresToAdd.removeAll(getModel().getTableModel(layer)
                            .getFeatures());
                    featuresToAdd = CollectionUtil.select(featuresToAdd,
                            criterionForAddingFeatures);
                    add(layer, featuresToAdd);
                    Collection featuresToRemove = new ArrayList(e.getFeatures());
                    featuresToRemove.retainAll(getModel().getTableModel(layer)
                            .getFeatures());
                    featuresToRemove = CollectionUtil.select(featuresToRemove,
                            FUTURE_Block.inverse(criterionForAddingFeatures));
                    remove(layer, featuresToRemove);
                }
            }

            private void add(Layer layer, Collection features) {
                if (features.isEmpty()) { return; }
                getModel().getTableModel(layer).addAll(features);
            }
            
            private void remove(Layer layer, Collection features) {
                if (features.isEmpty()) { return; }
                getModel().getTableModel(layer).removeAll(features);
            }            

            public void layerChanged(LayerEvent e) {
            }
        });
    }

    private Block criterionForAddingFeatures = new Block() {
        public Object yield(Object arg) {
            return Boolean.TRUE;
        }
    };

    protected Block getCriterionForAddingFeatures() {
        return criterionForAddingFeatures;
    }
    protected void setCriterionForAddingFeatures(
            Block criterionForAddingFeatures) {
        this.criterionForAddingFeatures = criterionForAddingFeatures;
    }
}
