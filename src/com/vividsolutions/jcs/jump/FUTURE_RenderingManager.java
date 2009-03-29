package com.vividsolutions.jcs.jump;
import java.util.List;
import com.vividsolutions.jump.workbench.ui.renderer.RenderingManager;
public class FUTURE_RenderingManager {
    public static List contentIDs(RenderingManager renderingManager) {
        return (List) FUTURE_LangUtil.invokePrivateMethod("contentIDs",
                renderingManager, RenderingManager.class, new Object[]{},
                new Class[]{});
    }
}