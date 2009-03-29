package com.vividsolutions.jcs.jump;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import com.vividsolutions.jcs.plugin.conflate.roads.SpecifyRoadFeaturesTool;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.MathUtil;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;

public class FUTURE_GUIUtil {
	public static interface Visitor {
		public void visit(Component component);
	}

	public static void visitComponentTree(Component component, Visitor visitor) {
		visitor.visit(component);
		if (component instanceof Container) {
			for (int i = 0; i < ((Container) component).getComponentCount(); i++) {
				visitComponentTree(((Container) component).getComponent(i),
						visitor);
			}
		}
	}
	
    public static String pad(String s) {
        return (s.length() == 1) ? ("0" + s) : s;
    }
    public static String toHTML(Color color) {
        String colorString = "#";
        colorString += pad(Integer.toHexString(color.getRed()));
        colorString += pad(Integer.toHexString(color.getGreen()));
        colorString += pad(Integer.toHexString(color.getBlue()));
        return colorString;
    }	

	public static AbstractButton selectedButton(ButtonGroup group) {
		for (Enumeration e = group.getElements(); e.hasMoreElements();) {
			AbstractButton button = (AbstractButton) e.nextElement();
			if (button.getModel() == group.getSelection()) {
				return button;
			}
		}
		return null;
	}

	public static Component getDescendantOfClass(final Class c,
			Container container) {
		return findDescendant(container, new Block() {
			public Object yield(Object child) {
				return Boolean.valueOf(c.isInstance(child));
			}
		});
	}

	public static Component findDescendant(Container container,
			final Block condition) {
		final Component descendant[] = { null };
		visitDescendants(container, new Block() {
			public Object yield(Object child) {
				if (condition.yield(child) == Boolean.TRUE) {
					descendant[0] = (Component) child;
					return Boolean.TRUE;
				}
				return Boolean.FALSE;
			}
		});
		return descendant[0];
	}

	public static boolean visitDescendants(Container container, Block visitor) {
		Collection children = new ArrayList();
		FUTURE_CollectionUtil.addAll(children, container.getComponents());
		if (container instanceof Window) {
			FUTURE_CollectionUtil.addAll(children, ((Window) container)
					.getOwnedWindows());
		}
		for (Iterator i = children.iterator(); i.hasNext();) {
			Component child = (Component) i.next();
			// Check that result is a Boolean [Jon Aquino 2004-08-04]
			Boolean result = (Boolean) visitor.yield(child);
			if (result == Boolean.TRUE) {
				return true;
			}
			if (child instanceof Container) {
				if (visitDescendants((Container) child, visitor)) {
					return true;
				}
			}
		}
		return false;
	}

	public static Cursor createCursorFromIcon(String iconImage) {
		//Don't use GUIUtil#resize, which uses SCALE_SMOOTH, which
		//makes the check-mark icons chunky-looking.
		//[2004-02-27]
		ImageIcon icon = new ImageIcon(SpecifyRoadFeaturesTool.createIcon(
				iconImage).getImage().getScaledInstance(12, 12,
				Image.SCALE_REPLICATE));
		ImageIcon basicCursor = new ImageIcon(SpecifyRoadFeaturesTool.class
				.getResource("images/basic-cursor.png"));
		BufferedImage image = new BufferedImage(basicCursor.getIconWidth(),
				basicCursor.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.drawImage(basicCursor.getImage(), 0, 0, null);
		graphics.drawImage(icon.getImage(), 10, 10, null);
		return AbstractCursorTool.createCursor(image, new Point(0, 15));
	}

	public static Color average(Color a, Color b) {
		return new Color((int) MathUtil.avg(a.getRed(), b.getRed()),
				(int) MathUtil.avg(a.getGreen(), b.getGreen()), (int) MathUtil
						.avg(a.getBlue(), b.getBlue()), (int) MathUtil.avg(a
						.getAlpha(), b.getAlpha()));
	}

	public static Color multiply(double saturationMultiplier,
			double brightnessMultiplier, double alphaMultiplier, Color color) {
		float hsb[] = Color.RGBtoHSB(color.getRed(), color.getGreen(), color
				.getBlue(), null);
		return GUIUtil.alphaColor(Color.getHSBColor(hsb[0],
				(float) (hsb[1] * saturationMultiplier),
				(float) (hsb[2] * brightnessMultiplier)), (int) (color
				.getAlpha() * alphaMultiplier));
	}

	public static String toHexString(Color color) {
		return Integer.toHexString(color.getRGB() & 0x00ffffff);
	}

	/**
	 * From http://javaalmanac.com/egs/javax.swing.text/LimitText.html
	 */
	public static class FixedSizePlainDocument extends PlainDocument {
		int maxSize;

		public FixedSizePlainDocument(int limit) {
			maxSize = limit;
		}

		public void insertString(int offs, String str, AttributeSet a)
				throws BadLocationException {
			if ((getLength() + str.length()) <= maxSize) {
				super.insertString(offs, str, a);
			}
		}
	}

	/**
	 * From http://mindprod.com/jgloss/focus.html. Requires JDK 1.4+.
	 */
	public static Component makeTabMoveFocus(Component component) {
		component.setFocusTraversalKeys(
				KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, new HashSet(
						Collections.singleton(KeyStroke.getKeyStroke("TAB"))));
		component.setFocusTraversalKeys(
				KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, new HashSet(
						Collections.singleton(KeyStroke
								.getKeyStroke("shift TAB"))));
		return component;
	}
}