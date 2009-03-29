package com.vividsolutions.jcs.jump;

import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.ui.InfoModel;
import com.vividsolutions.jump.workbench.ui.LayerTableModel;
import com.vividsolutions.jump.workbench.ui.TaskFrame;

public class FUTURE_OneLayerAttributeTab extends FUTURE_LayerAttributeTab {

    public FUTURE_OneLayerAttributeTab(InfoModel infoModel, WorkbenchContext context,
            TaskFrame taskFrame, LayerManagerProxy layerManagerProxy) {
        super(infoModel, context, taskFrame, layerManagerProxy);
    }

    public Layer getLayer() {
        //null LayerTableModel if for example the user has just removed the
        // layer
        //from the LayerManager [Jon Aquino]
        return (getLayerTableModel() != null) ? getLayerTableModel().getLayer()
                : null;
    }

    public FUTURE_OneLayerAttributeTab setLayer(Layer layer) {
        if (!getModel().getLayers().isEmpty()) {
            getModel().clear();
        }

        //InfoModel#add must be called after the AttributeTab is
        //created; otherwise layer won't be added to the Attribute Tab
        //-- the AttributeTab listens for the event fired by
        //InfoModel#add. [Jon Aquino]
        getModel().add(
                layer,
                CollectionUtil.select(layer.getFeatureCollectionWrapper()
                        .getFeatures(), getCriterionForAddingFeatures()));
        return this;
    }

    public LayerTableModel getLayerTableModel() {
        return (!getModel().getLayerTableModels().isEmpty()) ? (LayerTableModel) getModel()
                .getLayerTableModels().iterator().next()
                : null;
    }

    public void setCriterionForAddingFeatures(Block criterionForAddingFeatures) {
        super.setCriterionForAddingFeatures(criterionForAddingFeatures);
        refresh();
    }

    public void refresh() {
        setLayer(getLayer());
    }

}
