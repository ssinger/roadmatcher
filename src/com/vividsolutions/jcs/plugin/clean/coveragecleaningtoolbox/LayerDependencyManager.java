package com.vividsolutions.jcs.plugin.clean.coveragecleaningtoolbox;

import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerAdapter;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerComboBox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Keeps track of whether dependents (tabs in our case) are in sync with a layer.
 * We use this class to make tab text display in red when the tab needs refreshing.
 */
public class LayerDependencyManager {
    private Object[] dependents;
    private List dependentsIgnoringLayerChanges = new ArrayList();

    

    public void addDependencyListener(DependencyListener listener) {
        listeners.add(listener);
    }

    public LayerDependencyManager(Object[] dependents,
        final LayerComboBox layerComboBox) {
        this.dependents = dependents;
        layerComboBox.getModel().addListDataListener(GUIUtil.toListDataListener(
                new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    monitor(layerComboBox.getSelectedLayer());
                    fireDependencyChanged();
                }
            }));
        monitor(layerComboBox.getSelectedLayer());
        fireDependencyChanged();
    }

    private void monitor(final Layer layer) {
        if (layer == null) {
            return;
        }

        final String MONITORING_KEY = getClass() + " - MONITORING";

        if (layer.getBlackboard().get(MONITORING_KEY, false)) {
            return;
        }

        layer.getLayerManager().addLayerListener(new LayerAdapter() {
                public void featuresChanged(FeatureEvent e) {
                    if (e.getLayer() == layer) {
                        markDependentsAsOutOfDate(layer);
                    }
                }
            });
        layer.getBlackboard().put(MONITORING_KEY, true);
    }

    public void markAsUpToDate(Object dependent, Layer layer) {
        getUpToDateDependents(layer).add(dependent);
        fireDependencyChanged();
    }

    private void markDependentsAsOutOfDate(Layer layer) {
        for (Iterator i = getUpToDateDependents(layer).iterator(); i.hasNext(); ) {
            Object dependent = i.next();
            if (!dependentsIgnoringLayerChanges.contains(dependent)) {
                i.remove();
            }
        }
        fireDependencyChanged();
    }

    private Set getUpToDateDependents(Layer layer) {      
        try {
        return (Set) layer.getBlackboard().get(getClass() +
            " - UP TO DATE DEPENDENTS", new HashSet());
        }
        catch (NullPointerException e) {
            throw e;
        }
    }

    /**
     * @return true if layer is null
     */
    public boolean isUpToDate(Object dependent, Layer layer) {
        if (layer == null) {
            return true;
        }
        
        if (!CollectionUtil.containsReference(dependents, dependent)) {
            return true;
        }
        
        return getUpToDateDependents(layer).contains(dependent);
    }

    private void fireDependencyChanged() {
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            DependencyListener listener = (DependencyListener) i.next();
            listener.dependencyChanged();
        }
    }
    
    public static interface DependencyListener {
        public void dependencyChanged();
    }
    
    private List listeners = new ArrayList();

    public void setIgnoringLayerChanges(Object dependent, boolean ignoring) {
        if (ignoring) { 
            dependentsIgnoringLayerChanges.add(dependent);
        }
        else {
            dependentsIgnoringLayerChanges.remove(dependent);
        }
    }

    public void copyState(Layer source, Layer destination) {
        if (source == destination) {
            //input layer = output layer [Jon Aquino]
            return;}
        getUpToDateDependents(destination).clear();
        getUpToDateDependents(destination).addAll(getUpToDateDependents(source));
        fireDependencyChanged();                
    }
    protected Object[] getDependents() {
        return dependents;
    }

}
