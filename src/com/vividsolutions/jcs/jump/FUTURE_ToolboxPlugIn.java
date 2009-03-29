package com.vividsolutions.jcs.jump;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxPlugIn;
public abstract class FUTURE_ToolboxPlugIn extends ToolboxPlugIn {
    //no ellipsis [Jon Aquino 2004-05-12]
    public void createMainMenuItem(String[] menuPath, Icon icon,
            final WorkbenchContext context) throws Exception {
        new FeatureInstaller(context).addMainMenuItem(this, menuPath,
                getName(), true, icon, new EnableCheck() {
                    public String check(JComponent component) {
                        ((JCheckBoxMenuItem) component).setSelected(getToolbox(
                                context).isVisible());
                        return null;
                    }
                });
    }
}