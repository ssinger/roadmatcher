package com.vividsolutions.jcs.jump;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelContext;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;

public class FUTURE_PreventableConfirmationDialog extends JOptionPane {

    private FUTURE_PreventableConfirmationDialog() {
        super("Message", WARNING_MESSAGE, DEFAULT_OPTION);
    }

    public JDialog createDialog(Component parentComponent, String title) {
        JButton proceedButton = new JButton(proceedButtonText);
        JButton cancelButton = new JButton(cancelButtonText);
        setOptions(cancelButtonText != null ? new Object[] { proceedButton,
                cancelButton} : new Object[] { proceedButton});
        final JDialog dialog = super.createDialog(parentComponent, title);
        proceedButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                proceedButtonPressed = true;
                dialog.setVisible(false);
            }
        });
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        });
        dialog.getContentPane().add(doNotShowAgainCheckbox, BorderLayout.SOUTH);
        dialog.pack();

        if (parentComponent != null) {
            //For testing. [Jon Aquino]
            GUIUtil.centreOnWindow(dialog);
        }

        return dialog;
    }

    private String cancelButtonText;

    private JCheckBox doNotShowAgainCheckbox = new JCheckBox(
            "Just warn me on the status line in the future");

    private boolean proceedButtonPressed = false;

    private String proceedButtonText;

    public static boolean show(Component parentComponent, String title,
            String statusLineWarning, String dialogText,
            String proceedButtonText, String cancelButtonText,
            String doNotShowAgainID, Blackboard blackboard,
            LayerViewPanelContext context) {
        //Tag the ID with this class' name, so that we can find it again
        //when the Turn All Warning Dialogs Back On button is pressed.
        //[Jon Aquino 2004-02-23]
        String fullDoNotShowAgainID = FUTURE_PreventableConfirmationDialog.class
                .getName()
                + " - " + doNotShowAgainID;
        if (blackboard.get(fullDoNotShowAgainID, false)) {
            if (statusLineWarning != null) {
                context.warnUser(statusLineWarning);
            }
            return true;
        }
        FUTURE_PreventableConfirmationDialog dialog = new FUTURE_PreventableConfirmationDialog();
        if (statusLineWarning == null) {
            dialog.doNotShowAgainCheckbox.setText("Don't show me this warning in the future");
        }
        dialog.proceedButtonText = proceedButtonText;
        dialog.cancelButtonText = cancelButtonText;
        dialog.setMessage(StringUtil.split(dialogText, 80));
        dialog.createDialog(parentComponent, title).setVisible(true);
        blackboard.put(fullDoNotShowAgainID, dialog.doNotShowAgainCheckbox
                .isSelected());
        return dialog.proceedButtonPressed;
    }

    public static boolean show(WorkbenchFrame f, String title,
            String statusLineWarning, String dialogText,
            String proceedButtonText, String cancelButtonText,
            String doNotShowAgainID) {
        return show(f, title, statusLineWarning, dialogText, proceedButtonText,
                cancelButtonText, doNotShowAgainID, PersistentBlackboardPlugIn
                        .get(f.getContext()), f);
    }

}
