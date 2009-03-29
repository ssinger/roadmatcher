package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jump.util.LangUtil;
import com.vividsolutions.jump.workbench.model.LayerManager;

public class SegmentInfoPanel extends JPanel {

	protected static final Color COLUMN_1_BACKGROUND_COLOUR = new Color(255,
			204, 204);

	protected static final Color COLUMN_2_BACKGROUND_COLOUR = new Color(204,
			236, 255);

	public SegmentInfoPanel() {
		super();
		initialize();
	}

	private void initialize() {
		this.setLayout(new BorderLayout());
		this.add(getScrollPane(), java.awt.BorderLayout.CENTER);
	}

	public void indicateSegmentOutOfFocus() {
		if (this.indicatingSegmentInFocus == false) {
			// Don't repaint unnecessarily [Jon Aquino 2004-01-06]
			return;
		}
		this.indicatingSegmentInFocus = false;
		getTable().repaint();
	}

	public void updateText(SourceRoadSegment segment, LayerManager layerManager) {
		updateText(segment, (List) LangUtil.ifNull(layerManager.getBlackboard()
				.get(SegmentInfoPanelFactory.SEGMENT_INFO_ATTRIBUTES_0),
				defaultAttributeNames), (List) LangUtil.ifNull(
				(List) layerManager.getBlackboard().get(
						SegmentInfoPanelFactory.SEGMENT_INFO_ATTRIBUTES_1),
				defaultAttributeNames));
	}

	private void updateText(final SourceRoadSegment segment,
			final List attributeNames0, final List attributeNames1) {
		indicatingSegmentInFocus = true;
		removeRows();
		updateHeader(segment.getNetwork().getSession());
		addRows(segment, attributeNames0, attributeNames1);
	}

	private void removeRows() {
		for (int i = getTable().getRowCount() - 1; i >= 0; i--) {
			((DefaultTableModel) getTable().getModel()).removeRow(i);
		}
	}

	private void addRows(final SourceRoadSegment segment,
			final List attributeNames0, final List attributeNames1) {
		for (int i = 0; i < attributeNames0.size(); i++) {
			((DefaultTableModel) getTable().getModel()).addRow(new Object[] {
					attributeNames0.get(i),
					segment.getNetworkID() == 0 ? attributeValue(segment,
							(String) attributeNames0.get(i)) : segment
							.isMatched() ? attributeValue(segment
							.getMatchingRoadSegment(), (String) attributeNames0
							.get(i)) : NO_SEGMENT_MARKER,
					segment.getNetworkID() == 1 ? attributeValue(segment,
							(String) attributeNames1.get(i)) : segment
							.isMatched() ? attributeValue(segment
							.getMatchingRoadSegment(), (String) attributeNames1
							.get(i)) : NO_SEGMENT_MARKER,
					attributeNames1.get(i) });
		}
	}

	private void updateHeader(ConflationSession session) {
		getTable().getColumnModel().getColumn(1).setHeaderValue(
				session.getSourceNetwork(0).getName());
		getTable().getColumnModel().getColumn(2).setHeaderValue(
				session.getSourceNetwork(1).getName());
		getTable().repaint();
	}

	private static final Object NO_SEGMENT_MARKER = new Object() {
		public String toString() {
			return "";
		}
	};

	private Object attributeValue(SourceRoadSegment segment,
			String attributeName) {
		return attributeName != null ? segment.getFeature().getAttribute(
				attributeName) : null;
	}

	private TableCellRenderer segmentInFocusCellRenderer = new DefaultTableCellRenderer() {
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			JLabel label = (JLabel) super.getTableCellRendererComponent(table,
					value, isSelected, hasFocus, row, column);
			label.setForeground(column == 0 ? Color.red
					: column == 3 ? Color.blue : Color.black);
			label.setBackground(column == 1 ? greyIfNoSegmentMarker(value,
					COLUMN_1_BACKGROUND_COLOUR)
					: column == 2 ? greyIfNoSegmentMarker(value,
							COLUMN_2_BACKGROUND_COLOUR)
							: JPANEL_BACKGROUND_COLOUR);
			return label;
		}
	};

	private static final List defaultAttributeNames = new ArrayList(
			GenerateResultLayerPlugIn.conflationAttributeNames(false));

	private Color greyIfNoSegmentMarker(Object value, Color colour) {
		return value == NO_SEGMENT_MARKER ? JPANEL_BACKGROUND_COLOUR : colour;
	}

	private TableCellRenderer segmentOutOfFocusCellRenderer = new DefaultTableCellRenderer() {
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			JLabel label = (JLabel) super.getTableCellRendererComponent(table,
					value, isSelected, hasFocus, row, column);
			label.setForeground(column == 0 ? Color.red
					: column == 3 ? Color.blue : Color.black);
			label.setBackground(JPANEL_BACKGROUND_COLOUR);
			if (column == 1 || column == 2) {
				label.setText("");
			}
			return label;
		}
	};

	private JTable table = null;

	private JScrollPane scrollPane = null;

	private JTable getTable() {
		if (table == null) {
			// Use " ", not "", for the header text; otherwise the header
			// becomes a couple of millimeters high. [Jon Aquino 2005-01-06]
			table = new JTable(new DefaultTableModel(new Object[][] {},
					new Object[] { " ", " ", " ", " " })) {
				public TableCellRenderer getCellRenderer(int row, int column) {
					return cellRenderer;
				}
			};
		}
		return table;
	}

	private TableCellRenderer cellRenderer = new TableCellRenderer() {
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			return (indicatingSegmentInFocus ? segmentInFocusCellRenderer
					: segmentOutOfFocusCellRenderer)
					.getTableCellRendererComponent(table, value, isSelected,
							hasFocus, row, column);
		}
	};

	private boolean indicatingSegmentInFocus;

	private static final Color JPANEL_BACKGROUND_COLOUR = new JPanel()
			.getBackground();

	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
			scrollPane.setViewportView(getTable());
		}
		return scrollPane;
	}

}