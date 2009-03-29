package com.vividsolutions.jcs.jump;

import java.util.Iterator;
import java.util.Map;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.AttributePanel;
import com.vividsolutions.jump.workbench.ui.AttributeTablePanel;
import com.vividsolutions.jump.workbench.ui.AttributePanel.Row;

public class FUTURE_AttributePanel {

    /**
     * Fixed to handle tables without rows, possibly between tables with rows
     */
    private static class FUTURE_BasicRow implements AttributePanel.Row {

        public FUTURE_BasicRow(AttributeTablePanel panel, int index,
                AttributePanel parentPanel) {
            this.parentPanel = parentPanel;
            this.panel = panel;
            this.index = index;
        }

        public Feature getFeature() {
            return panel.getModel().getFeature(index);
        }

        public int getIndex() {
            return index;
        }

        public AttributeTablePanel getPanel() {
            return panel;
        }

        private Row getRowAbove() {
            if (index > 0) { return new FUTURE_BasicRow(panel, index - 1,
                    parentPanel); }
            return rowAbove(panel, parentPanel);
        }

        private Row getRowBelow() {
            if (index < panel.getTable().getRowCount() - 1) { return new FUTURE_BasicRow(
                    panel, index + 1, parentPanel); }
            return rowBelow(panel, parentPanel);
        }

        public boolean isFirstRow() {
            return getRowAbove() == null;
        }

        public boolean isLastRow() {
            return getRowBelow() == null;
        }

        public AttributePanel.Row nextRow() {
            if (isLastRow()) { return this; }
            return getRowBelow();
        }

        public AttributePanel.Row previousRow() {
            if (isFirstRow()) { return this; }
            return getRowAbove();
        }

        private AttributePanel parentPanel;

        private int index;

        private AttributeTablePanel panel;

        private static int index(AttributeTablePanel panel,
                AttributePanel parentPanel) {
            return parentPanel.getModel().getLayers().indexOf(
                    panel.getModel().getLayer());
        }

        private static Row rowAbove(AttributeTablePanel panel,
                AttributePanel parentPanel) {
            if (index(panel, parentPanel) == 0) { return null; }
            AttributeTablePanel panelAbove = parentPanel
                    .getTablePanel((Layer) parentPanel.getModel().getLayers()
                            .get(index(panel, parentPanel) - 1));
            if (panelAbove.getTable().getRowCount() > 0) { return new FUTURE_BasicRow(
                    panelAbove, panelAbove.getTable().getRowCount() - 1,
                    parentPanel); }
            return rowAbove(panelAbove, parentPanel);
        }

        private static Row rowBelow(AttributeTablePanel panel,
                AttributePanel parentPanel) {
            if (index(panel, parentPanel) == parentPanel.getModel().getLayers()
                    .size() - 1) { return null; }
            AttributeTablePanel panelBelow = parentPanel
                    .getTablePanel((Layer) parentPanel.getModel().getLayers()
                            .get(index(panel, parentPanel) + 1));
            if (panelBelow.getTable().getRowCount() > 0) { return new FUTURE_BasicRow(
                    panelBelow, 0, parentPanel); }
            return rowBelow(panelBelow, parentPanel);
        }
    }

    public static AttributePanel.Row topSelectedRow(final AttributePanel parentPanel) {
        for (Iterator i = ((Map) FUTURE_LangUtil.getPrivateField(
                "layerToTablePanelMap", parentPanel, AttributePanel.class))
                .values().iterator(); i.hasNext(); ) {
            AttributeTablePanel panel = (AttributeTablePanel) i.next();
            int selectedRow = panel.getTable().getSelectedRow();
            if (selectedRow == -1) {
                continue;
            }
            return new FUTURE_BasicRow(panel, selectedRow, parentPanel);
        }
        AttributePanel.Row nullRow = new AttributePanel.Row() {
            public boolean isFirstRow() {
                return false;
            }
            public boolean isLastRow() {
                return false;
            }
            public AttributeTablePanel getPanel() {
                throw new UnsupportedOperationException();
            }
            public int getIndex() {
                throw new UnsupportedOperationException();
            }
            public Row nextRow() {
                return firstRow();
            }
            public Row previousRow() {
                return firstRow();
            }
            private Row firstRow() {
                for (Iterator i = parentPanel.getModel().getLayers().iterator(); i.hasNext(); ) {
                    Layer layer = (Layer) i.next();
                    if (parentPanel.getTablePanel(layer).getTable().getRowCount() > 0) {
                        return new FUTURE_BasicRow(parentPanel.getTablePanel(layer), 0, parentPanel);
                    }
                }
                Assert.shouldNeverReachHere();
                return null;
            }
            public Feature getFeature() {
                throw new UnsupportedOperationException();
            }
        };    
        return nullRow;
    }

}
