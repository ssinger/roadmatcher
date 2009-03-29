package com.vividsolutions.jcs.jump;
import java.util.Collection;
/**
 * Workaround to enable Java2XML to handle collections directly.
 */
public class FUTURE_Java2XMLCollectionWrapper {
    private Collection collection;
    public Collection getCollection() {
        return collection;
    }
    public void setCollection(Collection collection) {
        this.collection = collection;
    }
    public FUTURE_Java2XMLCollectionWrapper() {
    }
    public FUTURE_Java2XMLCollectionWrapper(Collection collection) {
        setCollection(collection);
    }
}