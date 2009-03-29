package com.vividsolutions.jcs.jump;

import java.util.Map;

import com.vividsolutions.jump.workbench.ui.InfoModel;

public class FUTURE_InfoModel {

    public static Map protectedGetLayerToTableModelMap(InfoModel infoModel) {
        return (Map) FUTURE_LangUtil.getPrivateField("layerToTableModelMap",
                infoModel, InfoModel.class);
    }

}
