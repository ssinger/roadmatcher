package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Vector;
import com.vividsolutions.jcs.conflate.roads.model.ReferenceDatasetPrecedenceRuleEngine;
import com.vividsolutions.jcs.jump.FUTURE_AbstractWizardPanel;
import com.vividsolutions.jcs.jump.FUTURE_StringUtil;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.ColorPanel;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerNameRenderer;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.*;
import javax.swing.*;

public class SelectInputLayersWizardPanel extends FUTURE_AbstractWizardPanel {
	private GridBagLayout gridBagLayout1 = new GridBagLayout();

	private JLabel shortNameLabel = new JLabel();

	private JTextField shortNameTextFieldA = new JTextField();

	private JTextField shortNameTextFieldB = new JTextField();

	private JLabel referenceDatasetLabel = new JLabel();

	private JLabel layerLabel = new JLabel();

	private JComboBox layerAComboBox = new JComboBox();

	private JComboBox layerBComboBox = new JComboBox();

	private JRadioButton referenceDatasetARadioButton = new JRadioButton();

	private JRadioButton referenceDatasetBRadioButton = new JRadioButton();

	private JPanel spacerPanel2 = new JPanel();

	private GridBagLayout gridBagLayout3 = new GridBagLayout();

	private JPanel spacerPanel3 = new JPanel();

	private GridBagLayout gridBagLayout4 = new GridBagLayout();

	private Icon xIcon = GUIUtil.toSmallIcon(IconLoader.icon("Delete.gif"));

	private JLabel errorLabel = new JLabel();

	private Map dataMap;

	public static final String LAYER_A_KEY = SelectInputLayersWizardPanel.class
			.getName()
			+ " - LAYER A";

	public static final String LAYER_B_KEY = SelectInputLayersWizardPanel.class
			.getName()
			+ " - LAYER B";

	private ButtonGroup buttonGroup = new ButtonGroup();

	private JPanel spacerPanel4 = new JPanel();

	private GridBagLayout gridBagLayout5 = new GridBagLayout();

	private GridBagLayout gridBagLayout2 = new GridBagLayout();

	private boolean[] shortNameTextFieldEdited;

	private boolean updatingShortNameTextFields = false;

	private JPanel profilePanel = new JPanel();

	private GridBagLayout gridBagLayout6 = new GridBagLayout();

	private JLabel profileLabel = new JLabel();

	private JTextField profileTextField = new JTextField();

	private JPanel spacerPanel1 = new JPanel();

	private GridBagLayout gridBagLayout8 = new GridBagLayout();

	private JTextField matchPrecedenceRuleTextField = new JTextField();

	private JLabel matchPrecedenceRuleLabel = new JLabel();

	private ColorPanel colourPanelA = new ColorPanel();

	private ColorPanel colourPanelB = new ColorPanel();

	public SelectInputLayersWizardPanel(WorkbenchContext context) {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		initialize(referenceDatasetARadioButton);
		initialize(referenceDatasetBRadioButton);
		initialize(layerAComboBox, layerBComboBox, context);
		initialize(layerBComboBox, layerAComboBox, context);
		initialize(shortNameTextFieldA, 0);
		initialize(shortNameTextFieldB, 1);
		setEnabled(profileTextField, false);
		setEnabled(matchPrecedenceRuleTextField, false);
		colourPanelA.setFillColor(HighlightManager.instance(context)
				.getColourScheme().getDefaultColour0());
		colourPanelA.setLineColor(HighlightManager.instance(context)
				.getColourScheme().getDefaultColour0());
		colourPanelB.setFillColor(HighlightManager.instance(context)
				.getColourScheme().getDefaultColour1());
		colourPanelB.setLineColor(HighlightManager.instance(context)
				.getColourScheme().getDefaultColour1());
	}

	private void initialize(JRadioButton radioButton) {
		buttonGroup.add(radioButton);
		radioButton.setToolTipText(referenceDatasetLabel.getToolTipText());
	}

