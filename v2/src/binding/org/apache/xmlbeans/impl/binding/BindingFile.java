/**
 * XBeans implementation.
 * Author: David Bau
 * Date: Oct 1, 2003
 */
package org.apache.xmlbeans.impl.binding;

import org.apache.xmlbeans.XmlOptions;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.io.IOException;

public class BindingFile extends BindingLoader
{
    private Map bindingTypes = new LinkedHashMap();    // name-pair -> BindingType
    private Map xmlFromJava = new LinkedHashMap();     // javaName -> xmlName
    private Map javaFromXmlPojo = new LinkedHashMap(); // xmlName -> javaName (pojo)
    private Map javaFromXmlObj = new LinkedHashMap();  // xmlName -> javaName (xmlobj)
    
    /**
     * This constructor is used when making a new one out of the blue.
     */ 
    public BindingFile()
    {
        // nothing to do - all maps are empty
    }
    
    /**
     * Loader
     */ 
    public static BindingFile forDoc(org.apache.xmlbeans.x2003.x09.bindingConfig.BindingConfigDocument doc)
    {
        return new BindingFile(doc);
    }
    
    /**
     * This constructor loads an instance from an XML file
     */ 
    protected BindingFile(org.apache.xmlbeans.x2003.x09.bindingConfig.BindingConfigDocument doc)
    {
        List errors = new ArrayList();
        if (!doc.validate(new XmlOptions().setErrorListener(errors)))
            throw new IllegalArgumentException(errors.size() > 0 ? errors.get(0).toString() : "Invalid binding-config document");
        
        // todo: in the loops below, validate that entries are unique, or modify schema to do so.
        
        org.apache.xmlbeans.x2003.x09.bindingConfig.BindingType[] btNodes =
                doc.getBindingConfig().getBindings().getBindingTypeArray();
        for (int i = 0; i < btNodes.length; i++)
        {
            BindingType next = BindingType.loadFromBindingTypeNode(this, btNodes[i]);
            addBindingType(next, false, false);
        }
        org.apache.xmlbeans.x2003.x09.bindingConfig.Mapping[] mNodes =
                doc.getBindingConfig().getJavaToXml().getMappingArray();
        for (int i = 0; i < mNodes.length; i++)
        {
            JavaName jName = JavaName.forString(mNodes[i].getJavatype());
            XmlName xName = XmlName.forString(mNodes[i].getXmlcomponent());
            xmlFromJava.put(jName, xName);
        }
        
        mNodes = doc.getBindingConfig().getXmlToPojo().getMappingArray();
        for (int i = 0; i < mNodes.length; i++)
        {
            JavaName jName = JavaName.forString(mNodes[i].getJavatype());
            XmlName xName = XmlName.forString(mNodes[i].getXmlcomponent());
            javaFromXmlPojo.put(xName, jName);
        }

        mNodes = doc.getBindingConfig().getXmlToXmlobj().getMappingArray();
        for (int i = 0; i < mNodes.length; i++)
        {
            JavaName jName = JavaName.forString(mNodes[i].getJavatype());
            XmlName xName = XmlName.forString(mNodes[i].getXmlcomponent());
            javaFromXmlObj.put(xName, jName);
        }
    }
    
    /**
     * Writes out to XML
     */ 
    public org.apache.xmlbeans.x2003.x09.bindingConfig.BindingConfigDocument write() throws IOException
    {
        org.apache.xmlbeans.x2003.x09.bindingConfig.BindingConfigDocument doc =
                org.apache.xmlbeans.x2003.x09.bindingConfig.BindingConfigDocument.Factory.newInstance();
        write(doc);
        return doc;
    }
    
