package com.vividsolutions.jcs.jump;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.plugin.conflate.roads.ExportResultApp;
import com.vividsolutions.jcs.plugin.conflate.roads.OpenRoadMatcherSessionPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.ResultOptions;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.task.DummyTaskMonitor;

public class FUTURE_LangUtil {
	public static void main(String[] args) throws Exception {
		ConflationSession session = OpenRoadMatcherSessionPlugIn.open(new File(
				"c:/junk3/a.rms"), new DummyTaskMonitor());
		ResultOptions.get(session).setDataset0AttributesToInclude("ADDR_LEFT, ADDR_RIGHT");
		ResultOptions.get(session).setDataset1AttributesToInclude("CONST, ORIGIN");
		FUTURE_LangUtil.invokePrivateMethod("exportResult",
				new ExportResultApp(), ExportResultApp.class, new Object[] {
						session, new File("c:/junk3/a.zip") }, new Class[] {
						ConflationSession.class, File.class });
	}

	/**
	 * Warning: Seems like if the method is protected rather than private and it
	 * is overridden, the overriding method will be called. Thus, if you are
	 * using #invokePrivateMethod to access a private method in the superclass,
	 * you had best name your calling method something different from the method
	 * you are calling, lest the superclass method be made protected (or public)
	 * in the future. (Suggestion: prefix the name of your calling method with
	 * an underscore).
	 */
	public static Object invokePrivateMethod(String methodName, Object object,
			Class c, Object[] parameters, Class[] parameterTypes) {
		try {
			Method method = c.getDeclaredMethod(methodName, parameterTypes);
			method.setAccessible(true);
			return method.invoke(object, parameters);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public static Object getPrivateField(String fieldName, Object object,
			Class c) {
		try {
			Field field = c.getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(object);
		} catch (SecurityException e1) {
			Assert.shouldNeverReachHere();
		} catch (NoSuchFieldException e1) {
			Assert.shouldNeverReachHere();
		} catch (IllegalArgumentException e) {
			Assert.shouldNeverReachHere();
		} catch (IllegalAccessException e) {
			Assert.shouldNeverReachHere();
		}
		return null;
	}

	public static void setPrivateField(String fieldName, Object newValue,
			Object object, Class c) {
		try {
			Field field = c.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(object, newValue);
		} catch (SecurityException e1) {
			Assert.shouldNeverReachHere();
		} catch (NoSuchFieldException e1) {
			Assert.shouldNeverReachHere();
		} catch (IllegalArgumentException e) {
			Assert.shouldNeverReachHere();
		} catch (IllegalAccessException e) {
			Assert.shouldNeverReachHere();
		}
	}

	public static Object invokePrivateConstructor(Class c, Object[] parameters,
			Class[] parameterTypes) {
		try {
			Constructor constructor = c.getDeclaredConstructor(parameterTypes);
			constructor.setAccessible(true);
			return constructor.newInstance(parameters);
		} catch (IllegalArgumentException e1) {
			Assert.shouldNeverReachHere();
		} catch (InstantiationException e1) {
			Assert.shouldNeverReachHere();
		} catch (IllegalAccessException e1) {
			Assert.shouldNeverReachHere();
		} catch (InvocationTargetException e1) {
			Assert.shouldNeverReachHere();
		} catch (SecurityException e) {
			Assert.shouldNeverReachHere();
		} catch (NoSuchMethodException e) {
			Assert.shouldNeverReachHere();
		}
		return null;
	}
}