package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Color;
import java.awt.geom.NoninvertibleTransformException;

import javax.swing.SwingUtilities;

import com.vividsolutions.jcs.jump.FUTURE_CursorToolToPlugInAdapter;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.cursortool.DelegatingTool;
import com.vividsolutions.jump.workbench.ui.cursortool.DummyTool;

public class CommitOrPreciseMatchTool extends DelegatingTool {

    public CommitOrPreciseMatchTool(CommitTool.Mode mode, String cursorImage, String buttonImage,
            Color color, WorkbenchContext context) {
        super(new DummyTool());
        preciseMatchTool = new PreciseMatchTool(mode, context) {

            public void cancelGesture() {
                super.cancelGesture();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setDelegate(commitTool);
                    }
                });
            }
            protected void finishGesture() throws Exception {
                super.finishGesture();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setDelegate(commitTool);
                    }
                });
            }
        };
        commitTool = new CommitTool(mode, cursorImage, buttonImage, color, context) {

            protected void gestureFinished() throws Exception {
                if (wasClick()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            setDelegate(preciseMatchTool);
                            try {
                                FUTURE_CursorToolToPlugInAdapter.doClick(
                                        preciseMatchTool,
                                        (int) getViewDestination().getX(),
                                        (int) getViewDestination().getY(),
                                        getPanel());
                            } catch (NoninvertibleTransformException e) {
                                getWorkbench().getContext().getErrorHandler()
                                        .handleThrowable(e);
                            }
                        }
                    });
                    return;
                }
                super.gestureFinished();
            }
            protected boolean wasClick() {
                try {
                    //A bit fuzzier than usual; otherwise, true clicks
                    //will go to the PreciseMatchTool, whereas near
                    //clicks will go to the CommitTool -- the user may
                    //be confused. [Jon Aquino 2004-02-10]
                    return getViewSource().distance(getViewDestination()) < 3;
                } catch (NoninvertibleTransformException e) {
                    return false;
                }
            }
        };
        setDelegate(commitTool);
    }

    private CommitTool commitTool;

    private PreciseMatchTool preciseMatchTool;

}
