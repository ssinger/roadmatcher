package com.vividsolutions.jcs.jump;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.HashMap;
import java.util.Map;

public class FUTURE_CardLayoutWrapper {

    private int lastID = 0;

    private Container container;

    private Component topComponent = null;

    public FUTURE_CardLayoutWrapper(Container container) {
        container.setLayout(new CardLayout());
        this.container = container;
    }

    public void add(Component component) {
        container.add(component, id(component));
        topComponent = topComponent == null ? component : topComponent;
    }

    private String id(Component component) {
        if (!componentToIDMap.containsKey(component)) {
            componentToIDMap.put(component, Double.toString(lastID++));
        }
        return (String) componentToIDMap.get(component);
    }

    private Map componentToIDMap = new HashMap();

    public void setTopComponent(Component topComponent) {
        ((CardLayout) container.getLayout()).show(container,
                (String) componentToIDMap.get(topComponent));
        this.topComponent = topComponent;
    }

    public Component getTopComponent() {
        return topComponent;
    }
}
