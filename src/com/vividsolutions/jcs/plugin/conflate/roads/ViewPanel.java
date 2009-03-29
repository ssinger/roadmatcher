package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.vividsolutions.jcs.plugin.conflate.roads.HighlightManager.Highlight;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelProxy;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorScheme;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorSchemeListCellRenderer;
public class ViewPanel extends JPanel {
	private GridBagLayout gridBagLayout2 = new GridBagLayout();
	private WorkbenchContext context;
	public static final String NODE_SIZE_KEY = ViewPanel.class.getName()
			+ " - NODE SIZE";
	private Timer repaintDependentsTimer = GUIUtil
			.createRestartableSingleEventTimer(100, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					repaintDependentsImmediately();
				}
			});
	private void repaintDependentsImmediately() {
		for (Iterator i = toolboxModels(roadsLayerViewPanels()).iterator(); i
				.hasNext();) {
			ToolboxModel initializedToolboxModel = (ToolboxModel) i.next();
			initializedToolboxModel.getSourceLayer(0).fireAppearanceChanged();
			initializedToolboxModel.getSourceLayer(1).fireAppearanceChanged();
		}
		((ToolboxPanel) GUIUtil.getDescendantOfClass(ToolboxPanel.class,
				RoadMatcherToolboxPlugIn.instance(context).getToolbox(context)))
				.updateComponents();
		((QueryToolboxPanel) GUIUtil.getDescendantOfClass(
				QueryToolboxPanel.class, QueryToolboxPlugIn.instance(context)
						.getToolbox(context))).updateComponents();
		((LegendToolboxPanel) GUIUtil.getDescendantOfClass(
				LegendToolboxPanel.class, LegendToolboxPlugIn.instance(context)
						.getToolbox(context))).updateComponents();
	}
	public ViewPanel(final WorkbenchContext context) {
		this.context = context;
		showIncludedStatusCheckBox.setSelected(context.getBlackboard().get(
				ToolboxModel.SHOWING_INCLUDED_STATUS_KEY, true));
		showInconsistentSegmentsCheckBox.setSelected(context.getBlackboard()
				.get(ToolboxModel.SHOWING_INCONSISTENT_SEGMENTS_KEY, false));
		showIntersectionsCheckBox.setSelected(context.getBlackboard().get(
				IntersectionStyle.SHOWING_INTERSECTIONS_KEY, true));
		highlightCheckBox.setSelected(HighlightManager.instance(context)
				.isHighlighting());
		intersectionSymbolComboBox.setModel(new DefaultComboBoxModel(
				new Object[]{new IntersectionStyle.TriangleFactory(),
						new IntersectionStyle.SquareFactory(),
						new IntersectionStyle.XFactory(),
						new IntersectionStyle.CircleFactory()}));
		highlightComboBox.setModel(new DefaultComboBoxModel(new Object[]{
				new HighlightManager.UnknownSegments(),
				new HighlightManager.IncludedSegments(),
				new HighlightManager.InconsistentSegments()}));
		try {
			intersectionSymbolComboBox.setSelectedItem(((Class) ApplicationOptionsPlugIn
					.options(context).get(
							IntersectionStyle.SHAPE_FACTORY_CLASS_KEY,
							IntersectionStyle.DEFAULT_SHAPE_FACTORY_CLASS))
					.newInstance());
			highlightComboBox.setSelectedItem(((Highlight) HighlightManager
					.instance(context).getHighlightClass().newInstance()));
		} catch (InstantiationException e) {
			Assert.shouldNeverReachHere(e.toString());
		} catch (IllegalAccessException e) {
			Assert.shouldNeverReachHere(e.toString());
		}
		colourSchemeComboBox.setModel(new DefaultComboBoxModel(new Vector(
				ColourSchemeRegistry.instance().getColourSchemes())));
		colourSchemeComboBox.setSelectedItem(ColourSchemeRegistry
				.current(context));
		colourSchemeComboBox.setRenderer(new ColorSchemeListCellRenderer() {
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				Component component = super.getListCellRendererComponent(list,
						value.toString(), index, isSelected, cellHasFocus);
				hideColourCount();
				return component;
			}
			private void hideColourCount() {
				JLabel label = (JLabel) GUIUtil.getDescendantOfClass(
						JLabel.class, this);
				label.setText(label.getText().substring(
						label.getText().indexOf(')') + 1));
			}
			protected ColorScheme colorScheme(String name) {
				return ColourSchemeRegistry.instance().get(name)
						.toJUMPColourScheme();
			}
		});
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		//Set value after #jbInit [Jon Aquino 2004-02-25]
		nodeSizeSlider.setValue(ApplicationOptionsPlugIn.options(context).get(
				NODE_SIZE_KEY, 4));
		//And add the ChangeListener only after the slider's value has been
		//set [Jon Aquino 2004-02-25] [Jon Aquino 2004-02-25]
		nodeSizeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				ApplicationOptionsPlugIn.options(context).put(NODE_SIZE_KEY,
						nodeSizeSlider.getValue());
				repaintDependents();
			}
		});
		updateEnabledStates();
	}
	void showIncludedStatusCheckBox_actionPerformed(ActionEvent e) {
		context.getBlackboard().put(ToolboxModel.SHOWING_INCLUDED_STATUS_KEY,
				showIncludedStatusCheckBox.isSelected());
		repaintDependentsImmediately();
		updateEnabledStates();
	}
	private void updateEnabledStates() {
		showInconsistentSegmentsCheckBox.setEnabled(showIncludedStatusCheckBox
				.isSelected());
		showIntersectionsCheckBox.setEnabled(showIncludedStatusCheckBox
				.isSelected());
		intersectionSymbolComboBox.setEnabled(showIncludedStatusCheckBox
				.isSelected());
	}
	void showInconsistentSegmentsCheckBox_actionPerformed(ActionEvent e) {
		context.getBlackboard().put(
				ToolboxModel.SHOWING_INCONSISTENT_SEGMENTS_KEY,
				showInconsistentSegmentsCheckBox.isSelected());
		repaintDependentsImmediately();
	}
	void showIntersectionsCheckBox_actionPerformed(ActionEvent e) {
		context.getBlackboard().put(
				IntersectionStyle.SHOWING_INTERSECTIONS_KEY,
				showIntersectionsCheckBox.isSelected());
		repaintDependentsImmediately();
	}
	void highlightCheckBox_actionPerformed(ActionEvent e) {
		HighlightManager.instance(context).setHighlighting(
				highlightCheckBox.isSelected());
		updateStyles();
		repaintDependentsImmediately();
	}
	private void repaintDependents() {
		repaintDependentsTimer.restart();
	}
	private JCheckBox showIncludedStatusCheckBox = new JCheckBox();
	private JCheckBox showInconsistentSegmentsCheckBox = new JCheckBox();
	private JCheckBox showIntersectionsCheckBox = new JCheckBox();
	private JCheckBox highlightCheckBox = new JCheckBox();
	private JPanel fillerPanel2 = new JPanel();
	private JLabel colourSchemeLabel = new JLabel();
	private JComboBox colourSchemeComboBox = new JComboBox();
	private JPanel fillerPanel1 = new JPanel();
	private JPanel nodeSizePanel = new JPanel();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private GridBagLayout gridBagLayout3 = new GridBagLayout();
	private JLabel nodeSizeLabel = new JLabel();
	private JSlider nodeSizeSlider = new JSlider();
	private JPanel intersectionSymbolPanel = new JPanel();
	private JPanel highlightPanel = new JPanel();
	private GridBagLayout gridBagLayout4 = new GridBagLayout();
	private GridBagLayout gridBagLayout5 = new GridBagLayout();
	private JComboBox intersectionSymbolComboBox = new JComboBox();
	private JComboBox highlightComboBox = new JComboBox();
	void jbInit() throws Exception {
		setLayout(gridBagLayout2);
		showIncludedStatusCheckBox.setText("Show Result Status");
		showInconsistentSegmentsCheckBox.setText("Show Inconsistent Segments");
		showIncludedStatusCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showIncludedStatusCheckBox_actionPerformed(e);
			}
		});
		showInconsistentSegmentsCheckBox
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						showInconsistentSegmentsCheckBox_actionPerformed(e);
					}
				});
		showIntersectionsCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showIntersectionsCheckBox_actionPerformed(e);
			}
		});
		highlightCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				highlightCheckBox_actionPerformed(e);
			}
		});
		colourSchemeLabel.setText("Colour Scheme:");
		colourSchemeComboBox
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						colourSchemeComboBox_actionPerformed(e);
					}
				});
		fillerPanel1.setPreferredSize(new Dimension(12, 12));
		fillerPanel1.setLayout(gridBagLayout1);
		nodeSizePanel.setLayout(gridBagLayout3);
		nodeSizeLabel.setText("Node Size: ");
		intersectionSymbolPanel.setLayout(gridBagLayout4);
		highlightPanel.setLayout(gridBagLayout5);
		showIntersectionsCheckBox.setText("Show Intersections As ");
		highlightCheckBox.setText("Highlight ");
		intersectionSymbolComboBox
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						intersectionSymbolComboBox_actionPerformed(e);
					}
				});
		highlightComboBox
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						highlightComboBox_actionPerformed(e);
					}
				});
		add(colourSchemeComboBox, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 0, 0));
		add(showInconsistentSegmentsCheckBox, new GridBagConstraints(1, 6, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		add(showIncludedStatusCheckBox, new GridBagConstraints(1, 4, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		add(fillerPanel2, new GridBagConstraints(101, 105, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(colourSchemeLabel, new GridBagConstraints(1, 1, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(fillerPanel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(nodeSizePanel,  new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(24, 0, 0, 0), 0, 0));
		nodeSizePanel.add(nodeSizeLabel, new GridBagConstraints(0, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		nodeSizePanel.add(nodeSizeSlider, new GridBagConstraints(1, 0, 1, 1,
				1.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		this.add(intersectionSymbolPanel, new GridBagConstraints(1, 5, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		this.add(highlightPanel, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 0, 0));
		highlightPanel.add(highlightCheckBox, new GridBagConstraints(1, 0, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		intersectionSymbolPanel.add(showIntersectionsCheckBox,
				new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		intersectionSymbolPanel.add(intersectionSymbolComboBox,
				new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0,
						GridBagConstraints.CENTER,
						GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0),
						0, 0));
		highlightPanel.add(highlightComboBox, new GridBagConstraints(2, 0, 1,
				1, 1.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		Hashtable labelTable = new Hashtable();
		labelTable.put(new Integer(5), new JLabel("5"));
		labelTable.put(new Integer(10), new JLabel("10"));
		labelTable.put(new Integer(15), new JLabel("15"));
		labelTable.put(new Integer(20), new JLabel("20"));
		nodeSizeSlider.setLabelTable(labelTable);
		nodeSizeSlider.setMinorTickSpacing(1);
		nodeSizeSlider.setMajorTickSpacing(0);
		nodeSizeSlider.setMinimum(4);
		nodeSizeSlider.setValue(4);
		nodeSizeSlider.setMaximum(20);
		nodeSizeSlider.setSnapToTicks(true);
		nodeSizeSlider.setPreferredSize(new Dimension(130, 49));
		nodeSizeSlider.setPaintLabels(true);
	}
	void intersectionSymbolComboBox_actionPerformed(ActionEvent e) {
		if (ApplicationOptionsPlugIn.options(context).get(
				IntersectionStyle.SHAPE_FACTORY_CLASS_KEY) == intersectionSymbolComboBox
				.getSelectedItem().getClass()) {
			return;
		}
		ApplicationOptionsPlugIn.options(context).put(
				IntersectionStyle.SHAPE_FACTORY_CLASS_KEY,
				intersectionSymbolComboBox.getSelectedItem().getClass());
		if (!showIntersectionsCheckBox.isSelected()) {
			showIntersectionsCheckBox.doClick();
			return;
		}
		repaintDependentsImmediately();
	}
	void highlightComboBox_actionPerformed(ActionEvent e) {
		if (HighlightManager.instance(context).getHighlightClass() == highlightComboBox
				.getSelectedItem().getClass()) {
			return;
		}
		HighlightManager.instance(context).setHighlightClass(
				highlightComboBox.getSelectedItem().getClass());
		if (!highlightCheckBox.isSelected()) {
			highlightCheckBox.doClick();
			return;
		}
		updateStyles();
		repaintDependentsImmediately();
	}
	void colourSchemeComboBox_actionPerformed(ActionEvent e) {
		if (ColourSchemeRegistry.current(context) == colourSchemeComboBox
				.getSelectedItem()) {
			return;
		}
		ApplicationOptionsPlugIn.options(context).put(ColourSchemeRegistry.CURRENT_KEY,
				colourSchemeComboBox.getSelectedItem().toString());
		updateStyles();
		repaintDependentsImmediately();
	}
	private void updateStyles() {
		for (Iterator i = toolboxModels(roadsLayerViewPanels()).iterator(); i
				.hasNext();) {
			ToolboxModel initializedToolboxModel = (ToolboxModel) i.next();
			initializedToolboxModel.updateStyles();
		}
	}
	private Collection toolboxModels(Collection roadsLayerViewPanels) {
		return CollectionUtil.collect(roadsLayerViewPanels, new Block() {
			public Object yield(Object roadsLayerViewPanel) {
				return ToolboxModel.instance(
						((LayerViewPanel) roadsLayerViewPanel)
								.getLayerManager(), context);
			}
		});
	}
	private Collection roadsLayerViewPanels() {
		Collection roadsLayerViewPanels = new ArrayList();
		for (Iterator i = Arrays.asList(
				context.getWorkbench().getFrame().getInternalFrames())
				.iterator(); i.hasNext();) {
			JInternalFrame internalFrame = (JInternalFrame) i.next();
			if (!(internalFrame instanceof LayerViewPanelProxy)) {
				continue;
			}
			ToolboxModel toolboxModel = ToolboxModel.instance(
					((LayerViewPanelProxy) internalFrame).getLayerViewPanel()
							.getLayerManager(), context);
			if (!toolboxModel.isInitialized()) {
				continue;
			}
			roadsLayerViewPanels.add(((LayerViewPanelProxy) internalFrame)
					.getLayerViewPanel());
		}
		return roadsLayerViewPanels;
	}
}