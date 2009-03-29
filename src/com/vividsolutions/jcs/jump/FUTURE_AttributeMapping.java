package com.vividsolutions.jcs.jump;
import java.util.*;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
public class FUTURE_AttributeMapping {
    private List aAttributeNames;
    private List bAttributeNames;
    private List aNewAttributeNames;
    private List bNewAttributeNames;
    protected FUTURE_AttributeMapping() {
        //for testing [Jon Aquino]
    }
    /**
     * Constructs an AttributeMapping that will transfer all the attributes from
     * two feature collections A and B. If A and B have attributes with the same
     * name, they will be postfixed with _1 and _2 respectively. Case sensitive.
     * If you only wish to map attributes for one schema, simply pass in a new
     * FeatureSchema for the other schema.
     * 
     * @param a
     *                 schema for first feature collection from which to transfer
     *                 attributes
     * @param b
     *                 schema for second feature collection from which to transfer
     *                 attributes
     */
    public FUTURE_AttributeMapping(FeatureSchema a, FeatureSchema b) {
        List newAttributeNamesA = ensureUnique(nonSpatialAttributeNames(a),
                new ArrayList());
        List newAttributeNamesB = ensureUnique(nonSpatialAttributeNames(b),
                newAttributeNamesA);
        init(nonSpatialAttributeNames(a), newAttributeNamesA,
                nonSpatialAttributeNames(b), newAttributeNamesB);
    }
    /**
     * Constructs an AttributeMapping.
     * 
     * @param aSchema
     *                 metadata for feature-collection A
     * @param aAttributeNames
     *                 non-spatial feature-collection-A attributes to transfer
     * @param aNewAttributeNames
     *                 corresponding names in the feature collection receiving the
     *                 attributes
     * @param bSchema
     *                 metadata for feature-collection B
     * @param bAttributeNames
     *                 non-spatial feature-collection-B attributes to transfer
     * @param bNewAttributeNames
     *                 corresponding names in the feature collection receiving the
     *                 attributes
     */
    public FUTURE_AttributeMapping(List aAttributeNames,
            List aNewAttributeNames, List bAttributeNames,
            List bNewAttributeNames) {
        init(aAttributeNames, aNewAttributeNames, bAttributeNames,
                bNewAttributeNames);
    }
    public static List nonSpatialAttributeNames(FeatureSchema schema) {
        ArrayList attributeNames = new ArrayList();
        for (int i = 0; i < schema.getAttributeCount(); i++) {
            if (schema.getAttributeType(i) == AttributeType.GEOMETRY) {
                continue;
            }
            attributeNames.add(schema.getAttributeName(i));
        }
        return attributeNames;
    }
    private void init(List aAttributeNames, List aNewAttributeNames,
            List bAttributeNames, List bNewAttributeNames) {
        Assert.isTrue(aAttributeNames.size() == aNewAttributeNames.size());
        Assert.isTrue(bAttributeNames.size() == bNewAttributeNames.size());
        this.aAttributeNames = new ArrayList(aAttributeNames);
        this.bAttributeNames = new ArrayList(bAttributeNames);
        this.aNewAttributeNames = new ArrayList(aNewAttributeNames);
        this.bNewAttributeNames = new ArrayList(bNewAttributeNames);
    }
    /**
     * Returns a new FeatureSchema with the destination attributes of the
     * mapping and a spatial attribute with the given name
     * 
     * @param geometryName
     *                 name to assign to the spatial attribute
     */
    public CombinedSchema createSchema(FeatureSchema aSchema,
            FeatureSchema bSchema, String geometryName) {
        CombinedSchema newSchema = new CombinedSchema();
        addAttributes(newSchema, aSchema, aAttributeNames, aNewAttributeNames,
                newSchema.aNewToOldAttributeIndexMap);
        newSchema.lastNewAttributeIndexForA = newSchema.getAttributeCount() - 1;
        addAttributes(newSchema, bSchema, bAttributeNames, bNewAttributeNames,
                newSchema.bNewToOldAttributeIndexMap);
        newSchema.addAttribute(geometryName, AttributeType.GEOMETRY);
        return newSchema;
    }
    public static class CombinedSchema extends FeatureSchema {
        private Map aNewToOldAttributeIndexMap = new HashMap();
        private Map bNewToOldAttributeIndexMap = new HashMap();
        public int toAOldAttributeIndex(int newAttributeIndex) {
            return ((Integer) aNewToOldAttributeIndexMap.get(new Integer(
                    newAttributeIndex))).intValue();
        }
        public int toBOldAttributeIndex(int newAttributeIndex) {
            return ((Integer) bNewToOldAttributeIndexMap.get(new Integer(
                    newAttributeIndex))).intValue();
        }
        private int lastNewAttributeIndexForA;
        public boolean isFromA(int newAttributeIndex) {
            return newAttributeIndex <= lastNewAttributeIndexForA;
        };
    }
    private void addAttributes(FeatureSchema newSchema,
            FeatureSchema sourceSchema, List attributeNames,
            List newAttributeNames, Map newToOldAttributeIndexMap) {
        for (int i = 0; i < attributeNames.size(); i++) {
            String attributeName = (String) attributeNames.get(i);
            String newAttributeName = (String) newAttributeNames.get(i);
            AttributeType type = sourceSchema.getAttributeType(attributeName);
            if (type == AttributeType.GEOMETRY) {
                continue;
            }
            if (newSchema.hasAttribute(newAttributeName)) {
                //Get here for 2:1 mappings [Jon Aquino 2004-04-28]
                continue;
            }
            newSchema.addAttribute(newAttributeName, type);
            newToOldAttributeIndexMap.put(new Integer(newSchema
                    .getAttributeCount() - 1), new Integer(i));
        }
    }
    protected boolean isDisjoint(Collection a, Collection b) {
        HashSet c = new HashSet();
        c.addAll(a);
        c.addAll(b);
        return c.size() == (a.size() + b.size());
    }
    /**
     * Transfers attributes (not the geometry) from two features to a third
     * feature, using the mappings specified in the constructor. The third
     * feature's schema must be able to accomodate the attributes being
     * transferred.
     * 
     * @param aFeature
     *                 a feature from feature-collection A, or null
     * @param bFeature
     *                 a feature from feature-collection B, or null
     * @param cFeature
     *                 the feature to transfer the A and B attributes to
     */
    public void transferAttributes(Feature aFeature, Feature bFeature,
            Feature cFeature) {
        if (aFeature != null) {
            transferAttributes(aFeature, cFeature, aAttributeNames,
                    aNewAttributeNames);
        }
        if (bFeature != null) {
            transferAttributes(bFeature, cFeature, bAttributeNames,
                    bNewAttributeNames);
        }
    }
    private void transferAttributes(Feature source, Feature dest,
            List attributeNames, List newAttributeNames) {
        for (int i = 0; i < attributeNames.size(); i++) {
            String attributeName = (String) attributeNames.get(i);
            String newAttributeName = (String) newAttributeNames.get(i);
            Assert
                    .isTrue(source.getSchema().getAttributeType(attributeName) != AttributeType.GEOMETRY);
            dest.setAttribute(newAttributeName, source
                    .getAttribute(attributeName));
        }
    }
    public static List ensureUnique(List names, Collection takenNames) {
        final ArrayList myTakenNames = new ArrayList(takenNames);
        return (List) CollectionUtil.collect(names, new Block() {
            public Object yield(Object name) {
                myTakenNames.add(ensureUnique((String) name, myTakenNames));
                return myTakenNames.get(myTakenNames.size() - 1);
            }
        });
    }
    public static String ensureUnique(String name, Collection takenNames) {
        for (int i = 0; true; i++) {
            String candidate = i == 0 ? name : name + "_" + i;
            if (!FUTURE_StringUtil.toUpperCase(takenNames).contains(
                    candidate.toUpperCase())) {
                return candidate;
            }
        }
    }
}