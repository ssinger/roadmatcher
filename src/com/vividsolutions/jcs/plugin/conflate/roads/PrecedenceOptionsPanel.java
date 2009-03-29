package com.vividsolutions.jcs.plugin.conflate.roads;

import java.util.*;
import java.util.List;
import javax.swing.*;
import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.ReferenceDatasetPrecedenceRuleEngine;
import com.vividsolutions.jcs.conflate.roads.model.PrecedenceRuleEngine;
import com.vividsolutions.jcs.conflate.roads.model.RoadNetworkFeatureCollection;
import com.vividsolutions.jcs.conflate.roads.model.ScriptedPrecedenceRuleEngine;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerNameRenderer;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import java.awt.*;
import java.awt.event.*;

public class PrecedenceOptionsPanel extends JPanel implements OptionsPanel {
	private GridBagLayout gridBagLayout1 = new GridBagLayout();

	private JLabel label = new JLabel();

	private JPanel scriptPanel = new JPanel();

	private JPanel javaClassPanel = new JPanel();

	private GridBagLayout gridBagLayout2 = new GridBagLayout();

	private GridBagLayout gridBagLayout3 = new GridBagLayout();

	private JRadioButton scriptRadioButton = new JRadioButton();

	private JRadioButton javaClassRadioButton = new JRadioButton();

	private JButton editButton = new JButton();

	private JTextField javaClassTextField = new JTextField();

	private WorkbenchContext context;

	private Map radioButtonToModeMap = new HashMap();

