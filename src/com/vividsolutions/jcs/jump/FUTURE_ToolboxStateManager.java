package com.vividsolutions.jcs.jump;
import java.awt.Component;
import java.awt.event.ActionListener;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxStateManager.Strategy;
public class FUTURE_ToolboxStateManager {
    public static final Strategy DUMMY_STRATEGY = new Strategy() {
        protected void addActionListener(ActionListener actionListener,
                Component component) {
        }
        protected Object getToolboxValue(Component component) {
            return null;
        }
        protected void setToolboxValue(Object value, Component component) {
        }
    };
}