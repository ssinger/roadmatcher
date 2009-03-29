package com.vividsolutions.jcs.jump;

import java.awt.Shape;
import java.awt.geom.NoninvertibleTransformException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.StyleUtil;

public class FUTURE_StyleUtil {
    public static Shape _toShape(Geometry geometry, Viewport viewport) throws NoninvertibleTransformException {
    	return (Shape) FUTURE_LangUtil.invokePrivateMethod("toShape", new StyleUtil(), StyleUtil.class, new Object[] {geometry, viewport}, new Class[] {Geometry.class, Viewport.class});
    }
}