	public PrecedenceOptionsPanel(final WorkbenchContext context) {
		this.context = context;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		add(referenceDatasetRadioButton, new Mode() {
			public String validateInput() {
				return null;
			}

			public Class getPrecedenceRuleEngineClass() {
				return ReferenceDatasetPrecedenceRuleEngine.class;
			}

			public void init(PrecedenceRuleEngine engine) {
			}

			public PrecedenceRuleEngine createPrecedenceRuleEngine() {
				return new ReferenceDatasetPrecedenceRuleEngine()
						.setReferenceDatasetName(getToolboxModel().getSession()
								.getSourceNetwork(
										referenceDatasetComboBox
												.getSelectedIndex()).getName());
			}
		});
		add(scriptRadioButton, new Mode() {
			public String validateInput() {
				return script.indexOf("chooseReference") == -1 ? "Script must define a function \"SourceRoadSegment chooseReference(SourceRoadSegment, SourceRoadSegment)\""
						: null;
			}

			public Class getPrecedenceRuleEngineClass() {
				return ScriptedPrecedenceRuleEngine.class;
			}

			public void init(PrecedenceRuleEngine engine) {
				script = ((ScriptedPrecedenceRuleEngine) engine).getScript();
			}

			public PrecedenceRuleEngine createPrecedenceRuleEngine() {
				return new ScriptedPrecedenceRuleEngine().setScript(script);
			}
		});
		add(javaClassRadioButton, new Mode() {
			public String validateInput() {
				try {
					return !PrecedenceRuleEngine.class
							.isAssignableFrom(getJavaClass()) ? StringUtil
							.toFriendlyName(getJavaClass().getName())
							+ " is not an instance of "
							+ StringUtil
									.toFriendlyName(PrecedenceRuleEngine.class
											.getName()) : null;
				} catch (Exception e) {
					return WorkbenchFrame.toMessage(e);
				}
			}

			public Class getPrecedenceRuleEngineClass() {
				return PrecedenceRuleEngine.class;
			}

			public void init(PrecedenceRuleEngine engine) {
				javaClassTextField.setText(engine.getClass().getName());
			}

			public PrecedenceRuleEngine createPrecedenceRuleEngine() {
				try {
					return (PrecedenceRuleEngine) getJavaClass().newInstance();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			private Class getJavaClass() throws ClassNotFoundException {
				return Class.forName(javaClassTextField.getText().trim(), true,
						getClass().getClassLoader());
			}
		});
		editButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scriptRadioButton.setSelected(true);
			}
		});
		javaClassTextField.getDocument().addDocumentListener(
				GUIUtil.toDocumentListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						javaClassRadioButton.setSelected(true);
					}
				}));
	}

	private void add(JRadioButton radioButton, Mode mode) {
		buttonGroup.add(radioButton);
		radioButtonToModeMap.put(radioButton, mode);
	}

	private interface Mode {
		public String validateInput();

		public Class getPrecedenceRuleEngineClass();

		public void init(PrecedenceRuleEngine engine);

		public PrecedenceRuleEngine createPrecedenceRuleEngine();
	}

	private ButtonGroup buttonGroup = new ButtonGroup();

	void jbInit() throws Exception {
		label.setText("When assigning the reference segment in a match use:");
		this.setLayout(gridBagLayout1);
		scriptPanel.setLayout(gridBagLayout2);
		javaClassPanel.setLayout(gridBagLayout3);
		scriptRadioButton.setText("Rules in a BeanShell script");
		javaClassRadioButton.setText("Rules in a Java class");
		editButton.setText("Edit...");
		editButton.setMargin(new Insets(2, 2, 2, 2));
		editButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editButton_actionPerformed(e);
			}
		});
		javaClassTextField.setPreferredSize(new Dimension(200, 21));
		fillerPanel1.setPreferredSize(new Dimension(12, 12));
		fillerPanel1.setLayout(gridBagLayout4);
		referenceDatasetPanel.setLayout(gridBagLayout5);
		referenceDatasetRadioButton.setText("Reference Dataset");
		referenceDatasetComboBox.setActionCommand("");
		referenceDatasetComboBox
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						referenceDatasetComboBox_actionPerformed(e);
					}
				});
		fillerPanel2.setLayout(gridBagLayout6);
		this.add(label, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 10, 0), 0, 0));
		this.add(scriptPanel, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		scriptPanel.add(scriptRadioButton, new GridBagConstraints(0, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		scriptPanel.add(editButton, new GridBagConstraints(1, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(javaClassPanel, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		javaClassPanel.add(javaClassRadioButton, new GridBagConstraints(0, 0,
				1, 1, 1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		javaClassPanel.add(javaClassTextField, new GridBagConstraints(1, 0, 1,
				1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(fillerPanel2, new GridBagConstraints(2, 6, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(fillerPanel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(referenceDatasetPanel, new GridBagConstraints(1, 3, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		referenceDatasetPanel.add(referenceDatasetRadioButton,
				new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		referenceDatasetPanel.add(referenceDatasetComboBox,
				new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
	}

	private String script = "chooseReference(a, b) {\n    return a.getNetworkID() == 0 ? a : b;\n}";

	private JPanel fillerPanel2 = new JPanel();

	private JPanel fillerPanel1 = new JPanel();

	private GridBagLayout gridBagLayout4 = new GridBagLayout();

	private JPanel referenceDatasetPanel = new JPanel();

	private GridBagLayout gridBagLayout5 = new GridBagLayout();

	private JRadioButton referenceDatasetRadioButton = new JRadioButton();

	private DefaultComboBoxModel referenceDatasetComboBoxModel = new DefaultComboBoxModel();

	private JComboBox referenceDatasetComboBox = createDatasetComboBox(referenceDatasetComboBoxModel);

	private GridBagLayout gridBagLayout6 = new GridBagLayout();

	void editButton_actionPerformed(ActionEvent e) {
		JTextArea textArea = new JTextArea(script);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().add(textArea);
		scrollPane.setPreferredSize(new Dimension(500, 500));
		if (JOptionPane.CANCEL_OPTION == JOptionPane.showOptionDialog(context
				.getWorkbench().getFrame(), scrollPane,
				"Edit Script for Precedence Rule",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
				null, null)) {
			return;
		}
		script = textArea.getText();
	}

	public static JComboBox createDatasetComboBox(DefaultComboBoxModel model) {
		return new JComboBox(model) {
			{
				setRenderer(new LayerNameRenderer() {
					public Component getListCellRendererComponent(JList list,
							Object value, int index, boolean isSelected,
							boolean cellHasFocus) {
						try {
							return super.getListCellRendererComponent(list,
									value, index, isSelected, cellHasFocus);
						} finally {
							getLabel()
									.setText(
											((RoadNetworkFeatureCollection) ((Layer) value)
													.getFeatureCollectionWrapper()
													.getUltimateWrappee())
													.getNetwork().getName());
						}
					}
				});
			}
		};
	}

	public String validateInput() {
		return getCurrentMode().validateInput();
	}

	public void okPressed() {
		getToolboxModel().getSession().setPrecedenceRuleEngine(
				createPrecedenceRuleEngine());
	}

	private ToolboxModel getToolboxModel() {
		return ToolboxModel.instance(context.getLayerManager(), context);
	}

	private PrecedenceRuleEngine createPrecedenceRuleEngine() {
		return getCurrentMode().createPrecedenceRuleEngine();
	}

	private Mode getCurrentMode() {
		return (Mode) radioButtonToModeMap.get(getSelectedRadioButton());
	}

	private JRadioButton getSelectedRadioButton() {
		for (Iterator i = radioButtonToModeMap.keySet().iterator(); i.hasNext();) {
			JRadioButton radioButton = (JRadioButton) i.next();
			if (radioButton.isSelected()) {
				return radioButton;
			}
		}
		Assert.shouldNeverReachHere();
		return null;
	}

	public void init() {
		initReferenceDatasetComboBox();
		//Initialize the radio buttons after the combo box, because setting
		//the combo box sets the radio button [Jon Aquino 2004-04-30]
		initRadioButtonSelection();
	}

	private void initReferenceDatasetComboBox() {
		referenceDatasetComboBoxModel.removeAllElements();
		referenceDatasetComboBoxModel.addElement(getToolboxModel()
				.getSourceLayer(0));
		referenceDatasetComboBoxModel.addElement(getToolboxModel()
				.getSourceLayer(1));
		referenceDatasetComboBox
				.setSelectedIndex(getToolboxModel().getSession()
						.getPrecedenceRuleEngine() instanceof ReferenceDatasetPrecedenceRuleEngine ? getToolboxModel()
						.getSession()
						.getSourceNetwork(
								((ReferenceDatasetPrecedenceRuleEngine) getToolboxModel()
										.getSession().getPrecedenceRuleEngine())
										.getReferenceDatasetName()).getID()
						: 0);
	}

	private void initRadioButtonSelection() {
		PrecedenceRuleEngine engine = getToolboxModel().getSession()
				.getPrecedenceRuleEngine();
		List radioButtons = new ArrayList(radioButtonToModeMap.keySet());
		//Check Java-class-mode last, because its #isInstance will always
		//return true [Jon Aquino 2004-04-20]
		radioButtons.remove(javaClassRadioButton);
		radioButtons.add(javaClassRadioButton);
		for (Iterator i = radioButtons.iterator(); i.hasNext();) {
			JRadioButton radioButton = (JRadioButton) i.next();
			Mode mode = (Mode) radioButtonToModeMap.get(radioButton);
			if (mode.getPrecedenceRuleEngineClass().isInstance(engine)) {
				mode.init(engine);
				radioButton.setSelected(true);
				return;
			}
		}
		Assert.shouldNeverReachHere();
	}

	void referenceDatasetComboBox_actionPerformed(ActionEvent e) {
		referenceDatasetRadioButton.setSelected(true);
	}
}