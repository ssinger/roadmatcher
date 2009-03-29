package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
public class AutoAdjustOptionsPanel extends JPanel implements OptionsPanel {
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JCheckBox autoAdjustAfterManualCommitCheckBox = new JCheckBox();
	private WorkbenchContext context;
	private JPanel fillerPanel1 = new JPanel();
	private GridBagLayout gridBagLayout2 = new GridBagLayout();
	private JPanel fillerPanel2 = new JPanel();
	private GridBagLayout gridBagLayout3 = new GridBagLayout();
	public AutoAdjustOptionsPanel(WorkbenchContext context) {
		this.context = context;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	void jbInit() throws Exception {
		this.setLayout(gridBagLayout1);
		this.setFont(new java.awt.Font("Dialog", 1, 12));
		fillerPanel1.setPreferredSize(new Dimension(12, 12));
		fillerPanel1.setLayout(gridBagLayout2);
		fillerPanel2.setLayout(gridBagLayout3);
		autoAdjustAfterManualCommitCheckBox
				.setText("AutoAdjust after manual Commit");
		autoAdjustAfterManualCommitCheckBox
				.setToolTipText("<html>"
						+ StringUtil
								.replaceAll(
										StringUtil
												.split(
														"Check this box to have RoadMatcher try to automatically fix inconsistent nodes of road segments that you Match/Commit or Path Match",
														80), "\n", "<br>")
						+ "</html>");
		this.add(autoAdjustAfterManualCommitCheckBox, new GridBagConstraints(1,
				6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(fillerPanel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(fillerPanel2, new GridBagConstraints(3, 7, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));
	}
	public String validateInput() {
		return null;
	}
	public void okPressed() {
		AutoAdjustAfterManualCommitOp.setAutoAdjustingAfterManualCommit(
				autoAdjustAfterManualCommitCheckBox.isSelected(), context);
	}
	public void init() {
		autoAdjustAfterManualCommitCheckBox
				.setSelected(AutoAdjustAfterManualCommitOp
						.isAutoAdjustingAfterManualCommit(context));
	}
}