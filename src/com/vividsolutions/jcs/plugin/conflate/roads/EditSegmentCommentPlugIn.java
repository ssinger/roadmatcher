package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jcs.jump.FUTURE_GUIUtil;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.model.FeatureEventType;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public class EditSegmentCommentPlugIn extends RightClickSegmentPlugIn {

	protected boolean execute(final List segments, final PlugInContext context) {
		final List oldComments = (List) CollectionUtil.collect(segments,
				new Block() {
					public Object yield(Object segment) {
						return ((SourceRoadSegment) segment).getComment();
					}
				});
		Object[] promptResult = prompt(
				oldComments.size() == 1 ? (String) oldComments.get(0) : "",
				context.getWorkbenchFrame(), ((Collection) ToolboxModel
						.instance(context).getSession().getBlackboard().get(
								NewSessionPlugIn.SEGMENT_COMMENTS)),
				((Boolean) ToolboxModel.instance(context).getSession()
						.getBlackboard().get(
								NewSessionPlugIn.SEGMENT_COMMENTS_EDITABLE))
						.booleanValue(), StringUtil
						.toCommaDelimitedString(new HashSet(CollectionUtil
								.collect(segments, new Block() {
									public Object yield(Object segment) {
										return ((SourceRoadSegment) segment)
												.getNetwork().getName();
									}
								}))));
		Assert.isTrue(promptResult[0] instanceof Boolean);
		if (promptResult[0] == Boolean.FALSE) {
			return false;
		}
		final String newComment = (String) promptResult[1];
		execute(new UndoableCommand(getName()) {
			public void execute() {
				for (int i = 0; i < segments.size(); i++) {
					((SourceRoadSegment) segments.get(i))
							.setComment(newComment);
				}
				fireFeaturesChanged(0, segments, context);
				fireFeaturesChanged(1, segments, context);
			}

			public void unexecute() {
				for (int i = 0; i < segments.size(); i++) {
					((SourceRoadSegment) segments.get(i))
							.setComment((String) oldComments.get(i));
				}
				fireFeaturesChanged(0, segments, context);
				fireFeaturesChanged(1, segments, context);
			}
		}, context);
		return true;
	}

	private Object[] prompt(final String comment, Component parentComponent,
			Collection predefinedComments, boolean segmentCommentsEditable,
			String networks) {
		// ShapefileWriter does not support strings longer than 255 characters
		// [Jon Aquino 2004-09-24]
		final JTextArea textArea = (JTextArea) FUTURE_GUIUtil
				.makeTabMoveFocus(new JTextArea(
						new FUTURE_GUIUtil.FixedSizePlainDocument(255)) {
					{
						setText(comment);
						setFont(new JLabel().getFont());
						setLineWrap(true);
						setWrapStyleWord(true);
					}
				});
		textArea.setEnabled(segmentCommentsEditable);
		textArea.setBackground(segmentCommentsEditable ? textArea
				.getBackground() : new JPanel().getBackground());
		JButton clearButton = new JButton("Clear") {
			{
				addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						textArea.setText("");
					}
				});
			}
		};
		clearButton.setEnabled(segmentCommentsEditable);
		JComboBox comboBox = new JComboBox(new Vector(FUTURE_CollectionUtil
				.concatenate(Collections.singleton(""), predefinedComments))) {
			{
				addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						textArea.setText((String) getSelectedItem());
					}
				});
			}
		};
		return new Object[] {
				Boolean.valueOf(JOptionPane.OK_OPTION == JOptionPane
						.showOptionDialog(parentComponent, createPanel(
								comboBox, textArea, clearButton,
								segmentCommentsEditable), getName() + " for "
								+ networks, JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.PLAIN_MESSAGE, null, null, null)),
				textArea.getText() };
	}

	public static final int FIRST_COLUMN_WIDTH = 380;

	private JPanel createPanel(final JComboBox comboBox,
			final JTextArea textArea, final JButton clearButton,
			final boolean segmentCommentsEditable) {
		comboBox.setPreferredSize(new Dimension(FIRST_COLUMN_WIDTH,
				(int) comboBox.getPreferredSize().getHeight()));
		return new JPanel(new GridBagLayout()) {
			{
				add(new JLabel("Choose predefined comment"
						+ (segmentCommentsEditable ? ", or edit existing text"
								: "") + "."), new GridBagConstraints(0, 0, 2,
						1, 0, 0, GridBagConstraints.NORTHWEST,
						GridBagConstraints.NONE, new Insets(0, 0, 4, 0), 0, 0));
				add(comboBox, new GridBagConstraints(0, 1, 1, 1, 0, 0,
						GridBagConstraints.NORTHWEST,
						GridBagConstraints.HORIZONTAL, new Insets(0, 0, 4, 0),
						0, 0));
				add(new JScrollPane(textArea) {
					{
						setPreferredSize(new Dimension(FIRST_COLUMN_WIDTH, 80));
					}
				}, new GridBagConstraints(0, 2, 1, 1, 1, 1,
						GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				add(clearButton, new GridBagConstraints(1, 2, 1, 1, 0, 0,
						GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
						new Insets(0, 4, 0, 0), 0, 0));
			}
		};
	}
}