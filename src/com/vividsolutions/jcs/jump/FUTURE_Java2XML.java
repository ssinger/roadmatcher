package com.vividsolutions.jcs.jump;
import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.LangUtil;
import com.vividsolutions.jump.util.java2xml.Java2XML;
import com.vividsolutions.jump.util.java2xml.XMLBinder;
/**
 * Fixes #specResourceStream.
 */
public class FUTURE_Java2XML extends Java2XML {
    protected List specElements(Class c) throws XMLBinderException,
            JDOMException, IOException {
        InputStream stream = specResourceStream(c);
        if (stream == null) {
            throw new XMLBinderException("Could not find java2xml file for "
                    + c.getName() + " or its interfaces or superclasses");
        }
        try {
            Element root = new SAXBuilder().build(stream).getRootElement();
            if (!root.getAttributes().isEmpty()) {
                throw new XMLBinderException("Root element of "
                        + _specFilename(c) + " should not have attributes");
            }
            if (!root.getName().equals("root")) {
                throw new XMLBinderException("Root element of "
                        + _specFilename(c) + " should be named 'root'");
            }
            return root.getChildren();
        } finally {
            stream.close();
        }
    }
    public boolean _hasCustomConverter(Class fieldClass) {
        return ((Boolean) FUTURE_LangUtil.invokePrivateMethod(
                "hasCustomConverter", this, XMLBinder.class,
                new Object[]{fieldClass}, new Class[]{Class.class}))
                .booleanValue();
    }
    private InputStream specResourceStream(Class c) {
        for (Iterator i = LangUtil.classesAndInterfaces(c).iterator(); i
                .hasNext();) {
            Class type = (Class) i.next();
            Assert.isTrue(type.isAssignableFrom(c));
            InputStream stream = type.getResourceAsStream(_specFilename(type));
            if (stream != null) {
                return stream;
            }
        }
        return null;
    }
    private String _specFilename(Class c) {
        return (String) FUTURE_LangUtil.invokePrivateMethod("specFilename",
                this, XMLBinder.class, new Object[]{c},
                new Class[]{Class.class});
    }
    public void write(Object object, String rootTagName, File file)
            throws Exception {
        FileWriter fileWriter = new FileWriter(file, false);
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            try {
                new FUTURE_Java2XML()
                        .write(object, rootTagName, bufferedWriter);
                bufferedWriter.flush();
                fileWriter.flush();
            } finally {
                bufferedWriter.close();
            }
        } finally {
            fileWriter.close();
        }
    }
    public void write(Object object, String rootTagName, Writer writer)
            throws Exception {
        super
                .write(
                        object instanceof Collection ? new FUTURE_Java2XMLCollectionWrapper(
                                (Collection) object)
                                : object, rootTagName, writer);
    }
    public FUTURE_Java2XML() {
        FUTURE_XML2Java.addFileCustomConverter(this);
    }
}