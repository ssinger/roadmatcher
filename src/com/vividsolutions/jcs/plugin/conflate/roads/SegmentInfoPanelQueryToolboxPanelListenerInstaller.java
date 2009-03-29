package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.jump.FUTURE_GUIUtil;
import com.vividsolutions.jcs.jump.WeakHashSet;
import com.vividsolutions.jcs.plugin.clean.coveragecleaningtoolbox.TableTab;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.AttributePanel;
import com.vividsolutions.jump.workbench.ui.AttributeTablePanel;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.InfoModelListener;
import com.vividsolutions.jump.workbench.ui.LayerTableModel;

public class SegmentInfoPanelQueryToolboxPanelListenerInstaller {

	private WeakHashSet installedAttributePanels = new WeakHashSet();

	private WeakHashSet installedAttributeTablePanels = new WeakHashSet();

	public void install(final WorkbenchContext context,
			final SegmentInfoPanel segmentInfoPanel) {
		TableTab tableTab = (TableTab) GUIUtil.getDescendantOfClass(
				TableTab.class, QueryToolboxPlugIn.instance(context)
						.getToolbox(context));
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AttributePanel attributePanel = (AttributePanel) GUIUtil
						.getDescendantOfClass(AttributePanel.class,
								QueryToolboxPlugIn.instance(context)
										.getToolbox(context));
				if (attributePanel == null) {
					return;
				}
				if (installedAttributePanels.contains(attributePanel)) {
					return;
				}
				install(attributePanel, segmentInfoPanel);
			}
		};
		tableTab.addActionListener(listener);
		listener.actionPerformed(null);
	}

	private void install(final AttributePanel attributePanel,
			final SegmentInfoPanel segmentInfoPanel) {
		InfoModelListener listener = new InfoModelListener() {
			public void layerAdded(LayerTableModel layerTableModel) {
				FUTURE_GUIUtil.visitComponentTree(attributePanel,
						new FUTURE_GUIUtil.Visitor() {
							public void visit(Component component) {
								if (!(component instanceof AttributeTablePanel)) {
									return;
								}
								if (installedAttributeTablePanels
										.contains(component)) {
									return;
								}
								install((AttributeTablePanel) component,
										segmentInfoPanel);
							}
						});
			}

			public void layerRemoved(LayerTableModel layerTableModel) {
			}
		};
		attributePanel.getModel().addListener(listener);
		listener.layerAdded(null);
		installedAttributePanels.add(attributePanel);
	}

	private void install(final AttributeTablePanel attributeTablePanel,
			final SegmentInfoPanel segmentInfoPanel) {
		final ListSelectionListener listener = new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (attributeTablePanel.getTable().getSelectedRowCount() != 1) {
					segmentInfoPanel.indicateSegmentOutOfFocus();
					return;
				}
				if (!(selectedFeature(attributeTablePanel) instanceof SourceFeature)) {
					segmentInfoPanel.indicateSegmentOutOfFocus();
					return;
				}
				segmentInfoPanel.updateText(
						((SourceFeature) selectedFeature(attributeTablePanel))
								.getRoadSegment(), attributeTablePanel
								.getModel().getLayer().getLayerManager());
			}

			private Feature selectedFeature(
					final AttributeTablePanel attributeTablePanel) {
				return attributeTablePanel.getModel().getFeature(
						attributeTablePanel.getTable().getSelectedRow());
			}
		};
		attributeTablePanel.getTable().getSelectionModel()
				.addListSelectionListener(listener);
		// User may try to re-click the selected row (e.g. after moving to some
		// whitespace on the map, blanking the Segment Info panel). But
		// Selection Listener event will not fire in this case; so add a 
		// MouseListener. [Jon Aquino 2004-01-10]
		attributeTablePanel.getTable().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				listener.valueChanged(null);
			}
		});
		installedAttributeTablePanels.add(attributeTablePanel);
	}
}