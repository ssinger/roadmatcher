package com.vividsolutions.jcs.jump;

import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.JInternalFrame;

import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;

public class FUTURE_WorkbenchFrame {
    public static void initializeCurrentAndFutureInternalFrames(WorkbenchFrame workbenchFrame, final Block block) {
            workbenchFrame.getDesktopPane().addContainerListener(new ContainerAdapter() {
                public void componentAdded(ContainerEvent e) {
                    if (!(e.getChild() instanceof JInternalFrame)) {
                        return;
                    }
                    block.yield((JInternalFrame) e.getChild());
                }
            });
            for (Iterator i = Arrays.asList(workbenchFrame.getInternalFrames()).iterator(); i.hasNext(); ) {
                JInternalFrame internalFrame = (JInternalFrame) i.next();
                block.yield(internalFrame);
            }
    }
}
