package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.*;
import java.util.Map;
import java.util.Vector;

import javax.swing.*;

import com.vividsolutions.jcs.jump.FUTURE_AbstractWizardPanel;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.LayerNameRenderer;
import com.vividsolutions.jump.workbench.ui.plugin.AddNewLayerPlugIn;
import java.awt.event.*;

public class PerformAutomaticConflationWizardPanel
		extends
			FUTURE_AbstractWizardPanel {
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private Map dataMap;
	public static final String AUTOMATCH_KEY = PerformAutomaticConflationWizardPanel.class
			.getName()
			+ " - AUTOMATCH";
	public static final String AUTOADJUST_KEY = PerformAutomaticConflationWizardPanel.class
			.getName()
			+ " - AUTOADJUST";

	public PerformAutomaticConflationWizardPanel() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	void jbInit() throws Exception {
		this.setLayout(gridBagLayout1);
		fillerPanel2.setLayout(gridBagLayout2);
		fillerPanel.setLayout(gridBagLayout3);
		fillerPanel2.setPreferredSize(new Dimension(59, 17));
		this.add(fillerPanel, new GridBagConstraints(2, 4, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(fillerPanel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(runAutoMatchAutoAdjustPanel, new GridBagConstraints(1, 1, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
	}
	private PerformAutomaticConflationPanel runAutoMatchAutoAdjustPanel = new PerformAutomaticConflationPanel();
	public void enteredFromLeft(Map dataMap) {
		this.dataMap = dataMap;
		runAutoMatchAutoAdjustPanel
				.init(dataMap
						.get(ChooseConflationProfileWizardPanel.NO_PROFILE_KEY) == Boolean.FALSE);
	}
	public void exitingToRight() throws Exception {
		dataMap.put(AUTOMATCH_KEY, Boolean.valueOf(runAutoMatchAutoAdjustPanel
				.isAutoMatchSpecified()));
		dataMap.put(AUTOADJUST_KEY, Boolean.valueOf(runAutoMatchAutoAdjustPanel
				.isAutoAdjustSpecified()));
	}
	private JPanel fillerPanel = new JPanel();
	private JPanel fillerPanel2 = new JPanel();
	private GridBagLayout gridBagLayout2 = new GridBagLayout();
	private GridBagLayout gridBagLayout3 = new GridBagLayout();
	public String getInstructions() {
		return "Specify whether to perform automatic segment matching and automatic adjustment of inconsistencies.";
	}
	public boolean isInputValid() {
		return true;
	}
	public String getNextID() {
		return null;
	}
}