package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.Color;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import com.vividsolutions.jcs.jump.FUTURE_GUIUtil;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.java2xml.Java2XML;
import com.vividsolutions.jump.util.java2xml.XML2Java;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorScheme;

public class ColourSchemeRegistry {
	private ColourSchemeRegistry() {
		//Can't rename the first colour scheme to "... 1", because at
		//least one colour scheme must have the name specified by
		//ColourScheme.DEFAULT_NAME [Jon Aquino]
		addColourScheme(new ColourScheme());
		addColourScheme(new ColourScheme() {
			{
				setName(getName() + " 2");
				setPendingInnerLineColour0(getPendingInnerLineColour0().brighter());
				setPendingInnerLineColour1(getPendingInnerLineColour1().brighter());
                setInconsistentInnerLineColour0(getInconsistentInnerLineColour0().brighter());
                setInconsistentInnerLineColour1(getInconsistentInnerLineColour1().brighter());
                setIntegratedInnerLineColour0(getIntegratedInnerLineColour0().brighter());
                setIntegratedInnerLineColour1(getIntegratedInnerLineColour1().brighter());
			}
		});
		addColourScheme(new ColourScheme() {
			{
				setName(getName() + " 3");
                setPendingInnerLineColour0(getPendingInnerLineColour0().brighter().brighter());
                setPendingInnerLineColour1(getPendingInnerLineColour1().brighter().brighter());
                setInconsistentInnerLineColour0(getInconsistentInnerLineColour0().brighter().brighter());
                setInconsistentInnerLineColour1(getInconsistentInnerLineColour1().brighter().brighter());
                setIntegratedInnerLineColour0(getIntegratedInnerLineColour0().brighter().brighter());
                setIntegratedInnerLineColour1(getIntegratedInnerLineColour1().brighter().brighter());
			}
		});
		addColourScheme(generateColourScheme("Purple/Green", Color.getHSBColor(
				(float) (192.0 / 256.0), (float) 1.0, (float) 0.75), Color
				.getHSBColor((float) (108.0 / 256.0), (float) 1.0, (float) .75)));
		addColourScheme(generateColourScheme("Magenta/Turquoise",
				Color.getHSBColor((float) (240.0 / 256.0), (float) 1.0,
						(float) 1.0), Color.getHSBColor(
						(float) (128.0 / 256.0), (float) .75, (float) .75)));
		addColourScheme(generateColourScheme("Orange/Sky Blue", Color
				.getHSBColor((float) (16.0 / 256.0), (float) 1.0, (float) 1.0),
				Color.getHSBColor((float) (144.0 / 256.0), (float) 1.0,
						(float) 1.0)));
		//generateColourSchemes();
	}

	public ColourScheme get(String name) {
		return (ColourScheme) nameToColourSchemeMap.get(name);
	}
	public static ColourScheme current(WorkbenchContext context) {
		if (actualCurrent(context) == null) {
			//Get here when names change [Jon Aquino 2004-02-20]
			ApplicationOptionsPlugIn.options(context).put(CURRENT_KEY,
					ColourScheme.DEFAULT_NAME);
		}
		return actualCurrent(context);
	}
	private static ColourScheme actualCurrent(WorkbenchContext context) {
		return instance().get(
				(String) ApplicationOptionsPlugIn.options(context).get(CURRENT_KEY,
						ColourScheme.DEFAULT_NAME));
	}
	public static final String CURRENT_KEY = ColourSchemeRegistry.class
			.getName()
			+ " - CURRENT";
	private List colourSchemes = new ArrayList();
	public List getColourSchemes() {
		return Collections.unmodifiableList(colourSchemes);
	}
	public void addColourScheme(ColourScheme colourScheme) {
		colourSchemes.add(colourScheme);
		nameToColourSchemeMap.put(colourScheme.getName(), colourScheme);
	}
	private Map nameToColourSchemeMap = new HashMap();
	public static void main(String[] args) throws Exception {
		System.out.println(new Java2XML().write(instance(), "root"));
	}
	private static ColourSchemeRegistry instance = null;
	//Avoid name confusion [Jon Aquino 2004-02-19]
	private static class JUMPColourScheme extends ColorScheme {
		public JUMPColourScheme(String name, Collection colors) {
			super(name, colors);
		}
	}
	public static ColourSchemeRegistry instance() {
		if (instance == null) {
			instance = new ColourSchemeRegistry();
		}
		return instance;
	}

	private ColourScheme generateColourScheme(String name, Color colour0,
			Color colour1) {
		ColourScheme colourScheme = new ColourScheme();
		colourScheme.setName(name);
		colourScheme.setDefaultColour0(GUIUtil.alphaColor(colour0, colourScheme
				.getDefaultColour0().getAlpha()));
		colourScheme.setDefaultColour1(GUIUtil.alphaColor(colour1, colourScheme
				.getDefaultColour1().getAlpha()));
		colourScheme.setUnknownColour0(GUIUtil.alphaColor(colourScheme
				.getDefaultColour0(), colourScheme.getUnknownColour0()
				.getAlpha()));
		colourScheme.setUnknownColour1(GUIUtil.alphaColor(colourScheme
				.getDefaultColour1(), colourScheme.getUnknownColour1()
				.getAlpha()));
        colourScheme.setPendingInnerLineColour0(GUIUtil.alphaColor(colour0.darker()
                .darker(), colourScheme.getPendingInnerLineColour0().getAlpha()));
        colourScheme.setPendingInnerLineColour1(GUIUtil.alphaColor(colour1.darker()
                .darker(), colourScheme.getPendingInnerLineColour1().getAlpha()));
        colourScheme.setInconsistentInnerLineColour0(GUIUtil.alphaColor(colour0.darker()
                .darker(), colourScheme.getInconsistentInnerLineColour0().getAlpha()));
        colourScheme.setInconsistentInnerLineColour1(GUIUtil.alphaColor(colour1.darker()
                .darker(), colourScheme.getInconsistentInnerLineColour1().getAlpha()));
        colourScheme.setIntegratedInnerLineColour0(GUIUtil.alphaColor(colour0.darker()
                .darker(), colourScheme.getIntegratedInnerLineColour0().getAlpha()));
        colourScheme.setIntegratedInnerLineColour1(GUIUtil.alphaColor(colour1.darker()
                .darker(), colourScheme.getIntegratedInnerLineColour1().getAlpha()));
		colourScheme.setMatchedNonReferenceColour0(GUIUtil.alphaColor(
				colourScheme.getDefaultColour0(), colourScheme
						.getMatchedNonReferenceColour0().getAlpha()));
		colourScheme.setMatchedNonReferenceColour1(GUIUtil.alphaColor(
				colourScheme.getDefaultColour1(), colourScheme
						.getMatchedNonReferenceColour1().getAlpha()));
		colourScheme.setRetiredColour0(GUIUtil.alphaColor(colourScheme
				.getDefaultColour0(), colourScheme.getRetiredColour0()
				.getAlpha()));
		colourScheme.setRetiredColour1(GUIUtil.alphaColor(colourScheme
				.getDefaultColour1(), colourScheme.getRetiredColour1()
				.getAlpha()));
		return colourScheme;
	}
}
