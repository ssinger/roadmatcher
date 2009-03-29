package com.vividsolutions.jcs.plugin.clean.coveragecleaningtoolbox;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import com.vividsolutions.jump.workbench.ui.ActionEventFirer;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxStateManager;

/**
 * A part of a ToolboxDialog that displays a different component for each Task.
 * Managed by ToolboxStateManager. Perhaps inappropriately, named "Tab"
 * because it is often a child of a JTabbedPane, though this need not be the
 * case.
 */
//Jon Aquino [2004-02-09]
public abstract class Tab extends JPanel {

    public Tab() {
        super(new BorderLayout());
        setChild(createDefaultChild());
    }

    public void addActionListener(ActionListener listener) {
        firer.add(listener);
    }

    public abstract Component createDefaultChild();

    public Component getChild() {
        return child;
    }

    public void setChild(Component child) {
        remove(this.child);
        add(child, BorderLayout.CENTER);
        this.child = child;
        firer.fire(this, 0, null);
        repaint();
    }

    private Component child = new JPanel();

    private ActionEventFirer firer = new ActionEventFirer();

    public static final ToolboxStateManager.Strategy STRATEGY = new ToolboxStateManager.Strategy() {
        protected void addActionListener(ActionListener actionListener,
                Component component) {
            ((Tab) component).addActionListener(actionListener);
        }

        protected Object getToolboxValue(Component component) {
            return ((Tab) component).getChild();
        }

        protected void setToolboxValue(Object value, Component component) {
            ((Tab) component).setChild((Component) value);
        }

        protected Object getDefaultValue(Object initialToolboxValue,
                Component component) {
            return ((Tab) component).createDefaultChild();
        }
    };
}
