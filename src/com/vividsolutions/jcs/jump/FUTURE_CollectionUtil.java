package com.vividsolutions.jcs.jump;

import java.awt.Window;
import java.util.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jump.util.Block;

public class FUTURE_CollectionUtil {
    public static Collection concatenate(Collection a, Collection b) {
        ArrayList result = new ArrayList();
        result.addAll(a);
        result.addAll(b);
        return result;
    }
    
    public static Collection add(Object item, Collection collection) {
        collection.add(item);
        return collection;
    }
    
    public static Collection remove(Object item, Collection collection) {
        collection.remove(item);
        return collection;
    }    
    
    public static Iterator concatenate(final Iterator a, final Iterator b) {
        return new Iterator() {
            private Iterator currentIterator = a;
            public void remove() {
                //Tricky. Leave unimplemented for now. [Jon Aquino 12/8/2003]
                throw new UnsupportedOperationException();
            }
            private Iterator getCurrentIterator() {
                if (currentIterator == a && !a.hasNext()) {
                    currentIterator = b;
                }
                return currentIterator;
            }
            public boolean hasNext() {
                return getCurrentIterator().hasNext();
            }
            public Object next() {
                return getCurrentIterator().next();
            }
        };
    }

    public static Object firstOrNull(Collection collection) {
        return !collection.isEmpty() ? collection.iterator().next() : null; 
    }    

    public static List list(Object a, Object b) {
        ArrayList doubleton = new ArrayList();
        doubleton.add(a);
        doubleton.add(b);
        return doubleton;
    }    
    
    /**
     * The Smalltalk #inject:into: method
     */
    public static Object injectInto(Collection collection, Object initialValue, Block binaryBlock) {
        Object currentValue = initialValue;
        for (Iterator i = collection.iterator(); i.hasNext(); ) {
            Object item = i.next();
            binaryBlock.yield(currentValue, item);
        }
        return currentValue;
    }
    
    /**
     * The Ruby #each method
     */
    public static void each(Collection collection, Block block) {
        for (Iterator i = collection.iterator(); i.hasNext(); ) {
            Object item = i.next();
            block.yield(item);
        }
    }
    
    public static HashSet createHashSet(Object[] array) {
        HashSet hashSet = new HashSet();
        for (int i = 0; i < array.length; i++) {
            hashSet.add(array[i]);
        }
        return hashSet;
    }
    
    public static Collection removeAll(Collection a, Collection b) {
        a.removeAll(b);
        return a;
    }
    
    public static Collection addAll(Collection a, Collection b) {
        a.addAll(b);
        return a;
    }    

    public static Collection addAll(Collection a, Object[] b) {
        for (int i = 0; i < b.length; i++) {
            a.add(b[i]);
        }
        return a;
    }

    public static boolean containsAny(Collection a, Collection b) {
        for (Iterator i = b.iterator(); i.hasNext(); ) {
            Object bItem = i.next();
            if (a.contains(bItem)) { return true; }
        }
        return false;
    }

}
