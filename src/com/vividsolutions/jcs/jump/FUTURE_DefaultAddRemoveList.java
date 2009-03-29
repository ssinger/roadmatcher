package com.vividsolutions.jcs.jump;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.workbench.ui.InputChangedFirer;
import com.vividsolutions.jump.workbench.ui.InputChangedListener;
import com.vividsolutions.jump.workbench.ui.JListTypeAheadKeyListener;
import com.vividsolutions.jump.workbench.ui.addremove.AddRemoveList;
import com.vividsolutions.jump.workbench.ui.addremove.AddRemoveListModel;

public class FUTURE_DefaultAddRemoveList extends JPanel implements
		AddRemoveList {
	private BorderLayout borderLayout1 = new BorderLayout();

	private JList list = new JList();

	private AddRemoveListModel model;

	private InputChangedFirer inputChangedFirer = new InputChangedFirer();

	private Border border1;

	public FUTURE_DefaultAddRemoveList() {
		this(new FUTURE_DefaultAddRemoveListModel());
	}

	public void add(MouseListener listener) {
		list.addMouseListener(listener);
	}

	public FUTURE_DefaultAddRemoveList(FUTURE_AddRemoveListModel model) {
		this.model = model;
		list.setModel(listModel(model));
		list.addKeyListener(new JListTypeAheadKeyListener(list));
		list.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						inputChangedFirer.fire();
					}
				});

		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private ListModel listModel(final FUTURE_AddRemoveListModel model) {
		final Collection listDataListeners = new ArrayList();
		return new ListModel() {

			public int getSize() {
				return model.getItems().size();
			}

			public Object getElementAt(int index) {
				return model.getItems().get(index);
			}

			public void addListDataListener(ListDataListener l) {
				model.addListener(l);
			}

			public void removeListDataListener(ListDataListener l) {
				model.removeListener(l);
			}

		};
	}

	public void setSelectedItems(Collection items) {
		ArrayList indicesToSelect = new ArrayList();

		for (Iterator i = items.iterator(); i.hasNext();) {
			Object item = (Object) i.next();
			int index = getModel().getItems().indexOf(item);

			if (index == -1) {
				continue;
			}

			indicesToSelect.add(new Integer(index));
		}

		int[] indexArray = new int[indicesToSelect.size()];

		for (int i = 0; i < indicesToSelect.size(); i++) {
			Integer index = (Integer) indicesToSelect.get(i);
			indexArray[i] = index.intValue();
		}

		list.setSelectedIndices(indexArray);
	}

	public AddRemoveListModel getModel() {
		return model;
	}

	public void add(InputChangedListener listener) {
		inputChangedFirer.add(listener);
	}

	public JList getList() {
		return list;
	}

	public List getSelectedItems() {
		return Arrays.asList(list.getSelectedValues());
	}

	void jbInit() throws Exception {
		border1 = new EtchedBorder(EtchedBorder.RAISED, new Color(0, 0, 51),
				new Color(0, 0, 25));
		this.setLayout(borderLayout1);
		this.add(list, BorderLayout.CENTER);
	}
}