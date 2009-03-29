package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.Color;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.java2xml.Java2XML;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorScheme;
public class ColourScheme implements Cloneable {
	public static final String DEFAULT_NAME = "Red/Blue";
	private String name = DEFAULT_NAME;
	private Color adjustedColour = GUIUtil.alphaColor(Color.orange, 150);
	private Color defaultColour0 = Color.red;
	private Color defaultColour1 = Color.blue;
	private Color ringColour = Color.green.darker();
	private Color unknownColour0 = GUIUtil.alphaColor(Color.red, 155);
	private Color unknownColour1 = GUIUtil.alphaColor(Color.blue, 155);
	private Color integratedColour = new Color(122, 255, 88, 150);
	private Color retiredColour0 = new Color(200, 0, 0, 75);
	private Color retiredColour1 = new Color(0, 0, 200, 75);
	private Color matchedNonReferenceColour0 = GUIUtil
			.alphaColor(Color.red, 75);
	private Color matchedNonReferenceColour1 = GUIUtil.alphaColor(Color.blue,
			75);
	private Color pendingInnerLineColour0 = new Color(100, 0, 0);
	private Color pendingInnerLineColour1 = new Color(0, 0, 100);
	private Color inconsistentInnerLineColour0 = pendingInnerLineColour0;
	private Color inconsistentInnerLineColour1 = pendingInnerLineColour1;
	private Color integratedInnerLineColour0 = pendingInnerLineColour0;
	private Color integratedInnerLineColour1 = pendingInnerLineColour1;
	private Color inconsistentColour = new Color(255, 0, 255);
	private Color postponedInconsistentColour = new Color(234,212,234);
	public Color getAdjustedColour() {
		return adjustedColour;
	}
	public ColourScheme setAdjustedColour(Color adjustedColor) {
		this.adjustedColour = adjustedColor;
		return this;
	}
	public Color getDefaultColour0() {
		return defaultColour0;
	}
	public ColourScheme setDefaultColour0(Color defaultColour0) {
		this.defaultColour0 = defaultColour0;
		return this;
	}
	public Color getDefaultColour1() {
		return defaultColour1;
	}
	public ColourScheme setDefaultColour1(Color defaultColour1) {
		this.defaultColour1 = defaultColour1;
		return this;
	}
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			Assert.shouldNeverReachHere();
			return null;
		}
	}
	public ColourScheme brighter() {
		ColourScheme clone = (ColourScheme) clone();
		for (Iterator i = Arrays.asList(getClass().getDeclaredFields())
				.iterator(); i.hasNext();) {
			Field field = (Field) i.next();
			if (field.getType() != Color.class) {
				continue;
			}
			try {
				field.set(clone, brighter(((Color) field.get(clone))));
			} catch (IllegalArgumentException e) {
				Assert.shouldNeverReachHere();
			} catch (IllegalAccessException e) {
				Assert.shouldNeverReachHere();
			}
		}
		return clone;
	}
	private Object brighter(Color color) {
		return GUIUtil.alphaColor(color.brighter(), color.getAlpha());
	}
	public Color getInconsistentColour() {
		return inconsistentColour;
	}
	public Color getPostponedInconsistentColour() {		
		return postponedInconsistentColour;
	}
	public Color getIntegratedColour() {
		return integratedColour;
	}
	public String toString() {
		return getName();
	}
	public ColourScheme setIntegratedColour(Color integratedColour) {
		this.integratedColour = integratedColour;
		return this;
	}
	public Color getMatchedNonReferenceColour0() {
		return matchedNonReferenceColour0;
	}
	public ColourScheme setMatchedNonReferenceColour0(
			Color matchedNonReferenceColour0) {
		this.matchedNonReferenceColour0 = matchedNonReferenceColour0;
		return this;
	}
	public Color getMatchedNonReferenceColour1() {
		return matchedNonReferenceColour1;
	}
	public ColourScheme setMatchedNonReferenceColour1(
			Color matchedNonReferenceColour1) {
		this.matchedNonReferenceColour1 = matchedNonReferenceColour1;
		return this;
	}
	public Color getRetiredColour0() {
		return retiredColour0;
	}
	public ColourScheme setRetiredColour0(Color retiredColour0) {
		this.retiredColour0 = retiredColour0;
		return this;
	}
	public Color getRetiredColour1() {
		return retiredColour1;
	}
	public ColourScheme setRetiredColour1(Color retiredColour1) {
		this.retiredColour1 = retiredColour1;
		return this;
	}
	public Color getUnknownColour0() {
		return unknownColour0;
	}
	public ColourScheme setUnknownColour0(Color unknownColour0) {
		this.unknownColour0 = unknownColour0;
		return this;
	}
	public Color getUnknownColour1() {
		return unknownColour1;
	}
	public ColourScheme setUnknownColour1(Color unknownColour1) {
		this.unknownColour1 = unknownColour1;
		return this;
	}
	public static void main(String[] args) throws Exception {
		System.out.println(new Java2XML().write(new ColourScheme(), "root"));
	}
	public String getName() {
		return name;
	}
	public ColourScheme setName(String name) {
		this.name = name;
		return this;
	}
	public ColorScheme toJUMPColourScheme() {
		return new ColorScheme(name, Arrays.asList(new Object[]{
				getDefaultColour0(), getDefaultColour1(),
				getPendingInnerLineColour0(), getPendingInnerLineColour1(),
				getIntegratedColour()}));
	}
	public Color getRingColour() {
		return ringColour;
	}
	public ColourScheme setRingColour(Color ringColour) {
		this.ringColour = ringColour;
		return this;
	}
	public Color getInconsistentInnerLineColour0() {
		return inconsistentInnerLineColour0;
	}
	public ColourScheme setInconsistentInnerLineColour0(
			Color inconsistentInnerLineColour0) {
		this.inconsistentInnerLineColour0 = inconsistentInnerLineColour0;
        return this;
	}
	public Color getInconsistentInnerLineColour1() {
		return inconsistentInnerLineColour1;
	}
	public ColourScheme setInconsistentInnerLineColour1(
			Color inconsistentInnerLineColour1) {
		this.inconsistentInnerLineColour1 = inconsistentInnerLineColour1;
        return this;
	}
	public Color getIntegratedInnerLineColour0() {
		return integratedInnerLineColour0;
	}
	public ColourScheme setIntegratedInnerLineColour0(Color integratedInnerLineColour0) {
		this.integratedInnerLineColour0 = integratedInnerLineColour0;
        return this;
	}
	public Color getIntegratedInnerLineColour1() {
		return integratedInnerLineColour1;
	}
	public ColourScheme setIntegratedInnerLineColour1(Color integratedInnerLineColour1) {
		this.integratedInnerLineColour1 = integratedInnerLineColour1;
        return this;
	}
	public Color getPendingInnerLineColour0() {
		return pendingInnerLineColour0;
	}
	public ColourScheme setPendingInnerLineColour0(Color pendingInnerLineColour0) {
		this.pendingInnerLineColour0 = pendingInnerLineColour0;
        return this;
	}
	public Color getPendingInnerLineColour1() {
		return pendingInnerLineColour1;
	}
	public ColourScheme setPendingInnerLineColour1(Color pendingInnerLineColour1) {
		this.pendingInnerLineColour1 = pendingInnerLineColour1;
        return this;
	}
}
