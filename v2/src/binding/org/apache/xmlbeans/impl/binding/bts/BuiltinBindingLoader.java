/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache
*    XMLBeans", nor may "Apache" appear in their name, without prior
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/
package org.apache.xmlbeans.impl.binding.bts;

import org.apache.xmlbeans.impl.binding.bts.BindingType;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * 
 */ 
public class BuiltinBindingLoader extends BaseBindingLoader
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
        NamePair pair = pair(jn, xn);
        bindingTypes.put(pair, bt);
        if (defaultForJava)
            xmlFromJava.put(jn, pair);
        if (defaultForXml)
        {
            if (pojo)
                javaFromXmlPojo.put(xn, pair);
            else
                javaFromXmlObj.put(xn, pair);
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

}
