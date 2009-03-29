package com.vividsolutions.jcs.jump;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelProxy;

/**
 * No need to use this fix if you're using the latest JUMP.
 */
public class FUTURE_FixLayerViewPanelFocusPlugIn extends AbstractPlugIn {
    public void initialize(final PlugInContext context) throws Exception {
        FUTURE_WorkbenchFrame.initializeCurrentAndFutureInternalFrames(context
                .getWorkbenchFrame(), new Block() {
            public Object yield(Object arg) {
                if (!(arg instanceof LayerViewPanelProxy)) { return null; }
                (((LayerViewPanelProxy) arg).getLayerViewPanel())
                        .addMouseListener(new MouseAdapter() {
                            public void mouseEntered(MouseEvent e) {
                                //Re-activate WorkbenchFrame. Otherwise, user
                                //may try entering a quasi-mode by pressing a modifier key --
                                //nothing will happen because the WorkbenchFrame does not 
                                //have focus. [Jon Aquino]
                                //JavaDoc for #toFront says some platforms will not 
                                //activate the window. So use #requestFocus instead. 
                                //[Jon Aquino 12/9/2003]
                                context.getWorkbenchFrame().requestFocus();
                            }
                        });
                return null;
            }
        });
    }
}