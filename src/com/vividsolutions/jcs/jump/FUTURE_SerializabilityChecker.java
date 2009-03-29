package com.vividsolutions.jcs.jump;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import com.vividsolutions.jump.util.LangUtil;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.ui.GUIUtil;

/**
 * Given an object to be serialized, produces a report on objects that need to
 * be marked with the Serializable interface.
 */
public class FUTURE_SerializabilityChecker {

    private String description(Object object, String name) {
        return StringUtil.classNameWithoutPackageQualifiers(object.getClass()
                .getName())
                + " " + name;
    }

    private List fields(Class c) {
        return c == null ? new ArrayList() : (List) FUTURE_CollectionUtil
                .concatenate(Arrays.asList(c.getDeclaredFields()), fields(c
                        .getSuperclass()));
    }

    /**
     * @param maxObjects -1
     *                    for no restriction
     * @param maxStackDepth -1
     *                    for no restriction
     */
    public String report(Object object, int maxObjects, int maxStackDepth) {
        this.maxObjects = maxObjects;
        this.maxStackDepth = maxStackDepth;
        try {
            report(object, "root", new Stack(), new Stack());
        } catch (Exception e) {
            stringBuffer.append(StringUtil.stackTrace(e));
        }
        if (maxObjectsExceeded) {
            stringBuffer.append("Warning: Max objects exceeded (" + maxObjects
                    + ")\n");
        }
        if (maxStackDepthExceededEvents > 0) {
            stringBuffer.append("Warning: Max stack depth exceeded "
                    + maxStackDepthExceededEvents + " times\n");
        }
        stringBuffer.append("Done\n");
        return stringBuffer.toString();
    }

    private boolean maxObjectsExceeded = false;

    private int maxStackDepthExceededEvents = 0;
    
    private void report(Object object, String name, Stack objectStack,
            Stack nameStack) throws IllegalArgumentException,
            IllegalAccessException {
    	if (object == null) { return; }
        if (objects > maxObjects && maxObjects > 0) {
            maxObjectsExceeded = true;
            return;
        }
        if (objectStack.size() > maxStackDepth && maxStackDepth > 0) {
            maxStackDepthExceededEvents++;
            return;
        }
        if (objectsEncountered.contains(object)) { return; }
        objects++;
        objectsEncountered.add(object);
        objectStack.push(object);
        nameStack.push(name);
        try {
            if (verbose) {
                stringBuffer.append(StringUtil.repeat(' ', objectStack.size())
                        + description(objectStack.peek(), (String) nameStack
                                .peek()) + "\n");
            }
            if (object instanceof Object[]) {
                report((Object[]) object, name, objectStack, nameStack);
                return;
            }
            if (object instanceof Collection) {
                report(((Collection) object).toArray(), name, objectStack,
                        nameStack);
                return;
            }
            if (object instanceof Map) {
                report(((Map) object).keySet().toArray(), name, objectStack,
                        nameStack);
                report(((Map) object).values().toArray(), name, objectStack,
                        nameStack);
                return;
            }
            checkMarkedSerializable(object, objectStack, nameStack);
            List fields = fields(object.getClass());
            for (Iterator i = fields.iterator(); i.hasNext(); ) {
                Field field = (Field) i.next();
                if (LangUtil.isPrimitive(field.getType())) {
                    continue;
                }
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                if (Modifier.isTransient(field.getModifiers())) {
                    continue;
                }
                if (ignoringFieldsOf(field.getType())) {
                    continue;
                }
                field.setAccessible(true);
                if (field.get(object) == null) {
                    continue;
                }
                report(field.get(object), field.getName(), objectStack,
                        nameStack);
            }
        } finally {
            nameStack.pop();
            objectStack.pop();
        }
    }

    private boolean ignoringFieldsOf(Class c) {
        return c.getName().startsWith("java.") || c.getName().startsWith("javax.");
    }

    private void checkMarkedSerializable(Object object, Stack objectStack,
            Stack nameStack) {
        for (Iterator i = classAndSuperclasses(object.getClass()).iterator(); i
                .hasNext(); ) {
            Class type = (Class) i.next();
            if (!Serializable.class.isAssignableFrom(type)) {
                if (!badTypes.contains(type)) {
                    stringBuffer.append("Not marked Serializable");
                    if (type != object.getClass()) {
                        stringBuffer.append(" ("
                                + StringUtil
                                        .classNameWithoutPackageQualifiers(type
                                                .getName()) + ")");
                    }
                    stringBuffer.append(": " + string(objectStack, nameStack)
                            + "\n");
                    badTypes.add(type);
                }
            }
        }
    }

    private Collection classAndSuperclasses(Class c) {
        if (c == Object.class) { return new ArrayList(); }
        Collection classAndSuperclasses = classAndSuperclasses(c
                .getSuperclass());
        classAndSuperclasses.add(c);
        return classAndSuperclasses;
    }

    private void report(Object[] array, String name, Stack objectStack,
            Stack nameStack) throws IllegalArgumentException,
            IllegalAccessException {
        for (int i = 0; i < array.length; i++) {
            report(array[i], name + "[" + i + "]", objectStack, nameStack);
        }
    }

    public String reportHTML(Object object, int maxObjects, int maxStackDepth) {
        return "<UL>"
                + StringUtil.replaceAll(GUIUtil.escapeHTML("\n"
                        + report(object, maxObjects, maxStackDepth).trim(),
                        true, true), "<BR>", "<LI>") + "</UL>";
    }

    private String string(Stack objectStack, Stack nameStack) {
        StringBuffer b = new StringBuffer();
        for (int i = objectStack.size() - 1; i >= 0; i--) {
            if (i != objectStack.size() - 1) {
                b.append(", in ");
            }
            b
                    .append(description(objectStack.get(i), (String) nameStack
                            .get(i)));
        }
        return b.toString();
    }

    private int maxObjects;

    private int maxStackDepth;

    private int objects = 0;

    private StringBuffer stringBuffer = new StringBuffer();

    private boolean verbose = false;

    private HashSet badTypes = new HashSet();

    private HashSet objectsEncountered = new HashSet();
}
