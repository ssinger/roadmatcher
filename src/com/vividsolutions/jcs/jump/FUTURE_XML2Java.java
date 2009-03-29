package com.vividsolutions.jcs.jump;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.util.java2xml.XML2Java;
import com.vividsolutions.jump.util.java2xml.XMLBinder;

public class FUTURE_XML2Java extends XML2Java {
	private interface Block {
		public void yield() throws Exception;
	}

	private ArrayList listeners = new ArrayList();

	private ClassLoader classLoader;

	public FUTURE_XML2Java() {
		this(FUTURE_XML2Java.class.getClassLoader());
	}

	public FUTURE_XML2Java(final ClassLoader classLoader) {
		this.classLoader = classLoader;
		addFileCustomConverter(this);
		addCustomConverter(Class.class, new CustomConverter() {
			public Object toJava(String value) {
				try {
					return Class.forName(value, true, classLoader);
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			}

			public String toXML(Object object) {
				return ((Class) object).getName();
			}
		});
	}

	public Object read(String xml, Class c) throws Exception {
		StringReader reader = new StringReader(xml);
		try {
			return read(reader, c);
		} finally {
			reader.close();
		}
	}

	public Object read(Reader reader, Class c) throws Exception {
		return read(new SAXBuilder().build(reader).getRootElement(), c);
	}

	public Object read(File file, Class c) throws Exception {
		FileReader fileReader = new FileReader(file);
		try {
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			try {
				return new FUTURE_XML2Java(c.getClassLoader()).read(
						bufferedReader, c);
			} finally {
				bufferedReader.close();
			}
		} finally {
			fileReader.close();
		}
	}

	private void read(final Element tag, final Object object, List specElements)
			throws Exception {
		Assert.isTrue(tag != null);
		visit(specElements, new SpecVisitor() {
			private void fillerTagSpecFound(String xmlName,
					List specChildElements) throws Exception {
				if (tag.getChildren(xmlName).size() != 1) {
					throw new XMLBinderException("Expected 1 <" + xmlName
							+ "> tag but found "
							+ tag.getChildren(xmlName).size());
				}
				read(tag.getChild(xmlName), object, specChildElements);
			}

			private void normalTagSpecFound(String xmlName, String javaName,
					List specChildElements) throws Exception {
				setValuesFromTags(object, setter(object.getClass(), javaName),
						tag.getChildren(xmlName));
				//The parent may specify additional tags for itself in the
				// children. [Jon Aquino]
				for (Iterator i = tag.getChildren(xmlName).iterator(); i
						.hasNext();) {
					Element childTag = (Element) i.next();
					read(childTag, object, specChildElements);
				}
			}

			public void tagSpecFound(final String xmlName,
					final String javaName, final List specChildElements)
					throws Exception {
				printIfException(xmlName, new Block() {
					public void yield() throws Exception {
						if (javaName == null) {
							fillerTagSpecFound(xmlName, specChildElements);
						} else {
							normalTagSpecFound(xmlName, javaName,
									specChildElements);
						}
					}
				});
			}

			private void printIfException(String xmlName, Block block)
					throws Exception {
				try {
					block.yield();
				} catch (Exception e) {
					System.out.println(xmlName);
					throw e;
				}
			}

			public void attributeSpecFound(final String xmlName,
					final String javaName) throws Exception {
				printIfException(xmlName, new Block() {
					public void yield() throws Exception {
						if (tag.getAttribute(xmlName) == null) {
							throw new XMLBinderException("Expected '"
									+ xmlName
									+ "' attribute but found none. Tag = "
									+ tag.getName()
									+ "; Attributes = "
									+ StringUtil.toCommaDelimitedString(tag
											.getAttributes()));
						}
						Method setter = setter(object.getClass(), javaName);
						setValue(object, setter, toJava(tag.getAttribute(
								xmlName).getValue(),
								setter.getParameterTypes()[0]));
					}
				});
			}
		}, object.getClass());
	}

	private Object read(Element tag, Class c) throws Exception {
		if (tag.getAttribute("null") != null
				&& tag.getAttributeValue("null").equals("true")) {
			return null;
		}
		if (specifyingTypeExplicitly(c)) {
			if (tag.getAttribute("class") == null) {
				throw new XMLBinderException("Expected <" + tag.getName()
						+ "> to have 'class' attribute but found none");
			}
			return read(tag, Class.forName(tag.getAttributeValue("class"),
					true, classLoader));
		}
		fireCreatingObject(c);
		if (hasCustomConverter(c)) {
			return toJava(tag.getTextTrim(), c);
		}
		Object object = c.newInstance();
		if (object instanceof Map) {
			for (Iterator i = tag.getChildren().iterator(); i.hasNext();) {
				Element mappingTag = (Element) i.next();
				if (!mappingTag.getName().equals("mapping")) {
					throw new XMLBinderException("Expected <" + tag.getName()
							+ "> to have <mapping> tag but found none");
				}
				if (mappingTag.getChildren().size() != 2) {
					throw new XMLBinderException("Expected <" + tag.getName()
							+ "> to have 2 tags under <mapping> but found "
							+ mappingTag.getChildren().size());
				}
				if (mappingTag.getChildren("key").size() != 1) {
					throw new XMLBinderException(
							"Expected <"
									+ tag.getName()
									+ "> to have 1 <key> tag under <mapping> but found "
									+ mappingTag.getChildren("key").size());
				}
				if (mappingTag.getChildren("value").size() != 1) {
					throw new XMLBinderException(
							"Expected <"
									+ tag.getName()
									+ "> to have 1 <value> tag under <mapping> but found "
									+ mappingTag.getChildren("key").size());
				}
				((Map) object).put(read(mappingTag.getChild("key"),
						Object.class), read(mappingTag.getChild("value"),
						Object.class));
			}
		} else if (object instanceof Collection) {
			for (Iterator i = tag.getChildren().iterator(); i.hasNext();) {
				Element itemTag = (Element) i.next();
				if (!itemTag.getName().equals("item")) {
					throw new XMLBinderException("Expected <" + tag.getName()
							+ "> to have <item> tag but found none");
				}
				((Collection) object).add(read(itemTag, Object.class));
			}
		} else {
			read(tag, object, specElements(object.getClass()));
		}
		return object;
	}

	private void fireCreatingObject(Class c) {
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			Listener l = (Listener) i.next();
			l.creatingObject(c);
		}
	}

	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	private void setValuesFromTags(Object object, Method setter, Collection tags)
			throws Exception {
		for (Iterator i = tags.iterator(); i.hasNext();) {
			Element tag = (Element) i.next();
			setValueFromTag(object, setter, tag);
		}
	}

	private void setValueFromTag(Object object, Method setter, Element tag)
			throws Exception {
		setValue(object, setter, read(tag, fieldClass(setter)));
	}

	private void setValue(Object object, Method setter, Object value)
			throws IllegalAccessException, InvocationTargetException {
		//If you get an InvocationTargetException, check the bottom of the
		// stack
		//trace -- you should see the stack trace for the underlying exception.
		//[Jon Aquino]
		setter.invoke(object, new Object[] { value });
	}

	public static interface Listener {
		public void creatingObject(Class c);
	}

	public static void addFileCustomConverter(XMLBinder xmlBinder) {
		xmlBinder.addCustomConverter(File.class, new CustomConverter() {
			public Object toJava(String value) {
				return new File(value);
			}

			public String toXML(Object object) {
				return ((File) object).getPath();
			}
		});
	}
}