    /**
     * This function copies an instance into an empty doc.
     */ 
    private void write(org.apache.xmlbeans.x2003.x09.bindingConfig.BindingConfigDocument doc)
    {
        if (doc.getBindingConfig() != null)
            throw new IllegalArgumentException("Can only write into empty doc");
        org.apache.xmlbeans.x2003.x09.bindingConfig.BindingConfigDocument.BindingConfig bcNode = doc.addNewBindingConfig();
        
        // make tables
        org.apache.xmlbeans.x2003.x09.bindingConfig.BindingTable btabNode = bcNode.addNewBindings();
        org.apache.xmlbeans.x2003.x09.bindingConfig.MappingTable jtabNode = bcNode.addNewJavaToXml();
        org.apache.xmlbeans.x2003.x09.bindingConfig.MappingTable pojotabNode = bcNode.addNewXmlToPojo();
        org.apache.xmlbeans.x2003.x09.bindingConfig.MappingTable xotabNode = bcNode.addNewXmlToXmlobj();
        
        // fill em in: binding types (delegate to BindingType.write)
        for (Iterator i = bindingTypes.values().iterator(); i.hasNext(); )
        {
            BindingType bType = (BindingType)i.next();
            org.apache.xmlbeans.x2003.x09.bindingConfig.BindingType btNode = btabNode.addNewBindingType();
            bType.write(btNode);
        }

        // from-java mappings
        for (Iterator i = xmlFromJava.entrySet().iterator(); i.hasNext(); )
        {
            Map.Entry entry = (Map.Entry)i.next();
            JavaName jName = (JavaName)entry.getKey();
            XmlName xName = (XmlName)entry.getValue();
            org.apache.xmlbeans.x2003.x09.bindingConfig.Mapping mNode = jtabNode.addNewMapping();
            mNode.setJavatype(jName.toString());
            mNode.setXmlcomponent(xName.toString());
        }
            
        // to-pojo
        for (Iterator i = javaFromXmlPojo.entrySet().iterator(); i.hasNext(); )
        {
            Map.Entry entry = (Map.Entry)i.next();
            JavaName jName = (JavaName)entry.getValue();
            XmlName xName = (XmlName)entry.getKey();
            org.apache.xmlbeans.x2003.x09.bindingConfig.Mapping mNode = pojotabNode.addNewMapping();
            mNode.setJavatype(jName.toString());
            mNode.setXmlcomponent(xName.toString());
        }

        // to-xmlobj
        for (Iterator i = javaFromXmlObj.entrySet().iterator(); i.hasNext(); )
        {
            Map.Entry entry = (Map.Entry)i.next();
            JavaName jName = (JavaName)entry.getValue();
            XmlName xName = (XmlName)entry.getKey();
            org.apache.xmlbeans.x2003.x09.bindingConfig.Mapping mNode = xotabNode.addNewMapping();
            mNode.setJavatype(jName.toString());
            mNode.setXmlcomponent(xName.toString());
        }
    }


    public void addBindingType(BindingType bType, boolean fromJavaDefault, boolean fromXmlDefault)
    {
        bindingTypes.put(pair(bType.getJavaName(), bType.getXmlName()), bType);
        if (fromJavaDefault)
        {
            if (bType.isXmlObject())
                javaFromXmlObj.put(bType.getXmlName(), bType.getJavaName());
            else
                javaFromXmlPojo.put(bType.getXmlName(), bType.getJavaName());
        }
        if (fromXmlDefault)
        {
            xmlFromJava.put(bType.getJavaName(), bType.getXmlName());
        }
    }
    
    public BindingType getBindingType(JavaName jName, XmlName xName)
    {
        return (BindingType)bindingTypes.get(pair(jName, xName));
    }
    
    public BindingType getBindingTypeForXmlPojo(XmlName xName)
    {
        JavaName jName = (JavaName)javaFromXmlPojo.get(xName);
        if (jName == null)
            return null;
        
        return (BindingType)bindingTypes.get(pair(jName, xName));
    }

    public BindingType getBindingTypeForXmlObj(XmlName xName)
    {
        JavaName jName = (JavaName)javaFromXmlObj.get(xName);
        if (jName == null)
            return null;
        
        return (BindingType)bindingTypes.get(pair(jName, xName));
    }
    
    public BindingType getBindingTypeForJava(JavaName jName)
    {
        XmlName xName = (XmlName)xmlFromJava.get(jName);
        if (xName == null)
            return null;
        
        return (BindingType)bindingTypes.get(pair(jName, xName));
    }
    
    
    protected static NamePair pair(JavaName jName, XmlName xName)
    {
        return new NamePair(jName, xName);
    }

    private static class NamePair
    {
        private final JavaName jName;
        private final XmlName xName;

        NamePair(JavaName jName, XmlName xName)
        {
            this.jName = jName;
            this.xName = xName;
        }

        public JavaName getJavaName()
        {
            return jName;
        }

        public XmlName getXmlName()
        {
            return xName;
        }

        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (!(o instanceof BindingFile.NamePair)) return false;

            final BindingFile.NamePair namePair = (BindingFile.NamePair) o;

            if (!jName.equals(namePair.jName)) return false;
            if (!xName.equals(namePair.xName)) return false;

            return true;
        }

        public int hashCode()
        {
            int result;
            result = jName.hashCode();
            result = 29 * result + xName.hashCode();
            return result;
        }
    }
    
}
