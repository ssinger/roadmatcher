package com.vividsolutions.jcs.jump;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.cursortool.Animations;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.DeleteVertexTool;

public class FUTURE_DeleteVertexTool extends DeleteVertexTool {
    private EnableCheckFactory checkFactory;

    public FUTURE_DeleteVertexTool(EnableCheckFactory checkFactory) {
        super(checkFactory);
        this.checkFactory = checkFactory;
    }

    protected void gestureFinished() throws java.lang.Exception {
        reportNothingToUndoYet();
        if (!check(checkFactory.createAtLeastNItemsMustBeSelectedCheck(1))) {
            return;
        }
        if (!check(checkFactory.createSelectedItemsLayersMustBeEditableCheck())) {
            return;
        }
        final ArrayList verticesDeleted = new ArrayList();
        ArrayList transactions = new ArrayList();
        for (Iterator i =
            getPanel().getSelectionManager().getLayersWithSelectedItems().iterator();
            i.hasNext();
            ) {
            Layer layer = (Layer) i.next();
            transactions.add(createTransaction(layer, verticesDeleted));
        }
        int emptyGeometryCount = EditTransaction.emptyGeometryCount(transactions);
        if (emptyGeometryCount > 0) {
            getPanel().getContext().warnUser(
                "Cancelled -- deletion would result in empty geometry");
            return;
        }
        if (verticesDeleted.isEmpty()) {
            getPanel().getContext().warnUser("No selection handles here");
            return;
        }
        commit(verticesDeleted, transactions);
    }

    protected void commit(final List verticesDeleted, List transactions) {
        EditTransaction.commit(transactions, new EditTransaction.SuccessAction() {
            public void run() {
                try {
                    Animations.drawExpandingRings(
                        getPanel().getViewport().toViewPoints(verticesDeleted),
                        true,
                        Color.red,
                        getPanel(),
                        new float[] { 15, 15 });
                } catch (Throwable t) {
                    getPanel().getContext().warnUser(t.toString());
                }
            }
        });
    }
}
