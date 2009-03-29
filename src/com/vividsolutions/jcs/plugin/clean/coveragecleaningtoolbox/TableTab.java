package com.vividsolutions.jcs.plugin.clean.coveragecleaningtoolbox;
import java.awt.Component;

import javax.swing.JPanel;

import com.vividsolutions.jcs.jump.FUTURE_OneLayerAttributeTab;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.InfoModel;
import com.vividsolutions.jump.workbench.ui.TaskFrameProxy;
public class TableTab extends Tab {
    private Layer lastLayer;
    private Block lastCriterionForAddingFeatures;
    public TableTab(String sortAttributeName) {
        this.sortAttributeName = sortAttributeName;
    }
    protected FUTURE_OneLayerAttributeTab createAttributeTab(
            WorkbenchContext context) {
        return new FUTURE_OneLayerAttributeTab(new InfoModel(), context,
                ((TaskFrameProxy) context.getWorkbench().getFrame()
                        .getActiveInternalFrame()).getTaskFrame(), context);
    }
    public Component createDefaultChild() {
        return new JPanel();
    }
    public TableTab setLayer(final Layer layer, WorkbenchContext context) {
        if (layer == lastLayer) {
            return this;
        }
        alterModel(context, new Block() {
            public Object yield() {
                return ((FUTURE_OneLayerAttributeTab) getChild())
                        .setLayer(layer);
            }
        });
        lastLayer = layer;
        return this;
    }
    public TableTab setCriterionForAddingFeatures(WorkbenchContext context,
            final Block criterionForAddingFeatures) {
        if (criterionForAddingFeatures == lastCriterionForAddingFeatures) {
            return this;
        }
        alterModel(context, new Block() {
            public Object yield() {
                ((FUTURE_OneLayerAttributeTab) getChild())
                        .setCriterionForAddingFeatures(criterionForAddingFeatures);
                return null;
            }
        });
        lastCriterionForAddingFeatures = criterionForAddingFeatures;
        return this;
    }
    private void alterModel(WorkbenchContext context, Block block) {
        if (!(getChild() instanceof FUTURE_OneLayerAttributeTab)) {
            setChild(createAttributeTab(context));
        }
        block.yield();
        if (sortAttributeName != null) {
            ((FUTURE_OneLayerAttributeTab) getChild()).getLayerTableModel()
                    .sort(sortAttributeName, false);
        }
    }
    private String sortAttributeName;
    public Layer getLayer() {
        if (!(getChild() instanceof FUTURE_OneLayerAttributeTab)) {
            return null;
        }
        return ((FUTURE_OneLayerAttributeTab) getChild()).getLayer();
    }
    public void refresh() {
        if (getChild() instanceof FUTURE_OneLayerAttributeTab) {
            ((FUTURE_OneLayerAttributeTab) getChild()).refresh();
        }
    }
}