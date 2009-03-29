package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;

import com.vividsolutions.jcs.jump.FUTURE_PreventableConfirmationDialog;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.event.*;

public class ConfirmationOptionsPanel extends JPanel implements OptionsPanel {

    private BorderLayout borderLayout1 = new BorderLayout();

    private JPanel centrePanel = new JPanel();

    private GridBagLayout gridBagLayout1 = new GridBagLayout();

    private JButton button = new JButton();


    private WorkbenchContext context;
    private JTextArea instructionsTextArea = new JTextArea();

    public ConfirmationOptionsPanel(WorkbenchContext context) {
        this.context = context;
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        instructionsTextArea.setFont(new JLabel().getFont());
    }

    void jbInit() throws Exception {
        this.setLayout(borderLayout1);
        centrePanel.setLayout(gridBagLayout1);
        button.setText("Show all confirmation dialogs");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                button_actionPerformed(e);
            }
        });
        instructionsTextArea.setOpaque(false);
        instructionsTextArea.setEditable(false);
        instructionsTextArea.setMargin(new Insets(12, 12, 12, 12));
        instructionsTextArea.setText("Some confirmation dialogs have a \"Do Not Show Again\" checkbox that " +
    "enables you to prevent them from appearing in the future. Press the " +
    "button below to reset these checkboxes.");
        instructionsTextArea.setLineWrap(true);
        instructionsTextArea.setWrapStyleWord(true);
        this.add(centrePanel, BorderLayout.CENTER);
        centrePanel.add(button, new GridBagConstraints(0, 0, 1, 1, 0.0,
                0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(instructionsTextArea, BorderLayout.NORTH);
    }

    public String validateInput() {
        return null;
    }

    public void okPressed() {

    }

    public void init() {
    }

    void button_actionPerformed(ActionEvent e) {
        for (Iterator i = new ArrayList(PersistentBlackboardPlugIn.get(context)
                .getProperties().keySet()).iterator(); i.hasNext();) {
            String key = (String) i.next();
            if (key.indexOf(FUTURE_PreventableConfirmationDialog.class
                    .getName()) > -1) {
                PersistentBlackboardPlugIn.get(context).put(key, false);
            }
        }
    }
}