/**
 * XBeans implementation.
 * Author: David Bau
 * Date: Oct 6, 2003
 */
package org.apache.xmlbeans.impl.binding;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.LinkedHashMap;

public class BuiltinBindingLoader extends BindingLoader
{
    private Map bindingTypes = new LinkedHashMap();    // name-pair -> BindingType
    private Map xmlFromJava = new LinkedHashMap();     // javaName -> xmlName
    private Map javaFromXmlPojo = new LinkedHashMap(); // xmlName -> javaName (pojo)
    private Map javaFromXmlObj = new LinkedHashMap();  // xmlName -> javaName (xmlobj)
    
    private static final String xsns = "http://www.w3.org/2001/XMLSchema";
    
    private void addMapping(String xmlType, String javaName, boolean pojo, boolean defaultForJava, boolean defaultForXml)
    {
        XmlName xn = XmlName.forTypeNamed(new QName(xsns, xmlType));
        JavaName jn = JavaName.forString(javaName);
        BindingType bt = new BuiltinBindingType(jn, xn, !pojo);
        bindingTypes.put(new NamePair(jn, xn), bt);
        if (defaultForJava)
            xmlFromJava.put(jn, xn);
        if (defaultForXml)
        {
            if (pojo)
                javaFromXmlPojo.put(xn, jn);
            else
                javaFromXmlObj.put(xn, jn);
        }
    }
    
    private void addPojoTwoWay(String xmlType, String javaName)
    {
        addMapping(xmlType, javaName, true, true, true);
    }

    private void addPojoXml(String xmlType, String javaName)
    {
        addMapping(xmlType, javaName, true, false, true);
    }
    
    private void addPojoJava(String xmlType, String javaName)
    {
        addMapping(xmlType, javaName, true, true, false);
    }
    
    private void addPojo(String xmlType, String javaName)
    {
        addMapping(xmlType, javaName, true, true, false);
    }

    public BuiltinBindingLoader()
    {
        // todo: should each builtin binding type know about it's print/parse methods?
        
        addPojoXml("anySimpleType", "java.lang.String");
        
        addPojoTwoWay("string", "java.lang.String");
        addPojoXml("normalizedString", "java.lang.String");
        addPojoXml("token", "java.lang.String");
        addPojoXml("language", "java.lang.String");
        addPojoXml("Name", "java.lang.String");
        addPojoXml("NCName", "java.lang.String");
        addPojoXml("NMTOKEN", "java.lang.String");
        addPojoXml("ID", "java.lang.String");
        addPojoXml("IDREF", "java.lang.String");
        addPojoXml("ENTITY", "java.lang.String");
        
        addPojoTwoWay("duration", "org.apache.xmlbeans.GDuration");
        
        addPojoTwoWay("dateTime", "java.util.Calendar");
        addPojoJava("dateTime", "java.util.Date");
        addPojoXml("time", "java.util.Calendar");
        addPojoXml("date", "java.util.Calendar");
        addPojo("date", "java.util.Date");
        addPojoXml("gYearMonth", "java.util.Calendar");
        addPojoXml("gYear", "java.util.Calendar");
        addPojo("gYear", "int");
        addPojoXml("gMonthDay", "java.util.Calendar");
        addPojoXml("gMonth", "java.util.Calendar");
        addPojo("gMonth", "int");
        addPojoXml("gDay", "java.util.Calendar");
        addPojo("gDay", "int");
        
        addPojoTwoWay("boolean", "boolean");
        addPojoTwoWay("base64Binary", "byte[]");
        addPojoJava("base64Binary", "java.io.InputStream");
        addPojoXml("hexBinary", "byte[]");
        addPojo("hexBinary", "java.io.InputStream");
        addPojoTwoWay("float", "float");
        addPojoTwoWay("double", "double");
        addPojoTwoWay("decimal", "java.math.BigDecimal");
        addPojoTwoWay("integer", "java.math.BigInteger");
        addPojoTwoWay("long", "long");
        addPojoTwoWay("int", "int");
        addPojoTwoWay("short", "short");
        addPojoTwoWay("byte", "byte");
        addPojoXml("nonPositiveInteger", "java.math.BigInteger");
        addPojoXml("negativeInteger", "java.math.BigInteger");
        addPojoXml("nonNegativeInteger", "java.math.BigInteger");
        addPojoXml("positiveInteger", "java.math.BigInteger");
        addPojoXml("unsignedLong", "java.math.BigInteger");
        addPojoXml("unsignedInt", "long");
        addPojoXml("unsignedShort", "int");
        addPojoXml("unsignedByte", "short");
        addPojoXml("anyURI", "java.lang.String");
        addPojoJava("anyURI", "java.net.URI");
        addPojoTwoWay("QName", "javax.xml.namespace.QName");
        addPojoXml("NOTATION", "java.lang.String");
    }

    public BindingType getBindingType(JavaName jName, XmlName xName)
    {
        return null;
    }

    public BindingType getBindingTypeForXmlPojo(XmlName xName)
    {
        return null;
    }

    public BindingType getBindingTypeForXmlObj(XmlName xName)
    {
        return null;
    }

    public BindingType getBindingTypeForJava(JavaName jName)
    {
        return null;
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
            if (!(o instanceof BuiltinBindingLoader.NamePair)) return false;

            final BuiltinBindingLoader.NamePair namePair = (BuiltinBindingLoader.NamePair) o;

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
