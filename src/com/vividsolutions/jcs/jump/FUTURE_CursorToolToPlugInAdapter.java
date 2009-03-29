package com.vividsolutions.jcs.jump;

import java.awt.Point;
import java.awt.event.MouseEvent;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;

public class FUTURE_CursorToolToPlugInAdapter extends AbstractPlugIn {
    private CursorTool tool;

    public FUTURE_CursorToolToPlugInAdapter(CursorTool tool) {
        this.tool = tool;
    }
    
    public boolean execute(PlugInContext context) throws Exception {
        Assert.isTrue(context.getLayerViewPanel() != null);
        Assert.isTrue(p(context) != null);
        tool.activate(context.getLayerViewPanel());
        try {
            doClick(tool, p(context).x, p(context).y, context.getLayerViewPanel());
        }
        finally {
            tool.deactivate();
        }
        return true;
    }
    
    public static void doClick(CursorTool tool, int x, int y, LayerViewPanel panel) {
        tool.mousePressed(createEvent(x, y, panel));
        tool.mouseReleased(createEvent(x, y, panel));
        tool.mouseClicked(createEvent(x, y, panel));
    }

    public String getName() {
        return tool.getName();
    }

    private static MouseEvent createEvent(int x, int y, LayerViewPanel panel) {
        return new MouseEvent(panel, 0, 0, MouseEvent.BUTTON1_MASK, 
                x, y, 1, false, MouseEvent.BUTTON1);
    }

    private Point p(PlugInContext context) {
        return context.getLayerViewPanel().getLastClickedPoint();
    }
}
