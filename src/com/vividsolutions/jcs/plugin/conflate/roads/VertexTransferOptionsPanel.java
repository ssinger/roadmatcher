package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.event.ActionEvent;
import javax.swing.*;
import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.vertextransfer.ClosestPointVertexTransferOp;
import com.vividsolutions.jcs.conflate.roads.vertextransfer.ProportionalLengthVertexTransferOp;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import java.awt.*;
public class VertexTransferOptionsPanel extends JPanel implements OptionsPanel {
    private WorkbenchContext context;
    public VertexTransferOptionsPanel(WorkbenchContext context) {
        this.context = context;
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    void jbInit() throws Exception {
        from0To1CheckBox.setText("From 0 to 1");
        this.setLayout(gridBagLayout1);
        transferVerticesCheckBox.setText("Transfer vertices");
        transferVerticesCheckBox
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        transferVerticesCheckBox_actionPerformed(e);
                    }
                });
        from1To0CheckBox.setText("From 1 to 0");
        algorithmPanel.setLayout(gridBagLayout2);
        algorithmLabel.setText("Algorithm: ");
        fillerPanel1.setPreferredSize(new Dimension(12, 12));
        fillerPanel1.setLayout(gridBagLayout4);
        fillerPanel2.setLayout(gridBagLayout3);
        this.add(transferVerticesCheckBox, new GridBagConstraints(1, 1, 1, 1,
                0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(from0To1CheckBox, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
                        20, 0, 0), 0, 0));
        this.add(from1To0CheckBox, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
                        20, 0, 0), 0, 0));
        this.add(algorithmPanel, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
                        20, 0, 0), 0, 0));
        this.add(fillerPanel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
                        0, 0, 0, 0), 0, 0));
        algorithmPanel.add(algorithmLabel, new GridBagConstraints(0, 0, 1, 1,
                0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        algorithmPanel.add(algorithmComboBox, new GridBagConstraints(1, 0, 1,
                1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(fillerPanel2, new GridBagConstraints(2, 6, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
                        0, 0, 0, 0), 0, 0));
    }
    private ConflationSession getSession() {
        return ToolboxModel.instance(context).getSession();
    }
    void transferVerticesCheckBox_actionPerformed(ActionEvent e) {
        updateEnabledState();
    }
    private void updateEnabledState() {
        from0To1CheckBox.setEnabled(transferVerticesCheckBox.isSelected());
        from1To0CheckBox.setEnabled(transferVerticesCheckBox.isSelected());
        algorithmLabel.setEnabled(transferVerticesCheckBox.isSelected());
        algorithmComboBox.setEnabled(transferVerticesCheckBox.isSelected());
    }
    private void updateText(ConflationSession session) {
        from0To1CheckBox.setText("From "
                + session.getSourceNetwork(0).getName() + " to "
                + session.getSourceNetwork(1).getName());
        from1To0CheckBox.setText("From "
                + session.getSourceNetwork(1).getName() + " to "
                + session.getSourceNetwork(0).getName());
    }
    private VertexTransferProperties getProperties() {
        return ResultOptions.get(getSession())
                .getVertexTransferProperties();
    }
    private JComboBox algorithmComboBox = new JComboBox(new Object[]{
            ClosestPointVertexTransferOp.class,
            ProportionalLengthVertexTransferOp.class}) {
        {
            setRenderer(new DefaultListCellRenderer() {
                public Component getListCellRendererComponent(JList list,
                        Object value, int index, boolean isSelected,
                        boolean cellHasFocus) {
                    return super.getListCellRendererComponent(list, StringUtil
                            .toFriendlyName(((Class) value).getName(),
                                    "VertexTransferOp"), index, isSelected,
                            cellHasFocus);
                }
            });
        }
    };
    private JLabel algorithmLabel = new JLabel();
    private JPanel algorithmPanel = new JPanel();
    private JCheckBox from0To1CheckBox = new JCheckBox();
    private JCheckBox from1To0CheckBox = new JCheckBox();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private GridBagLayout gridBagLayout2 = new GridBagLayout();
    private JCheckBox transferVerticesCheckBox = new JCheckBox();
    private JPanel fillerPanel1 = new JPanel();
    private JPanel fillerPanel2 = new JPanel();
    private GridBagLayout gridBagLayout3 = new GridBagLayout();
    private GridBagLayout gridBagLayout4 = new GridBagLayout();
    public String validateInput() {
        return null;
    }
    public void okPressed() {
        getProperties().setTransferringVertices(
                transferVerticesCheckBox.isSelected());
        getProperties().setTransferringVerticesFrom0To1(
                from0To1CheckBox.isSelected());
        getProperties().setTransferringVerticesFrom1To0(
                from1To0CheckBox.isSelected());
        getProperties().setVertexTransferOpClass(
                (Class) algorithmComboBox.getSelectedItem());
    }
    public void init() {
        transferVerticesCheckBox.setSelected(getProperties()
                .isTransferringVertices());
        from0To1CheckBox.setSelected(getProperties()
                .isTransferringVerticesFrom0To1());
        from1To0CheckBox.setSelected(getProperties()
                .isTransferringVerticesFrom1To0());
        algorithmComboBox.setSelectedItem(getProperties()
                .getVertexTransferOpClass());
        updateText(ToolboxModel.instance(context.getLayerManager(), context)
                .getSession());
        updateEnabledState();
    }
}