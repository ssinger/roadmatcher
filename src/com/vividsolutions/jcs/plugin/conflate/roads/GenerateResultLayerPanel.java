package com.vividsolutions.jcs.plugin.conflate.roads;

import javax.swing.JPanel;

import javax.swing.JButton;

import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SourceState;

public class GenerateResultLayerPanel extends JPanel {

	private JLabel instructionLabel = null;

	private JLabel layer0Label = null;

	private JLabel layer1Label = null;

	private JLabel includedLabel = null;

	private JCheckBox included0CheckBox = null;

	private JCheckBox included1CheckBox = null;

	private JLabel retiredLabel = null;

	private JLabel matchedNonRefLabel = null;

	private JCheckBox retired0CheckBox = null;

	private JCheckBox retired1CheckBox = null;

	private JCheckBox matchedNonRef0CheckBox = null;

	private JCheckBox matchedNonRef1CheckBox = null;

	private JCheckBox createVertexTransferVectorsLayerCheckBox = null;

	private JPanel spacerPanel0 = null;

	private JPanel spacerPanel1 = null;

	public GenerateResultLayerPanel() {
		super();
		initialize();
		layer0Label.setFont(layer0Label.getFont().deriveFont(Font.BOLD));
		layer1Label.setFont(layer1Label.getFont().deriveFont(Font.BOLD));
	}

	private void initialize() {
		matchedNonRefLabel = new JLabel();
		retiredLabel = new JLabel();
		includedLabel = new JLabel();
		layer1Label = new JLabel();
		layer0Label = new JLabel();
		instructionLabel = new JLabel();
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
		this.setLayout(new GridBagLayout());
		this.setSize(561, 291);
		gridBagConstraints3.gridx = 0;
		gridBagConstraints3.gridy = 0;
		gridBagConstraints3.gridwidth = 10;
		gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints3.insets = new java.awt.Insets(0, 0, 20, 0);
		instructionLabel
				.setText("Choose which segments to include in the result:");
		instructionLabel.setName("instructionsLabel");
		gridBagConstraints4.gridx = 2;
		gridBagConstraints4.gridy = 1;
		layer0Label.setText("<Layer0>");
		layer0Label.setName("layer0Label");
		gridBagConstraints5.gridx = 4;
		gridBagConstraints5.gridy = 1;
		layer1Label.setText("<Layer1>");
		layer1Label.setName("layer1label");
		gridBagConstraints8.gridx = 0;
		gridBagConstraints8.gridy = 3;
		gridBagConstraints8.anchor = java.awt.GridBagConstraints.WEST;
		includedLabel.setText("Included");
		gridBagConstraints9.gridx = 2;
		gridBagConstraints9.gridy = 3;
		gridBagConstraints10.gridx = 4;
		gridBagConstraints10.gridy = 3;
		gridBagConstraints11.gridx = 0;
		gridBagConstraints11.gridy = 4;
		gridBagConstraints11.anchor = java.awt.GridBagConstraints.WEST;
		retiredLabel.setText("Retired");
		gridBagConstraints12.gridx = 0;
		gridBagConstraints12.gridy = 5;
		gridBagConstraints12.anchor = java.awt.GridBagConstraints.WEST;
		matchedNonRefLabel.setText("Matched (Non-Ref)");
		gridBagConstraints13.gridx = 2;
		gridBagConstraints13.gridy = 4;
		gridBagConstraints14.gridx = 4;
		gridBagConstraints14.gridy = 4;
		gridBagConstraints15.gridx = 2;
		gridBagConstraints15.gridy = 5;
		gridBagConstraints16.gridx = 4;
		gridBagConstraints16.gridy = 5;
		gridBagConstraints18.gridx = 0;
		gridBagConstraints18.gridy = 7;
		gridBagConstraints18.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints18.gridwidth = 12;
		gridBagConstraints18.insets = new java.awt.Insets(20, 0, 0, 0);
		gridBagConstraints20.gridx = 3;
		gridBagConstraints20.gridy = 1;
		gridBagConstraints21.gridx = 1;
		gridBagConstraints21.gridy = 1;
		this.add(instructionLabel, gridBagConstraints3);
		this.add(layer0Label, gridBagConstraints4);
		this.add(layer1Label, gridBagConstraints5);
		this.add(includedLabel, gridBagConstraints8);
		this.add(getIncluded0CheckBox(), gridBagConstraints9);
		this.add(getIncluded1CheckBox(), gridBagConstraints10);
		this.add(retiredLabel, gridBagConstraints11);
		this.add(matchedNonRefLabel, gridBagConstraints12);
		this.add(getRetired0CheckBox(), gridBagConstraints13);
		this.add(getRetired1CheckBox(), gridBagConstraints14);
		this.add(getMatchedNonRef0CheckBox(), gridBagConstraints15);
		this.add(getMatchedNonRef1CheckBox(), gridBagConstraints16);
		this.add(getCreateVertexTransferVectorsLayerCheckBox(),
				gridBagConstraints18);
		this.add(getSpacerPanel0(), gridBagConstraints20);
		this.add(getSpacerPanel1(), gridBagConstraints21);
	}

