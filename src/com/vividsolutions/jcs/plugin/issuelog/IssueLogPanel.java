package com.vividsolutions.jcs.plugin.issuelog;

import java.awt.*;

import javax.swing.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jcs.jump.FUTURE_GUIUtil;
import com.vividsolutions.jcs.plugin.conflate.roads.NewSessionPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.ToolboxModel;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.LangUtil;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.ButtonPanel;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;

import java.awt.event.*;

public class IssueLogPanel extends JPanel {

	private static final int MAX_RECORDED_DESCRIPTIONS_COUNT = 50;

	private WorkbenchContext context;

	private Date createdDate;

	private Date updatedDate;

	private static final String USER_DEFINED_LABEL = "< User-defined >";

	public IssueLogPanel(WorkbenchContext context) {
		this.context = context;
		fixLabelMinimumSizes();
		statusComboBox.setModel(new DefaultComboBoxModel(IssueLog.instance(
				context.getLayerManager()).getStatusCodes().toArray()));
		typeComboBox.setModel(new DefaultComboBoxModel(IssueLog.instance(
				context.getLayerManager()).getTypeCodes().toArray()));
		updateRecordedDescriptionsComboBox();
		descriptionTextArea.setFont(new JLabel().getFont());
		commentTextArea.setFont(new JLabel().getFont());
		GUIUtil.makeTabMoveFocus(descriptionTextArea);
		GUIUtil.makeTabMoveFocus(commentTextArea);
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		descriptionTextArea.getDocument().addDocumentListener(
				GUIUtil.toDocumentListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						if (transferringRecordedDescription) {
							return;
						}
						recordedDescriptionsComboBox
								.setSelectedItem(USER_DEFINED_LABEL);
					}
				}));
	}

	/**
	 * Prevent GridBagLayout from collapsing labels when their minimum width
	 * requirement is not satisfied.
	 */
	private void fixLabelMinimumSizes() {
		userNameLabel.setMinimumSize(new Dimension(0, 0));
		createdLabel.setMinimumSize(new Dimension(0, 0));
		updatedLabel.setMinimumSize(new Dimension(0, 0));
	}

	private boolean transferringRecordedDescription = false;

	private void updateRecordedDescriptionsComboBox() {
		recordedDescriptionsComboBox
				.setModel(new DefaultComboBoxModel(
						ToolboxModel.instance(context).getSession() == null ? new Object[] {}
								: insertUserDefinedLabel(
										(List) FUTURE_CollectionUtil
												.concatenate(
														(Collection) ToolboxModel
																.instance(
																		context)
																.getSession()
																.getBlackboard()
																.get(
																		NewSessionPlugIn.ISSUE_LOG_DESCRIPTIONS),
														recordedDescriptions()))
										.toArray()));
	}

	private List insertUserDefinedLabel(List recordedDescriptions) {
		List newRecordedDescriptions = new ArrayList();
		newRecordedDescriptions.add(USER_DEFINED_LABEL);
		newRecordedDescriptions.addAll(recordedDescriptions);
		return newRecordedDescriptions;
	}

	public void setType(String type) {
		typeComboBox.setSelectedItem(type);
	}

	public String getType() {
		return (String) typeComboBox.getSelectedItem();
	}

	public void setStatus(String status) {
		statusComboBox.setSelectedItem(status);
	}

	public String getStatus() {
		return (String) statusComboBox.getSelectedItem();
	}

	private List recordedDescriptions() {
		return (java.util.List) PersistentBlackboardPlugIn.get(context).get(
				getClass().getName() + " - RECORDED DESCRIPTIONS",
				new ArrayList());
	}

	public void setCreatedDate(Date createdDate) {
		createdLabel.setText("<html><b>Created:</b> " + string(createdDate)
				+ "</html>");
	}

	public void setUpdatedDate(Date updatedDate) {
		updatedLabel.setText("<html><b>Updated:</b> " + string(updatedDate)
				+ "</html>");
	}

	private String string(Date date) {
		return new SimpleDateFormat("d-MMM-yyyy").format(date);
	}

	public String getDescription() {
		return descriptionTextArea.getText().trim();
	}

	public String getComment() {
		return commentTextArea.getText().trim();
	}

	void jbInit() throws Exception {
		this.setLayout(gridBagLayout1);
		metadataPanel.setLayout(gridBagLayout2);
		typeAndStatusPanel.setLayout(gridBagLayout3);
		typeLabel.setText("Type: ");
		statusLabel.setText("Status: ");
		userNameLabel.setText("User: abcdefg");
		createdLabel.setText("Created: abcdefg");
		updatedLabel.setText("Updated: abcdefg");
		descriptionPanel.setLayout(gridBagLayout4);
		recordDescriptionButton.setMargin(new Insets(2, 2, 2, 2));
		recordDescriptionButton.setText("Record");
		recordDescriptionButton
				.addActionListener(new java.awt.event.ActionListener() {

					public void actionPerformed(ActionEvent e) {
						recordDescriptionButton_actionPerformed(e);
					}
				});
		recordDescriptionPanel.setLayout(gridBagLayout6);
		descriptionTextArea.setLineWrap(true);
		descriptionTextArea.setWrapStyleWord(true);
		descriptionLabel.setToolTipText("");
		descriptionLabel.setText("Description: ");
		recordedDescriptionsComboBox
				.addActionListener(new java.awt.event.ActionListener() {

					public void actionPerformed(ActionEvent e) {
						recordedDescriptionsComboBox_actionPerformed(e);
					}
				});
		descriptionScrollPane.setPreferredSize(new Dimension(377, 50));
		commentScrollPane.setPreferredSize(new Dimension(377, 100));
		commentTextArea.setLineWrap(true);
		commentTextArea.setWrapStyleWord(true);
		commentLabel.setText("Comment:");
		commentPanel.setLayout(gridBagLayout5);
		this.add(metadataPanel, new GridBagConstraints(0, 11, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		this.add(typeAndStatusPanel, new GridBagConstraints(0, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		typeAndStatusPanel.add(typeLabel, new GridBagConstraints(0, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		typeAndStatusPanel.add(typeComboBox, new GridBagConstraints(1, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		typeAndStatusPanel.add(statusLabel, new GridBagConstraints(2, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 10, 0, 0), 0, 0));
		typeAndStatusPanel.add(statusComboBox, new GridBagConstraints(3, 0, 1,
				1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(descriptionPanel, new GridBagConstraints(0, 8, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 0, 0, 0), 0, 0));
		descriptionPanel.add(descriptionScrollPane, new GridBagConstraints(1,
				1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		descriptionPanel.add(recordDescriptionPanel, new GridBagConstraints(1,
				0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		descriptionScrollPane.getViewport().add(descriptionTextArea, null);
		recordDescriptionPanel.add(descriptionLabel, new GridBagConstraints(0,
				0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		recordDescriptionPanel.add(recordedDescriptionsComboBox,
				new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 0, 0));
		recordDescriptionButton
				.setToolTipText("Saves the description below to the dropdown on the left");
		recordDescriptionPanel.add(recordDescriptionButton,
				new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 5, 0, 0), 0, 0));
		this.add(commentPanel, new GridBagConstraints(0, 10, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						5, 0, 0, 0), 0, 0));
		commentPanel.add(commentLabel, new GridBagConstraints(0, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		commentPanel.add(commentScrollPane, new GridBagConstraints(0, 1, 1, 1,
				1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		commentScrollPane.getViewport().add(commentTextArea, null);
		metadataPanel.add(userNameLabel, new GridBagConstraints(0, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 10), 0, 0));
		metadataPanel.add(createdLabel, new GridBagConstraints(1, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 10), 0, 0));
		metadataPanel.add(updatedLabel, new GridBagConstraints(2, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
	}

	public void setDescription(String description) {
		descriptionTextArea.setText(description);
		recordedDescriptionsComboBox.setSelectedItem(getDescription());
	}

	public void setComment(String comment) {
		commentTextArea.setText(comment);
	}

	private GridBagLayout gridBagLayout1 = new GridBagLayout();

	private JPanel metadataPanel = new JPanel();

	private GridBagLayout gridBagLayout2 = new GridBagLayout();

	private JPanel typeAndStatusPanel = new JPanel();

	private GridBagLayout gridBagLayout3 = new GridBagLayout();

	private JLabel typeLabel = new JLabel();

	private JComboBox typeComboBox = new JComboBox() {

		{
			setRenderer(new DefaultListCellRenderer() {

				private Icon warningIcon = GUIUtil.toSmallIcon(IconLoader
						.icon("Caution.gif"));

				private Icon errorIcon = GUIUtil.toSmallIcon(IconLoader
						.icon("Delete.gif"));

				private Icon commentIcon = GUIUtil.toSmallIcon(IconLoader
						.icon("Draw.gif"));

				public Component getListCellRendererComponent(JList list,
						Object value, int index, boolean isSelected,
						boolean cellHasFocus) {
					try {
						return super.getListCellRendererComponent(list, value,
								index, isSelected, cellHasFocus);
					} finally {
						setIcon(value
								.equals(IssueLog.AttributeValues.COMMENT_TYPE) ? commentIcon
								: value
										.equals(IssueLog.AttributeValues.WARNING_TYPE) ? warningIcon
										: value
												.equals(IssueLog.AttributeValues.ERROR_TYPE) ? errorIcon
												: null);
					}
				}
			});
		}
	};

	private JLabel statusLabel = new JLabel();

	private JComboBox statusComboBox = new JComboBox();

	private JLabel userNameLabel = new JLabel();

	private JLabel createdLabel = new JLabel();

	private JLabel updatedLabel = new JLabel();

	private JPanel descriptionPanel = new JPanel();

	private GridBagLayout gridBagLayout6 = new GridBagLayout();

	private JButton recordDescriptionButton = new JButton();

	private GridBagLayout gridBagLayout4 = new GridBagLayout();

	private JPanel recordDescriptionPanel = new JPanel();

	private JTextArea descriptionTextArea = new JTextArea();

	private JLabel descriptionLabel = new JLabel();

	private JComboBox recordedDescriptionsComboBox = new JComboBox() {

		{
			setRenderer(new DefaultListCellRenderer() {

				public Component getListCellRendererComponent(JList list,
						Object value, int index, boolean isSelected,
						boolean cellHasFocus) {
					return super.getListCellRendererComponent(list,
							value != null ? StringUtil.replaceAll(StringUtil
									.replaceAll((String) value, "\n", " "),
									"\r", " ") : value, index, isSelected,
							cellHasFocus);
				}
			});
		}
	};

	private JScrollPane descriptionScrollPane = new JScrollPane();

	private JTextArea commentTextArea = new JTextArea();

	private JLabel commentLabel = new JLabel();

	private GridBagLayout gridBagLayout5 = new GridBagLayout();

	private JPanel commentPanel = new JPanel();

	private JScrollPane commentScrollPane = new JScrollPane();

	void recordedDescriptionsComboBox_actionPerformed(ActionEvent e) {
		if (recordedDescriptionsComboBox.getSelectedItem().equals(
				USER_DEFINED_LABEL)) {
			return;
		}
		transferringRecordedDescription = true;
		try {
			descriptionTextArea.setText((String) recordedDescriptionsComboBox
					.getSelectedItem());
		} finally {
			transferringRecordedDescription = false;
		}
	}

	public JTextArea getDescriptionTextArea() {
		return descriptionTextArea;
	}

	public void setUserName(String userName) {
		userNameLabel.setText("<html><b>User:</b> "
				+ LangUtil.ifNull(userName, "") + "</html>");
	}

	public static IssueLogPanel prompt(String title, String description,
			String comment, String status, String type, Date createdDate,
			Date modifiedDate, String userName, ButtonPanel buttonPanel,
			WorkbenchContext workbenchContext) {
		final IssueLogPanel issueLogPanel = new IssueLogPanel(workbenchContext);
		issueLogPanel.setDescription(description);
		issueLogPanel.setComment(comment);
		issueLogPanel.setStatus(status);
		issueLogPanel.setUserName(userName);
		issueLogPanel.setType(type);
		issueLogPanel.setCreatedDate(createdDate);
		issueLogPanel.setUpdatedDate(modifiedDate);
		final JDialog dialog = new JDialog(workbenchContext.getWorkbench()
				.getFrame(), title, true);
		dialog.getContentPane().setLayout(new BorderLayout());
		dialog.getContentPane().add(issueLogPanel, BorderLayout.CENTER);
		dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});
		dialog.pack();
		dialog.addWindowListener(new WindowAdapter() {

			public void windowOpened(WindowEvent e) {
				issueLogPanel.getDescriptionTextArea().requestFocus();
			}
		});
		GUIUtil.centreOnWindow(dialog);
		dialog.setVisible(true);
		return issueLogPanel;
	}

	void recordDescriptionButton_actionPerformed(ActionEvent e) {
		if (getDescription().length() == 0) {
			return;
		}
		recordedDescriptions().remove(getDescription());
		recordedDescriptions().add(0, getDescription());
		if (recordedDescriptions().size() > MAX_RECORDED_DESCRIPTIONS_COUNT) {
			recordedDescriptions().subList(MAX_RECORDED_DESCRIPTIONS_COUNT,
					recordedDescriptions().size()).clear();
		}
		updateRecordedDescriptionsComboBox();
		recordedDescriptionsComboBox.setSelectedItem(getDescription());
	}
}