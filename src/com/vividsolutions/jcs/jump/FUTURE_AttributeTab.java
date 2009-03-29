package com.vividsolutions.jcs.jump;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.NoninvertibleTransformException;

import javax.swing.JButton;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.*;
import com.vividsolutions.jump.workbench.ui.cursortool.FeatureInfoTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInfoPlugIn;

public class FUTURE_AttributeTab extends AttributeTab {

    public FUTURE_AttributeTab(InfoModel model,
            WorkbenchContext workbenchContext, TaskFrame taskFrame,
            LayerManagerProxy layerManagerProxy) {
        super(model, workbenchContext, taskFrame, layerManagerProxy);
        AttributePanel panel = (AttributePanel) FUTURE_LangUtil
                .getPrivateField("panel", this, AttributeTab.class);
        FUTURE_LangUtil.setPrivateField("zoomToSelectedItemsPlugIn",
                new FUTURE_ZoomToSelectedItemsPlugIn(), panel,
                AttributePanel.class);
        this.toolBar = (EnableableToolBar) FUTURE_LangUtil.getPrivateField(
                "toolBar", this, AttributeTab.class);
        this.panel = (AttributePanel) FUTURE_LangUtil.getPrivateField("panel",
                this, AttributeTab.class);
        this.errorHandler = (ErrorHandler) FUTURE_LangUtil.getPrivateField(
                "errorHandler", this, AttributeTab.class);
        this.taskFrameEnableCheck = (EnableCheck) FUTURE_LangUtil
                .getPrivateField("taskFrameEnableCheck", this,
                        AttributeTab.class);
        this.layersEnableCheck = (EnableCheck) FUTURE_LangUtil.getPrivateField(
                "layersEnableCheck", this, AttributeTab.class);
        this.rowsSelectedEnableCheck = (EnableCheck) FUTURE_LangUtil
                .getPrivateField("rowsSelectedEnableCheck", this,
                        AttributeTab.class);
        toolBar.removeAll();
        installToolBarButtons(workbenchContext, taskFrame);
    }

    private void installToolBarButtons(final WorkbenchContext workbenchContext,
            final TaskFrame taskFrame) {
        toolBar.add(new JButton(), "Zoom To Previous Row", IconLoader
                .icon("SmallUp.gif"), new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (panel.rowCount() == 0) { return; }
                    _zoom(FUTURE_AttributePanel.topSelectedRow(panel).previousRow());
                } catch (Throwable t) {
                    errorHandler.handleThrowable(t);
                }
            }
        }, new MultiEnableCheck().add(taskFrameEnableCheck).add(
                layersEnableCheck));
        toolBar.add(new JButton(), "Zoom To Next Row", IconLoader
                .icon("SmallDown.gif"), new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (panel.rowCount() == 0) { return; }
                    _zoom(FUTURE_AttributePanel.topSelectedRow(panel).nextRow());
                } catch (Throwable t) {
                    errorHandler.handleThrowable(t);
                }
            }
        }, new MultiEnableCheck().add(taskFrameEnableCheck).add(
                layersEnableCheck));
        toolBar.add(new JButton(), "Zoom To Selected Rows", IconLoader
                .icon("SmallMagnify.gif"), new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    panel.zoom(panel.selectedFeatures());
                } catch (Throwable t) {
                    errorHandler.handleThrowable(t);
                }
            }
        }, new MultiEnableCheck().add(taskFrameEnableCheck).add(
                layersEnableCheck).add(rowsSelectedEnableCheck));
        toolBar.add(new JButton(), "Zoom To Full Extent", IconLoader
                .icon("SmallWorld.gif"), new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    taskFrame.getLayerViewPanel().getViewport()
                            .zoomToFullExtent();
                } catch (Throwable t) {
                    errorHandler.handleThrowable(t);
                }
            }
        }, new MultiEnableCheck().add(taskFrameEnableCheck).add(
                layersEnableCheck));
        toolBar.add(new JButton(), "Select In Task Window", IconLoader
                .icon("SmallSelect.gif"), new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    panel.selectInLayerViewPanel();
                } catch (Throwable t) {
                    errorHandler.handleThrowable(t);
                }
            }
        }, new MultiEnableCheck().add(taskFrameEnableCheck).add(
                layersEnableCheck).add(rowsSelectedEnableCheck));
        toolBar.add(new JButton(), "Flash Selected Rows", IconLoader
                .icon("Flashlight.gif"), new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    panel.flashSelectedFeatures();
                } catch (Throwable t) {
                    errorHandler.handleThrowable(t);
                }
            }
        }, new MultiEnableCheck().add(taskFrameEnableCheck).add(
                layersEnableCheck).add(rowsSelectedEnableCheck));

        FeatureInfoPlugIn featureInfoPlugIn = new FeatureInfoPlugIn();
        toolBar.add(new JButton(), featureInfoPlugIn.getName(), GUIUtil
                .toSmallIcon(FeatureInfoTool.ICON), FeatureInfoPlugIn
                .toActionListener(featureInfoPlugIn, workbenchContext, null),
                FeatureInfoPlugIn.createEnableCheck(workbenchContext));
    }

    private void _zoom(AttributePanel.Row row)
            throws NoninvertibleTransformException {
        FUTURE_LangUtil.invokePrivateMethod("zoom", this, AttributeTab.class,
                new Object[]{row}, new Class[]{AttributePanel.Row.class});
    }

    private ErrorHandler errorHandler;

    private EnableCheck layersEnableCheck;

    private AttributePanel panel;

    private EnableCheck rowsSelectedEnableCheck;

    private EnableCheck taskFrameEnableCheck;

    private EnableableToolBar toolBar;

}
