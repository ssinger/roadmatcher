package com.vividsolutions.jcs.jump;

import java.awt.Container;

import javax.swing.*;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

public class FUTURE_FeatureInstaller extends FeatureInstaller {

    public FUTURE_FeatureInstaller(WorkbenchContext workbenchContext) {
        super(workbenchContext);
    }

    public void addPopupMenuItem(JPopupMenu popupMenu, PlugIn executable,
            String menuPath[], String menuItemName, boolean checkBox, Icon icon,
            EnableCheck enableCheck) {
        super.addPopupMenuItem(popupMenu, executable, menuItemName, checkBox,
                icon, enableCheck);
        JMenuItem menuItem = childMenuItem(menuItemName, popupMenu);
        popupMenu.remove(menuItem);
        createMenusIfNecessary(popupMenu, menuPath).add(menuItem);
        reinstallMnemonic(menuItemName, menuItem);
    }

    private void reinstallMnemonic(String menuItemName, JMenuItem menuItem) {
        menuItem.setText(menuItemName);
        installMnemonic(menuItem, (MenuElement)menuItem.getParent());
    }

    public static JMenuItem childMenuItem(String childName, MenuElement menu) {
        if (menu instanceof JMenu) {
            return childMenuItem(childName, ((JMenu)menu).getPopupMenu());
        }
        MenuElement[] childMenuItems = menu.getSubElements();
        for (int i = 0; i < childMenuItems.length; i++) {
            if (childMenuItems[i] instanceof JMenuItem
                    && ((JMenuItem) childMenuItems[i]).getText().equals(
                            childName)) { return ((JMenuItem) childMenuItems[i]); }
        }
        return null;
    }

    public String[] _behead(String[] a1) {
        return (String[]) FUTURE_LangUtil.invokePrivateMethod("behead", this,
                FeatureInstaller.class, new Object[] { a1},
                new Class[] { String[].class});
    }

    public Container createMenusIfNecessary(MenuElement parent,
            String[] menuPath) {
        if (menuPath.length == 0) { return (Container)parent; }
        MenuElement child = (MenuElement) childMenuItem(menuPath[0], parent);
        if (child == null) {
            child = installMnemonic(new JMenu(menuPath[0]), parent);
            ((Container) parent).add((JComponent) child);
        }
        return createMenusIfNecessary(child, _behead(menuPath));
    }
}
