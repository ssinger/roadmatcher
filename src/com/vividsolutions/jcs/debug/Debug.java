/*
 * The JCS Conflation Suite (JCS) is a library of Java classes that
 * can be used to build automated or semi-automated conflation solutions.
 *
 * Copyright (C) 2003 Vivid Solutions
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */
package com.vividsolutions.jcs.debug;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

/**
 * Provides routines to simplify and localize debugging output.
 * <p>
 * To enable the debugging functions, the Java system property <code>debug</code>
 * must be set to <code>on</code> in the java command line, using the following
 * option:
 * <pre>
 *     -Ddebug=on
 * </pre>
 *
 * @version 1.4
 */
public class Debug {

  private static String DEBUG_PROPERTY_NAME = "debug";
  private static String DEBUG_PROPERTY_VALUE_ON = "on";
  private static String DEBUG_PROPERTY_VALUE_TRUE = "true";

  private static boolean debugOn = false;

  static {
    String debugValue = System.getProperty(DEBUG_PROPERTY_NAME);
    if (debugValue != null) {
      if (debugValue.equalsIgnoreCase(DEBUG_PROPERTY_VALUE_ON)
          || debugValue.equalsIgnoreCase(DEBUG_PROPERTY_VALUE_TRUE) )
        debugOn = true;
    }
  }

  public static void main(String[] args)
  {
    Debug.println("Debugging is ON");
  }

  private static Debug debug = new Debug();

  private static final String DEBUG_LINE_TAG = "D! ";

  private PrintStream out;
  private Class[] printArgs;
  private Object watchObj = null;
  private Object[] args = new Object[1];

  public static boolean isDebugging() { return debugOn; }

  public static void print(String str) {
    if (!debugOn) return;
    debug.instancePrint(str);
  }
/*
  public static void println(String str) {
    if (! debugOn) return;
    debug.instancePrint(str);
    debug.println();
  }
*/
  public static void print(Object obj) {
    if (! debugOn) return;
    debug.instancePrint(obj);
  }

  public static void print(boolean isTrue, Object obj) {
    if (! debugOn) return;
    if (! isTrue) return;
    debug.instancePrint(obj);
  }

  public static void println(Object obj) {
    if (!debugOn) {
      return;
    }
    debug.instancePrint(obj);
    debug.println();
  }

  public static void addWatch(Object obj) {
    debug.instanceAddWatch(obj);
  }

  public static void printWatch() {
    debug.instancePrintWatch();
  }

  public static void printIfWatch(Object obj) {
    debug.instancePrintIfWatch(obj);
  }

  private Debug() {
    out = System.out;
    printArgs = new Class[1];
    try {
      printArgs[0] = Class.forName("java.io.PrintStream");
    }
    catch (Exception ex) {
      // ignore this exception - it will fail later anyway
    }
  }


  public void instancePrintWatch() {
    if (watchObj == null) return;
    instancePrint(watchObj);
  }

  public void instancePrintIfWatch(Object obj) {
    if (obj != watchObj) return;
    if (watchObj == null) return;
    instancePrint(watchObj);
  }

  public void instancePrint(Object obj)
  {
    if (obj instanceof Collection) {
      instancePrint(((Collection) obj).iterator());
    }
    else if (obj instanceof Iterator) {
      instancePrint((Iterator) obj);
    }
    else {
      instancePrintObject(obj);
    }
  }

  public void instancePrint(Iterator it)
  {
    for (; it.hasNext(); ) {
      Object obj = it.next();
      instancePrintObject(obj);
    }
  }
  public void instancePrintObject(Object obj) {
    //if (true) throw new RuntimeException("DEBUG TRAP!");
    Method printMethod = null;
    try {
      Class cls = obj.getClass();
      try {
        printMethod = cls.getMethod("print", printArgs);
        args[0] = out;
        out.print(DEBUG_LINE_TAG);
        printMethod.invoke(obj, args);
      }
      catch (NoSuchMethodException ex) {
        instancePrint(obj.toString());
      }
    }
    catch (Exception ex) {
      ex.printStackTrace(out);
    }
  }

  public void println() {
    out.println();
  }

  private void instanceAddWatch(Object obj) {
    watchObj = obj;
  }

  private void instancePrint(String str) {
    out.print(DEBUG_LINE_TAG);
    out.print(str);
  }
}
