package com.vividsolutions.jcs.jump;

import javax.swing.JPanel;

import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.ui.InputChangedFirer;
import com.vividsolutions.jump.workbench.ui.InputChangedListener;
import com.vividsolutions.jump.workbench.ui.wizard.WizardPanel;

public abstract class FUTURE_AbstractWizardPanel extends JPanel implements
        WizardPanel {
    public String getTitle() {
        return StringUtil.toFriendlyName(getClass().getName(), "WizardPanel");
    }
    public String getID() {
        return getClass().getName();
    }
    private InputChangedFirer inputChangedFirer = new InputChangedFirer();
    public void add(InputChangedListener listener) {
        inputChangedFirer.add(listener);
    }
    protected void fireInputChanged() {
        inputChangedFirer.fire();
    }
    public void remove(InputChangedListener listener) {
        inputChangedFirer.remove(listener);
    }    
}
