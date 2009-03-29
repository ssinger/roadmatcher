package com.vividsolutions.jcs.plugin.conflate.roads;

import javax.swing.*;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.jump.FUTURE_ValidatingTextField;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.ValidatingTextField;
import java.awt.*;

public class AutoAdjustConflationOptionsPanel extends JPanel implements
		OptionsPanel {
	private GridBagLayout gridBagLayout1 = new GridBagLayout();

	private JLabel adjustmentPrecedenceRuleLabel = new JLabel();

	private JLabel performAdjustmentsLabel = new JLabel();

	private DefaultComboBoxModel datasetComboBoxModel = new DefaultComboBoxModel();

	private JComboBox datasetComboBox = PrecedenceOptionsPanel
			.createDatasetComboBox(datasetComboBoxModel);

	private JLabel tolerancesLabel = new JLabel();

	private JLabel maxSegmentAngleDeltaLabel = new JLabel();

	private JLabel maxAdjustmentSizeLabel = new JLabel();

	private JLabel minIncidenceAngleLabel = new JLabel();

	private JTextField maximumSegmentAngleDeltaTextField = new ValidatingTextField(
			"", 5, SwingConstants.RIGHT,
			new ValidatingTextField.BoundedDoubleValidator(0, true, 180, true),
			new ValidatingTextField.BlankCleaner(""
					+ new AutoAdjustOptions().getMaximumSegmentAngleDelta()));

	private JTextField maximumAdjustmentSizeTextField = new ValidatingTextField(
			"", 5, SwingConstants.RIGHT,
			new ValidatingTextField.BoundedDoubleValidator(0, true,
					Double.POSITIVE_INFINITY, true),
			new ValidatingTextField.BlankCleaner(""
					+ new AutoAdjustOptions().getMaximumAdjustmentSize()));

	private JTextField minimumIncidenceAngleTextField = new ValidatingTextField(
			"", 5, SwingConstants.RIGHT,
			new ValidatingTextField.BoundedDoubleValidator(0, true, 180, true),
			new ValidatingTextField.BlankCleaner(""
					+ new AutoAdjustOptions().getMinimumIncidenceAngle()));

	private WorkbenchContext context;

	private JPanel fillerPanel1 = new JPanel();

	private GridBagLayout gridBagLayout2 = new GridBagLayout();

	private JPanel fillerPanel2 = new JPanel();

	private GridBagLayout gridBagLayout3 = new GridBagLayout();

	private JLabel adjustmentMethodLabel = new JLabel();

	private JRadioButton warpEntireSegmentRadioButton = new JRadioButton();

	private JRadioButton warpPartialSegmentRadioButton = new JRadioButton();

	private ValidatingTextField segmentAdjustmentLengthTextField = new ValidatingTextField(
			"", 5, SwingConstants.RIGHT,
			new ValidatingTextField.BoundedDoubleValidator(0, false,
					Integer.MAX_VALUE, false),
			new FUTURE_ValidatingTextField.NumberCleaner(
					AutoAdjustOptions.DEFAULT_SEGMENT_ADJUSTMENT_LENGTH + ""));

	private JCheckBox shiftSegmentsCheckBox = new JCheckBox();

	private JLabel jLabel1 = new JLabel();

	public AutoAdjustConflationOptionsPanel(WorkbenchContext context) {
		this.context = context;
		new ButtonGroup() {
			{
				add(warpPartialSegmentRadioButton);
				add(warpEntireSegmentRadioButton);
			}
		};
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void jbInit() throws Exception {
		adjustmentPrecedenceRuleLabel
				.setFont(new java.awt.Font("Dialog", 1, 12));
		adjustmentPrecedenceRuleLabel.setText("Precedence Rule");
		this.setLayout(gridBagLayout1);
		this.setFont(new java.awt.Font("Dialog", 1, 12));
		performAdjustmentsLabel
				.setText("Perform adjustments on segments from: ");
		tolerancesLabel.setFont(new java.awt.Font("Dialog", 1, 12));
		tolerancesLabel.setText("Tolerances");
		maxSegmentAngleDeltaLabel
				.setText("Maximum Segment Angle Delta (degrees): ");
		maxAdjustmentSizeLabel.setText("Maximum Adjustment Size: ");
		minIncidenceAngleLabel.setText("Minimum Incidence Angle (degrees): ");
		fillerPanel1.setPreferredSize(new Dimension(12, 12));
		fillerPanel1.setLayout(gridBagLayout2);
		fillerPanel2.setLayout(gridBagLayout3);
		adjustmentMethodLabel.setFont(new java.awt.Font("Dialog", 1, 12));
		adjustmentMethodLabel.setText("Adjustment Method");
		warpEntireSegmentRadioButton.setText("Warp entire segment");
		warpPartialSegmentRadioButton.setText("Warp partial segment");
		shiftSegmentsCheckBox.setText("Shift short segments with one connected end");
		jLabel1.setText("Segment Adjustment Length: ");
		this.add(adjustmentPrecedenceRuleLabel, new GridBagConstraints(1, 1, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(performAdjustmentsLabel, new GridBagConstraints(1, 2, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 30, 0, 0), 0, 0));
		this.add(datasetComboBox, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		this.add(tolerancesLabel, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(
						20, 0, 0, 0), 0, 0));
		this.add(maxAdjustmentSizeLabel, new GridBagConstraints(1, 4, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 30, 0, 0), 0, 0));
		this.add(maxSegmentAngleDeltaLabel, new GridBagConstraints(1, 5, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 30, 0, 0), 0, 0));
		this.add(minIncidenceAngleLabel, new GridBagConstraints(1, 6, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 30, 0, 0), 0, 0));
		this.add(maximumAdjustmentSizeTextField, new GridBagConstraints(2, 4,
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(maximumSegmentAngleDeltaTextField, new GridBagConstraints(2,
				5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(minimumIncidenceAngleTextField, new GridBagConstraints(2, 6,
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(fillerPanel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(fillerPanel2, new GridBagConstraints(3, 12, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(adjustmentMethodLabel, new GridBagConstraints(1, 8, 2, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(20, 0, 0, 0), 0, 0));
		this.add(warpPartialSegmentRadioButton, new GridBagConstraints(1, 10,
				2, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 30, 0, 0), 0, 0));
		this.add(warpEntireSegmentRadioButton, new GridBagConstraints(1, 9, 2,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 30, 0, 0), 0, 0));
		this.add(shiftSegmentsCheckBox, new GridBagConstraints(1, 11, 2, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(10, 30, 0, 0), 0, 0));
		this.add(jLabel1, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						30, 0, 0), 0, 0));
		this.add(segmentAdjustmentLengthTextField, new GridBagConstraints(2, 7,
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
	}

	public String validateInput() {
		return null;
	}

	private AdjustmentConstraintsOptionsPanel adjustmentConstraintsOptionsPanel() {
		return (AdjustmentConstraintsOptionsPanel) GUIUtil
				.getDescendantOfClass(AdjustmentConstraintsOptionsPanel.class,
						SwingUtilities.windowForComponent(this));
	}

	public void okPressed() {
		options().setMaximumAdjustmentSize(
				Double.parseDouble(maximumAdjustmentSizeTextField.getText()));
		options()
				.setMaximumSegmentAngleDelta(
						Double.parseDouble(maximumSegmentAngleDeltaTextField
								.getText()));
		options().setMinimumIncidenceAngle(
				Double.parseDouble(minimumIncidenceAngleTextField.getText()));
		options().setDatasetName(
				session().getSourceNetwork(datasetComboBox.getSelectedIndex())
						.getName());
		options().setSegmentAdjustmentLength(
				segmentAdjustmentLengthTextField.getDouble());
		options().setShiftingSegmentsWithOneConnectedEnd(
				shiftSegmentsCheckBox.isSelected());
		options()
				.setMethodClass(
						warpEntireSegmentRadioButton.isSelected() ? WarpAdjustmentMethod.class
								: WarpLocallyAdjustmentMethod.ModelBasedWarpZone.class);
	}

	private AutoAdjustOptions options() {
		return AutoAdjustOptions.get(session());
	}

	private ConflationSession session() {
		return ToolboxModel.instance(context).getSession();
	}

	public void init() {
		maximumAdjustmentSizeTextField.setText(""
				+ options().getMaximumAdjustmentSize());
		maximumSegmentAngleDeltaTextField.setText(""
				+ options().getMaximumSegmentAngleDelta());
		minimumIncidenceAngleTextField.setText(""
				+ options().getMinimumIncidenceAngle());
		datasetComboBoxModel.removeAllElements();
		datasetComboBoxModel.addElement(ToolboxModel.instance(context)
				.getSourceLayer(0));
		datasetComboBoxModel.addElement(ToolboxModel.instance(context)
				.getSourceLayer(1));
		datasetComboBox.setSelectedIndex(session().getSourceNetwork(
				options().getDatasetName()).getID());
		segmentAdjustmentLengthTextField.setText(options()
				.getSegmentAdjustmentLength()
				+ "");
		warpEntireSegmentRadioButton
				.setSelected(options().getMethodClass() == WarpAdjustmentMethod.class);
		warpPartialSegmentRadioButton
				.setSelected(options().getMethodClass() == WarpLocallyAdjustmentMethod.ModelBasedWarpZone.class);
		shiftSegmentsCheckBox.setSelected(options()
				.isShiftingSegmentsWithOneConnectedEnd());
	}
}