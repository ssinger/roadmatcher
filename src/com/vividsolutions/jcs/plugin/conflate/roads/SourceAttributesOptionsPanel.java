package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.jump.FUTURE_AbstractAddRemoveListModel;
import com.vividsolutions.jcs.jump.FUTURE_AddRemoveListModel;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jcs.jump.FUTURE_DefaultAddRemoveList;
import com.vividsolutions.jcs.jump.FUTURE_DefaultAddRemoveListModel;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.ui.ColorPanel;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.addremove.AddRemoveList;
import com.vividsolutions.jump.workbench.ui.addremove.AddRemoveListModel;
import com.vividsolutions.jump.workbench.ui.addremove.AddRemovePanel;
import com.vividsolutions.jump.workbench.ui.addremove.DefaultAddRemoveList;
import com.vividsolutions.jump.workbench.ui.addremove.DefaultAddRemoveListModel;

public class SourceAttributesOptionsPanel extends JPanel implements
		OptionsPanel {
	private GridBagLayout gridBagLayout1 = new GridBagLayout();

	private JPanel sourcePanel = new JPanel();

	private GridBagLayout gridBagLayout2 = new GridBagLayout();

	private JLabel sourceLabel = new JLabel();

	private JTextArea instructionsTextArea = new JTextArea();

	private DefaultComboBoxModel datasetComboBoxModel = new DefaultComboBoxModel();

	private JComboBox datasetComboBox = PrecedenceOptionsPanel
			.createDatasetComboBox(datasetComboBoxModel);

	private AddRemovePanel addRemovePanel = new AddRemovePanel(false) {
		{
			((DefaultAddRemoveListModel) getRightList().getModel())
					.setSorted(true);
		}
	};

	private WorkbenchContext context;

	public SourceAttributesOptionsPanel(WorkbenchContext context) {
		this.context = context;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		instructionsTextArea.setFont(new JLabel().getFont());
	}

	void jbInit() throws Exception {
		this.setLayout(gridBagLayout1);
		sourcePanel.setLayout(gridBagLayout2);
		sourceLabel.setText("Source:");
		instructionsTextArea.setOpaque(false);
		instructionsTextArea.setEditable(false);
		instructionsTextArea
				.setText("Choose the attributes of each Source dataset to be included in the "
						+ "Result dataset.");
		instructionsTextArea.setLineWrap(true);
		instructionsTextArea.setWrapStyleWord(true);
		datasetComboBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				datasetComboBox_actionPerformed(e);
			}
		});
		fillerPanel1.setPreferredSize(new Dimension(12, 12));
		this.setPreferredSize(new Dimension(420, 320));
		this.add(sourcePanel, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		sourcePanel.add(sourceLabel, new GridBagConstraints(0, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(instructionsTextArea, new GridBagConstraints(2, 1, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 12, 0), 0, 0));
		sourcePanel.add(datasetComboBox, new GridBagConstraints(1, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(addRemovePanel, new GridBagConstraints(2, 4, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(fillerPanel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
	}

	private ToolboxModel getToolboxModel() {
		return ToolboxModel.instance(context.getLayerManager(), context);
	}

	public String validateInput() {
		return null;
	}

	public void okPressed() {
		configureDatasetAttributesToInclude(0);
		configureDatasetAttributesToInclude(1);
	}

	private ResultOptions configureDatasetAttributesToInclude(
			final int networkID) {
		return ResultOptions.get(getSession()).setDatasetAttributesToInclude(
				networkID, unwrap(networkID, addRemovePanel.getRightItems()));
	}

	private List unwrap(final int networkID, Collection wrappers) {
		return (List) CollectionUtil.collect(CollectionUtil.select(wrappers,
				new Block() {
					public Object yield(Object wrapper) {
						return Boolean
								.valueOf(((AttributeWrapper) wrapper).networkID == networkID);
					}
				}), new Block() {
			public Object yield(Object wrapper) {
				return ((AttributeWrapper) wrapper).name;
			}
		});
	}

	private ConflationSession getSession() {
		return ToolboxModel.instance(context).getSession();
	}

	private JPanel fillerPanel1 = new JPanel();

	private static class EventEnablingAddRemoveListModel implements
			FUTURE_AddRemoveListModel {
		private FUTURE_AddRemoveListModel addRemoveListModel;

		private boolean eventsEnabled = false;

		public EventEnablingAddRemoveListModel(
				FUTURE_AddRemoveListModel addRemoveListModel) {
			this.addRemoveListModel = addRemoveListModel;
		}

		public void addListener(ListDataListener listener) {
			addRemoveListModel.addListener(wrap(listener));
		}

		private ListDataListener wrap(final ListDataListener listener) {
			return new ListDataListener() {

				public void contentsChanged(ListDataEvent e) {
					if (!eventsEnabled) {
						return;
					}
					listener.contentsChanged(e);
				}

				public void intervalAdded(ListDataEvent e) {
					if (!eventsEnabled) {
						return;
					}
					listener.intervalAdded(e);
				}

				public void intervalRemoved(ListDataEvent e) {
					if (!eventsEnabled) {
						return;
					}
					listener.intervalRemoved(e);
				}
			};
		}

		public void removeListener(ListDataListener listener) {
			throw new UnsupportedOperationException(
					"Todo: find wrapped listener [Jon Aquino 2004-09-28]");
		}

		public void add(Object item) {
			addRemoveListModel.add(item);
		}

		public void setItems(Collection items) {
			addRemoveListModel.setItems(items);
		}

		public List getItems() {
			return addRemoveListModel.getItems();
		}

		public void remove(Object item) {
			addRemoveListModel.remove(item);
		}

		public void setEventsEnabled(boolean eventsEnabled) {
			this.eventsEnabled = eventsEnabled;
		}
	}

	private static class DelegatingAddRemoveListModel extends
			FUTURE_AbstractAddRemoveListModel {
		private EventEnablingAddRemoveListModel[] addRemoveListModels;

		private int currentAddRemoveListModel = 0;

		private boolean initialized = false;

		public DelegatingAddRemoveListModel(FUTURE_AddRemoveListModel a,
				FUTURE_AddRemoveListModel b) {
			addRemoveListModels = new EventEnablingAddRemoveListModel[] {
					new EventEnablingAddRemoveListModel(a),
					new EventEnablingAddRemoveListModel(b) };
		}

		public void addListener(ListDataListener listener) {
			addRemoveListModels[0].addListener(listener);
			addRemoveListModels[1].addListener(listener);
			super.addListener(listener);
		}

		public void removeListener(ListDataListener listener) {
			addRemoveListModels[0].removeListener(listener);
			addRemoveListModels[1].removeListener(listener);
			super.removeListener(listener);
		}

		public void add(Object item) {
			addRemoveListModels[((AttributeWrapper) item).networkID].add(item);
		}

		public void setItems(Collection items) {
			if (!items.isEmpty()) {
				for (Iterator i = items.iterator(); i.hasNext();) {
					AttributeWrapper item = (AttributeWrapper) i.next();
					Assert.isTrue(item.networkID == ((AttributeWrapper) items
							.iterator().next()).networkID);
				}
			}
			addRemoveListModels[currentAddRemoveListModel].setItems(items);
		}

		public List getItems() {
			return addRemoveListModels[currentAddRemoveListModel].getItems();
		}

		public void remove(Object item) {
			addRemoveListModels[((AttributeWrapper) item).networkID]
					.remove(item);
		}

		public void setCurrentAddRemoveListModel(int currentAddRemoveListModel) {
			int oldSize = addRemoveListModels[this.currentAddRemoveListModel]
					.getItems().size();
			int newSize = addRemoveListModels[currentAddRemoveListModel]
					.getItems().size();
			this.currentAddRemoveListModel = currentAddRemoveListModel;
			addRemoveListModels[currentAddRemoveListModel]
					.setEventsEnabled(true);
			addRemoveListModels[1 - currentAddRemoveListModel]
					.setEventsEnabled(false);
			if (!initialized) {
				initialized = true;
				return;
			}
			fireStructureChanged(oldSize, newSize);
		}
	}

	private DelegatingAddRemoveListModel delegatingAddRemoveListModel;

	public void init() {
		delegatingAddRemoveListModel = new DelegatingAddRemoveListModel(
				createLeftAddRemoveListModel(0),
				createLeftAddRemoveListModel(1));
		addRemovePanel.setLeftList(new FUTURE_DefaultAddRemoveList(
				delegatingAddRemoveListModel));
		addRemovePanel.getRightList().getModel().setItems(
				FUTURE_CollectionUtil.concatenate(wrap(0, ResultOptions.get(
						getSession()).getDatasetAttributesToInclude(0)), wrap(
						1, ResultOptions.get(getSession())
								.getDatasetAttributesToInclude(1))));
		datasetComboBoxModel.removeAllElements();
		datasetComboBoxModel.addElement(getToolboxModel().getSourceLayer(0));
		datasetComboBoxModel.addElement(getToolboxModel().getSourceLayer(1));
		((FUTURE_DefaultAddRemoveList) addRemovePanel.getLeftList()).getList()
				.setCellRenderer(
						new AttributeRenderer(ToolboxModel.instance(context)));
		((DefaultAddRemoveList) addRemovePanel.getRightList()).getList()
				.setCellRenderer(
						new AttributeRenderer(ToolboxModel.instance(context)));
		updateAddRemovePanel();
	}

	private FUTURE_AddRemoveListModel createLeftAddRemoveListModel(int networkID) {
		return new FUTURE_DefaultAddRemoveListModel(
				wrap(networkID, FUTURE_CollectionUtil.removeAll(ResultOptions
						.allAttributes(networkID, getSession()), ResultOptions
						.get(getSession()).getDatasetAttributesToInclude(
								networkID)))).setSorted(true);
	}

	private FeatureSchema schema(int networkID) {
		return getSession().getSourceNetwork(networkID).getFeatureCollection()
				.getFeatureSchema();
	}

	private List wrap(final int networkID, Collection names) {
		return (List) CollectionUtil.collect(names, new Block() {
			public Object yield(Object name) {
				return new AttributeWrapper(networkID, schema(networkID)
						.getAttributeIndex((String) name), (String) name);
			}
		});
	}

	private class AttributeWrapper implements Comparable {
		private int networkID;

		private int i;

		private String name;

		public AttributeWrapper(int networkID, int i, String name) {
			this.networkID = networkID;
			this.i = i;
			this.name = name;
		}

		public int compareTo(Object o) {
			return networkID != ((AttributeWrapper) o).networkID ? networkID
					- ((AttributeWrapper) o).networkID : i
					- ((AttributeWrapper) o).i;
		}

		public boolean equals(Object o) {
			return compareTo(o) == 0;
		}

		public int hashCode() {
			return name.hashCode();
		}

		public String toString() {
			return name;
		}
	}

	private void updateAddRemovePanel() {
		if (datasetComboBox.getSelectedIndex() == -1) {
			//Get here during initialization [Jon Aquino 2004-05-03]
			return;
		}
		delegatingAddRemoveListModel
				.setCurrentAddRemoveListModel(datasetComboBox
						.getSelectedIndex());
	}

	void datasetComboBox_actionPerformed(ActionEvent e) {
		updateAddRemovePanel();
	}

	private static class AttributeRenderer extends JPanel implements
			ListCellRenderer {
		private JLabel label = new JLabel();

		private ColorPanel colorPanel = new ColorPanel();

		private ToolboxModel toolboxModel;

		public AttributeRenderer(ToolboxModel toolboxModel) {
			this.toolboxModel = toolboxModel;
			colorPanel.setMaximumSize(new Dimension(4, 4));
			colorPanel.setMinimumSize(new Dimension(4, 4));
			colorPanel.setPreferredSize(new Dimension(4, 4));
			JPanel colorPanelPanel = new JPanel(new GridBagLayout());
			colorPanelPanel.add(colorPanel, new GridBagConstraints(0, 0, 1, 1,
					0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
					new Insets(0, 5, 0, 5), 0, 0));
			colorPanelPanel.setOpaque(false);
			setLayout(new BorderLayout());
			add(colorPanelPanel, BorderLayout.WEST);
			add(label, BorderLayout.CENTER);
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			label.setText(value.toString());
			colorPanel.setFillColor(toolboxModel.getSourceLayer(
					((AttributeWrapper) value).networkID).getBasicStyle()
					.getFillColor());
			colorPanel.setLineColor(colorPanel.getFillColor());
			if (isSelected) {
				label.setForeground(list.getSelectionForeground());
				label.setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
				setBackground(list.getSelectionBackground());
			} else {
				label.setForeground(list.getForeground());
				label.setBackground(list.getBackground());
				setForeground(list.getForeground());
				setBackground(list.getBackground());
			}
			return this;
		}
	}
}