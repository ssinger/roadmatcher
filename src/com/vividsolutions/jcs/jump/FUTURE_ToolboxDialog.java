package com.vividsolutions.jcs.jump;

import java.util.Collections;
import java.util.List;

import javax.swing.AbstractButton;

import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxDialog;

public class FUTURE_ToolboxDialog {
    public static List getToolBars(ToolboxDialog toolboxDialog) {
        return Collections
                .unmodifiableList((List) FUTURE_LangUtil.getPrivateField(
                        "toolBars", toolboxDialog, ToolboxDialog.class));
    }
    public static void _registerButton(ToolboxDialog toolboxDialog, AbstractButton button) {
        FUTURE_LangUtil.invokePrivateMethod("registerButton", toolboxDialog, ToolboxDialog.class,
                new Object[] {button, null}, new Class[] {AbstractButton.class, EnableCheck.class});
    }    
}
