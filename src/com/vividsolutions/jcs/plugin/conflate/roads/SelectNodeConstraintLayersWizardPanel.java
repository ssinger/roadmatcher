package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.*;
import java.util.Map;
import java.util.Vector;

import javax.swing.*;

import com.vividsolutions.jcs.jump.FUTURE_AbstractWizardPanel;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.LayerNameRenderer;
import com.vividsolutions.jump.workbench.ui.plugin.AddNewLayerPlugIn;

public class SelectNodeConstraintLayersWizardPanel
		extends
			FUTURE_AbstractWizardPanel {
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JLabel datasetLabel = new JLabel();
	private JLabel nodeConstraintLayerLabel = new JLabel();
	private JLabel shortName0Label = new JLabel();
	private JLabel shortName1Label = new JLabel();
	private JComboBox nodeConstraintLayer0ComboBox = createComboBox();
	private JComboBox nodeConstraintLayer1ComboBox = createComboBox();
	private JPanel strutPanel = new JPanel();
	private Map dataMap;
	private WorkbenchContext context;

	public SelectNodeConstraintLayersWizardPanel(WorkbenchContext context) {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		this.context = context;
	}
	private JComboBox createComboBox() {
		return new JComboBox() {
			{
				final ListCellRenderer layerRenderer = new LayerNameRenderer();
				final ListCellRenderer noneRenderer = new DefaultListCellRenderer();
				setRenderer(new LayerNameRenderer() {
					public Component getListCellRendererComponent(JList list,
							Object value, int index, boolean isSelected,
							boolean cellHasFocus) {
						return value == NONE
								? noneRenderer.getListCellRendererComponent(
										list, value, index, isSelected,
										cellHasFocus)
								: layerRenderer.getListCellRendererComponent(
										list, value, index, isSelected,
										cellHasFocus);
					}
				});
			}
		};
	}
	void jbInit() throws Exception {
		datasetLabel.setText("Dataset");
		this.setLayout(gridBagLayout1);
		nodeConstraintLayerLabel.setText("Node Constraint Layer");
		shortName0Label.setBorder(BorderFactory.createLoweredBevelBorder());
		shortName0Label.setText("shortName0");
		shortName1Label.setBorder(BorderFactory.createLoweredBevelBorder());
		shortName1Label.setToolTipText("");
		shortName1Label.setText("shortName1");
		fillerPanel2.setLayout(gridBagLayout2);
		fillerPanel.setLayout(gridBagLayout3);
		strutPanel.setLayout(gridBagLayout4);
		strutPanel.setPreferredSize(new Dimension(12, 12));
		fillerPanel2.setPreferredSize(new Dimension(59, 17));
		this.add(datasetLabel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		this.add(nodeConstraintLayerLabel, new GridBagConstraints(3, 1, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(shortName0Label, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(shortName1Label, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(nodeConstraintLayer0ComboBox, new GridBagConstraints(3, 2, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(nodeConstraintLayer1ComboBox, new GridBagConstraints(3, 3, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(strutPanel, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(fillerPanel, new GridBagConstraints(4, 4, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(fillerPanel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
	}
	private Profile profile() {
		return (Profile) dataMap
				.get(ChooseConflationProfileWizardPanel.SELECTED_PROFILE_KEY);
	}
	public void enteredFromLeft(Map dataMap) {
		this.dataMap = dataMap;
		shortName0Label.setText(profile().dataset(0).getShortName());
		shortName1Label.setText(profile().dataset(1).getShortName());
		initialize(nodeConstraintLayer0ComboBox);
		initialize(nodeConstraintLayer1ComboBox);
	}
	private void initialize(JComboBox nodeConstraintLayerComboBox) {
		Vector entries = new Vector();
		entries.add(NONE);
		entries.addAll(ToolboxModel
				.instance(context.getLayerManager(), context)
				.nonConflationLayers());
		entries.remove(dataMap.get(SelectInputLayersWizardPanel.LAYER_A_KEY));
		entries.remove(dataMap.get(SelectInputLayersWizardPanel.LAYER_B_KEY));
		nodeConstraintLayerComboBox.setModel(new DefaultComboBoxModel(entries));
	}
	private static final Object NONE = new Object() {
		public String toString() {
			return "None";
		}
	};
	public void exitingToRight() throws Exception {
		dataMap.put(NODE_CONSTRAINT_FEATURE_COLLECTION_0_KEY,
				featureCollection(nodeConstraintLayer0ComboBox));
		dataMap.put(NODE_CONSTRAINT_FEATURE_COLLECTION_1_KEY,
				featureCollection(nodeConstraintLayer1ComboBox));
	}
	private FeatureCollection featureCollection(
			JComboBox nodeConstraintLayerComboBox) {
		return nodeConstraintLayerComboBox.getSelectedItem() == NONE
				? AddNewLayerPlugIn.createBlankFeatureCollection()
				: ((Layer) nodeConstraintLayerComboBox.getSelectedItem())
						.getFeatureCollectionWrapper().getUltimateWrappee();
	}
	public static final String NODE_CONSTRAINT_FEATURE_COLLECTION_0_KEY = SelectNodeConstraintLayersWizardPanel.class
			.getName()
			+ " - NODE CONSTRAINT LAYER 0";
	public static final String NODE_CONSTRAINT_FEATURE_COLLECTION_1_KEY = SelectNodeConstraintLayersWizardPanel.class
			.getName()
			+ " - NODE CONSTRAINT LAYER 1";
	private JPanel fillerPanel = new JPanel();
	private JPanel fillerPanel2 = new JPanel();
	private GridBagLayout gridBagLayout2 = new GridBagLayout();
	private GridBagLayout gridBagLayout3 = new GridBagLayout();
	private GridBagLayout gridBagLayout4 = new GridBagLayout();
	public String getInstructions() {
		return "Choose layers for Node Constraints, if required.";
	}
	public boolean isInputValid() {
		return true;
	}
	public String getNextID() {
		return PerformAutomaticConflationWizardPanel.class.getName();
	}
}