	private JCheckBox getIncluded0CheckBox() {
		if (included0CheckBox == null) {
			included0CheckBox = new JCheckBox();
			included0CheckBox.setSelected(true);
		}
		return included0CheckBox;
	}

	private JCheckBox getIncluded1CheckBox() {
		if (included1CheckBox == null) {
			included1CheckBox = new JCheckBox();
			included1CheckBox.setSelected(true);
		}
		return included1CheckBox;
	}

	private JCheckBox getRetired0CheckBox() {
		if (retired0CheckBox == null) {
			retired0CheckBox = new JCheckBox();
		}
		return retired0CheckBox;
	}

	private JCheckBox getRetired1CheckBox() {
		if (retired1CheckBox == null) {
			retired1CheckBox = new JCheckBox();
		}
		return retired1CheckBox;
	}

	private JCheckBox getMatchedNonRef0CheckBox() {
		if (matchedNonRef0CheckBox == null) {
			matchedNonRef0CheckBox = new JCheckBox();
		}
		return matchedNonRef0CheckBox;
	}

	private JCheckBox getMatchedNonRef1CheckBox() {
		if (matchedNonRef1CheckBox == null) {
			matchedNonRef1CheckBox = new JCheckBox();
		}
		return matchedNonRef1CheckBox;
	}

	private JCheckBox getCreateVertexTransferVectorsLayerCheckBox() {
		if (createVertexTransferVectorsLayerCheckBox == null) {
			createVertexTransferVectorsLayerCheckBox = new JCheckBox();
			createVertexTransferVectorsLayerCheckBox
					.setText("Create Vertex Transfer Vectors layer");
		}
		return createVertexTransferVectorsLayerCheckBox;
	}

	private JPanel getSpacerPanel0() {
		if (spacerPanel0 == null) {
			spacerPanel0 = new JPanel();
			spacerPanel0.setLayout(new GridBagLayout());
			spacerPanel0.setPreferredSize(new java.awt.Dimension(10, 10));
		}
		return spacerPanel0;
	}

	private JPanel getSpacerPanel1() {
		if (spacerPanel1 == null) {
			spacerPanel1 = new JPanel();
			spacerPanel1.setLayout(new GridBagLayout());
			spacerPanel1.setPreferredSize(new java.awt.Dimension(10, 10));
		}
		return spacerPanel1;
	}

	public boolean isSpecifyingVertexTransferVectorsLayer() {
		return createVertexTransferVectorsLayerCheckBox.isSelected();
	}

	public boolean allows(SourceRoadSegment segment) {
		return (included0CheckBox.isSelected() && segment.getNetworkID() == 0 && segment
				.getState().indicates(SourceState.INCLUDED))
				|| (included1CheckBox.isSelected()
						&& segment.getNetworkID() == 1 && segment.getState()
						.indicates(SourceState.INCLUDED))
				|| (retired0CheckBox.isSelected()
						&& segment.getNetworkID() == 0 && segment.getState() == SourceState.RETIRED)
				|| (retired1CheckBox.isSelected()
						&& segment.getNetworkID() == 1 && segment.getState() == SourceState.RETIRED)
				|| (matchedNonRef0CheckBox.isSelected()
						&& segment.getNetworkID() == 0 && segment.getState() == SourceState.MATCHED_NON_REFERENCE)
				|| (matchedNonRef1CheckBox.isSelected()
						&& segment.getNetworkID() == 1 && segment.getState() == SourceState.MATCHED_NON_REFERENCE);
	}

	public void initialize(ToolboxModel toolboxModel) {
		layer0Label.setText(toolboxModel.getSession().getSourceNetwork(0)
				.getName());
		layer1Label.setText(toolboxModel.getSession().getSourceNetwork(1)
				.getName());
		layer0Label.setForeground(toolboxModel.getSourceLayer(0)
				.getBasicStyle().getFillColor());
		layer1Label.setForeground(toolboxModel.getSourceLayer(1)
				.getBasicStyle().getFillColor());
	}
} //  @jve:decl-index=0:visual-constraint="10,10"