	private void initialize(JTextField shortNameTextField, final int i) {
		shortNameTextField.getDocument().addDocumentListener(
				GUIUtil.toDocumentListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (!updatingShortNameTextFields) {
							shortNameTextFieldEdited[i] = true;
						}
						fireInputChanged();
					}
				}));
	}

	private void initialize(final JComboBox layerComboBox,
			final JComboBox otherLayerComboBox, WorkbenchContext context) {
		layerComboBox.setModel(new DefaultComboBoxModel(new Vector(ToolboxModel
				.instance(context.getLayerManager(), context)
				.nonConflationLayers())));
		layerComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (layerComboBox.getSelectedItem() == otherLayerComboBox
						.getSelectedItem()) {
					otherLayerComboBox.setSelectedItem(chooseLayerMatching("",
							otherLayerComboBox, (Layer) layerComboBox
									.getSelectedItem()));
				}
				updateShortNameTextFields();
				fireInputChanged();
			}
		});
		layerComboBox.setRenderer(new LayerNameRenderer());
	}

	void jbInit() throws Exception {
		this.setLayout(gridBagLayout1);
		shortNameLabel.setText("Short Name");
		referenceDatasetLabel.setText("Reference Dataset");
		layerLabel.setText("Layer");
		spacerPanel2.setPreferredSize(new Dimension(12, 1));
		spacerPanel2.setLayout(gridBagLayout3);
		spacerPanel3.setPreferredSize(new Dimension(12, 1));
		spacerPanel3.setLayout(gridBagLayout4);
		errorLabel.setText(" ");
		spacerPanel4.setLayout(gridBagLayout5);
		spacerPanel4.setPreferredSize(new Dimension(0, 0));
		profilePanel.setLayout(gridBagLayout6);
		profileLabel.setText("Profile: ");
		spacerPanel1.setPreferredSize(new Dimension(20, 20));
		spacerPanel1.setLayout(gridBagLayout8);
		matchPrecedenceRuleLabel.setText(" Match Precedence Rule: ");
		this.add(shortNameLabel, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		this.add(shortNameTextFieldA, new GridBagConstraints(2, 4, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(shortNameTextFieldB, new GridBagConstraints(2, 5, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(layerLabel, new GridBagConstraints(4, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		this.add(referenceDatasetLabel, new GridBagConstraints(6, 3, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(referenceDatasetARadioButton, new GridBagConstraints(6, 4, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(referenceDatasetBRadioButton, new GridBagConstraints(6, 5, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(layerAComboBox, new GridBagConstraints(4, 4, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(layerBComboBox, new GridBagConstraints(4, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(spacerPanel2, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(spacerPanel3, new GridBagConstraints(5, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(errorLabel, new GridBagConstraints(1, 6, 7, 1, 1.0, 1.0,
				GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(spacerPanel4, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(profilePanel, new GridBagConstraints(0, 1, 10, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		profilePanel.add(profileLabel, new GridBagConstraints(0, 2, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		profilePanel.add(profileTextField, new GridBagConstraints(1, 2, 1, 1,
				0.0, 0.0, GridBagConstraints.SOUTHEAST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		profilePanel.add(matchPrecedenceRuleTextField, new GridBagConstraints(
				3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		profilePanel.add(matchPrecedenceRuleLabel, new GridBagConstraints(2, 2,
				1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(spacerPanel1, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(colourPanelA, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 5), 0, 0));
		this.add(colourPanelB, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 5), 0, 0));
	}

	public void enteredFromLeft(Map dataMap) {
		this.dataMap = dataMap;
		shortNameTextFieldEdited = new boolean[] { false, false };
		profileTextField.setText(pad((String) dataMap
				.get(ChooseConflationProfileWizardPanel.PROFILE_NAME)));
		matchPrecedenceRuleTextField
				.setText(pad(profile().getPrecedenceRuleEngine() instanceof ReferenceDatasetPrecedenceRuleEngine ? "Reference Dataset"
						: "Scripted"));
		initialize(shortNameTextFieldA, profile().dataset(0).getShortName());
		initialize(shortNameTextFieldB, profile().dataset(1).getShortName());
		referenceDatasetARadioButton
				.setSelected(profile().getPrecedenceRuleEngine() instanceof ReferenceDatasetPrecedenceRuleEngine
						&& (((ReferenceDatasetPrecedenceRuleEngine) profile()
								.getPrecedenceRuleEngine())
								.getReferenceDatasetName() == null || ((ReferenceDatasetPrecedenceRuleEngine) profile()
								.getPrecedenceRuleEngine())
								.getReferenceDatasetName().equals(
										profile().dataset(0).getShortName())));
		referenceDatasetARadioButton
				.setEnabled(dataMap
						.get(ChooseConflationProfileWizardPanel.NO_PROFILE_KEY) == Boolean.TRUE);
		referenceDatasetBRadioButton
				.setSelected(profile().getPrecedenceRuleEngine() instanceof ReferenceDatasetPrecedenceRuleEngine
						&& ((ReferenceDatasetPrecedenceRuleEngine) profile()
								.getPrecedenceRuleEngine())
								.getReferenceDatasetName() != null
						&& ((ReferenceDatasetPrecedenceRuleEngine) profile()
								.getPrecedenceRuleEngine())
								.getReferenceDatasetName().equals(
										profile().dataset(1).getShortName()));
		referenceDatasetBRadioButton
				.setEnabled(dataMap
						.get(ChooseConflationProfileWizardPanel.NO_PROFILE_KEY) == Boolean.TRUE);
		referenceDatasetLabel
				.setVisible(profile().getPrecedenceRuleEngine() instanceof ReferenceDatasetPrecedenceRuleEngine);
		referenceDatasetARadioButton
				.setVisible(profile().getPrecedenceRuleEngine() instanceof ReferenceDatasetPrecedenceRuleEngine);
		referenceDatasetBRadioButton
				.setVisible(profile().getPrecedenceRuleEngine() instanceof ReferenceDatasetPrecedenceRuleEngine);
		initializeSelectedLayers();
		//Create a new JTextField to ensure #preferredSize is set correctly
		//[Jon Aquino 2004-05-18]
		profileTextField.setPreferredSize(new Dimension((int) Math.max(
				profileTextField.getPreferredSize().getWidth(), new JTextField(
						matchPrecedenceRuleTextField.getText())
						.getPreferredSize().getWidth()), (int) profileTextField
				.getPreferredSize().getHeight()));
		matchPrecedenceRuleTextField.setPreferredSize(profileTextField
				.getPreferredSize());
	}

	private String pad(String s) {
		//Workaround for pixel getting chopped off
		//[Jon Aquino 2004-04-26]
		return " " + s + " ";
	}

	private void initialize(final JTextField shortNameTextField,
			final String shortName) {
		indicateUpdatingShortNameTextFields(new Block() {
			public Object yield() {
				shortNameTextField.setText(shortName);
				return null;
			}
		});
		setEnabled(
				shortNameTextField,
				dataMap.get(ChooseConflationProfileWizardPanel.NO_PROFILE_KEY) == Boolean.TRUE);
	}

	private void setEnabled(JTextField textField, boolean enabled) {
		textField.setEnabled(enabled);
		textField.setBackground(enabled ? new JTextField().getBackground()
				: getBackground());
		textField.setDisabledTextColor(new JTextField().getForeground());
	}

	private void initializeSelectedLayers() {
		Layer initialLayerA = chooseLayerMatching(profile().dataset(0).getShortName(),
				layerAComboBox, null);
		Layer initialLayerB = chooseLayerMatching(profile().dataset(1).getShortName(),
				layerBComboBox, initialLayerA);
		if (initialLayerA == null) {
			initialLayerA = chooseLayerMatching("", layerAComboBox,
					initialLayerB);
		}
		if (initialLayerB == null) {
			initialLayerB = chooseLayerMatching("", layerBComboBox,
					initialLayerA);
		}
		layerAComboBox.setSelectedItem(initialLayerA);
		layerBComboBox.setSelectedItem(initialLayerB);
	}

	private Layer chooseLayerMatching(String shortName,
			JComboBox layerComboBox, Layer butNotThisLayer) {
		for (int i = 0; i < layerComboBox.getItemCount(); i++) {
			Layer layer = (Layer) layerComboBox.getItemAt(i);
			if (layer == butNotThisLayer) {
				continue;
			}
			if (layer.getName().toUpperCase().indexOf(
					shortName.trim().toUpperCase()) > -1) {
				return layer;
			}
		}
		return null;
	}

	private Profile profile() {
		return (Profile) dataMap
				.get(ChooseConflationProfileWizardPanel.SELECTED_PROFILE_KEY);
	}

	public void exitingToRight() throws Exception {
		profile().dataset(0).setShortName(shortNameTextFieldA.getText().trim());
		profile().dataset(1).setShortName(shortNameTextFieldB.getText().trim());
		if (dataMap.get(ChooseConflationProfileWizardPanel.NO_PROFILE_KEY) == Boolean.TRUE) {
			((ReferenceDatasetPrecedenceRuleEngine) profile()
					.getPrecedenceRuleEngine())
					.setReferenceDatasetName(referenceDatasetARadioButton
							.isSelected() ? profile().dataset(0).getShortName()
							: profile().dataset(1).getShortName());
			profile()
					.getAutoAdjustOptions()
					.setDatasetName(
							referenceDatasetARadioButton.isSelected() ? shortNameTextFieldB
									.getText().trim()
									: shortNameTextFieldA.getText().trim());
		}
		dataMap.put(LAYER_A_KEY, layerAComboBox.getSelectedItem());
		dataMap.put(LAYER_B_KEY, layerBComboBox.getSelectedItem());
	}

	public String getInstructions() {
		return "Choose the two input layers to conflate.\n\nThe Reference Dataset choice is only used when a Match Precedence Rule of “Reference Dataset” is selected.";
	}

	public boolean isInputValid() {
		if (layerAComboBox.getSelectedItem() == layerBComboBox
				.getSelectedItem()) {
			return setErrorMessage(ErrorMessages.newSessionPanel_sameLayers);
		}
		if (shortNameTextFieldA.getText().trim().equalsIgnoreCase(
				shortNameTextFieldB.getText().trim())) {
			return setErrorMessage(ErrorMessages.newSessionPanel_sameShortNames);
		}
		if (shortNameTextFieldA.getText().trim().length() == 0) {
			return setErrorMessage(ErrorMessages.newSessionPanel_noNetworkAShortName);
		}
		if (shortNameTextFieldB.getText().trim().length() == 0) {
			return setErrorMessage(ErrorMessages.newSessionPanel_noNetworkBShortName);
		}
		return setErrorMessage(null);
	}

	private boolean setErrorMessage(String errorMessage) {
		errorLabel.setIcon(errorMessage != null ? xIcon = GUIUtil
				.toSmallIcon(IconLoader.icon("Delete.gif")) : null);
		errorLabel.setText(errorMessage != null ? errorMessage : " ");
		return errorMessage == null;
	}

	public String getNextID() {
		return SelectNodeConstraintLayersWizardPanel.class.getName();
	}

	private String commonPrefix(String s0, String s1) {
		int maxPrefixLen = Math.min(s0.length(), s1.length());
		int commonPrefLen = 0;
		for (commonPrefLen = 1; commonPrefLen <= maxPrefixLen; commonPrefLen++) {
			if (!s0.regionMatches(0, s1, 0, commonPrefLen))
				break;
		}
		return s0.substring(0, commonPrefLen - 1);
	}

	private void updateShortNameTextFields() {
		if (dataMap.get(ChooseConflationProfileWizardPanel.NO_PROFILE_KEY) == Boolean.FALSE) {
			return;
		}
		indicateUpdatingShortNameTextFields(new Block() {
			public Object yield() {
				updateShortNameTextFieldsProper();
				return null;
			}
		});
	}

	private void indicateUpdatingShortNameTextFields(Block block) {
		updatingShortNameTextFields = true;
		try {
			block.yield();
		} finally {
			updatingShortNameTextFields = false;
		}
	}

	private void updateShortNameTextFieldsProper() {
		String shortNameTextA = removeCommonPrefix(((Layer) layerAComboBox
				.getSelectedItem()).getName(), ((Layer) layerBComboBox
				.getSelectedItem()).getName());
		String shortNameTextB = removeCommonPrefix(((Layer) layerBComboBox
				.getSelectedItem()).getName(), ((Layer) layerAComboBox
				.getSelectedItem()).getName());
		String newShortNameTextA = FUTURE_StringUtil
				.reverse(removeCommonPrefix(FUTURE_StringUtil
						.reverse(shortNameTextA), FUTURE_StringUtil
						.reverse(shortNameTextB)));
		String newShortNameTextB = FUTURE_StringUtil
				.reverse(removeCommonPrefix(FUTURE_StringUtil
						.reverse(shortNameTextB), FUTURE_StringUtil
						.reverse(shortNameTextA)));
		shortNameTextA = newShortNameTextA;
		shortNameTextB = newShortNameTextB;
		if (shortNameTextA.trim().length() > 0 && !shortNameTextFieldEdited[0]) {
			shortNameTextFieldA.setText(shortNameTextA.trim());
		}
		if (shortNameTextB.trim().length() > 0 && !shortNameTextFieldEdited[1]) {
			shortNameTextFieldB.setText(shortNameTextB.trim());
		}
	}

	private String removeCommonPrefix(String a, String b) {
		return FUTURE_StringUtil.replace(a, commonPrefix(a, b), "", false);
	}
}