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

package org.apache.xmlbeans.impl.binding.compile;

import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JProperty;
import org.apache.xmlbeans.impl.common.NameUtil;
import org.apache.xmlbeans.impl.binding.bts.BindingTypeName;
import org.apache.xmlbeans.impl.binding.bts.JavaTypeName;
import org.apache.xmlbeans.impl.binding.bts.XmlTypeName;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaProperty;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a simple implementation of a type matcher
 * that uses the names of the Java classes and Schema
 * types, and Java properties and Schema elements or
 * attributes, to line them up with each other. 
 */
public class SimpleTypeMatcher implements TypeMatcher
{
    public MatchedType[] matchTypes(BothSourceSet bss)
    {
        JClass[] jClasses = bss.getJClasses();
        SchemaType[] types = bss.getSchemaTypeSystem().globalTypes();
        
        List result = new ArrayList();
        
        // start the match
        startMatch();
        
        // add all the jClasses
        // todo: there is a need for some code which does
        // "primary interfaces" rather than "impl classes".  This
        // should be plugin behavior or controlled by annotations
        // rather than hard-coded.
        for (int i = 0; i < jClasses.length; i++) {
            putJavaName(jClasses[i].getSimpleName(), jClasses[i]);
        }
        
        // Then, go through all the Schema types with complex content
        // and try to find a matching Java class by looking for a similar
        // name.
        for (int i = 0; i < types.length; i++) {
            JClass jClass = (JClass)getSchemaName(types[i].getName());
            if (jClass != null)
                result.add(new MatchedType(jClass, types[i]));
        }
        
        return (MatchedType[])result.toArray(new MatchedType[result.size()]);
    }

    public TypeMatcher.MatchedProperties[] matchProperties(JClass jClass, SchemaType sType)
    {
        SchemaProperty[] sProps = sType.getProperties();
        JProperty[] jProps = jClass.getProperties();
        
        List result = new ArrayList();
        
        // start the match
        startMatch();
        
        // add all the jProperties
        for (int i = 0; i < jProps.length; i++) {
            putJavaName(jProps[i].getSimpleName(), jProps[i]);
        }
        
        // Then, go through all the Schema properties and try to find a
        // matching Java property by looking for a similar name.
        for (int i = 0; i < sProps.length; i++) {
            JProperty jProp = (JProperty)getSchemaName(sProps[i].getName());
            if (jProp != null)
                result.add(new MatchedProperties(jProp, sProps[i]));
        }
        
        return (MatchedProperties[])result.toArray(new MatchedProperties[result.size()]);
    }

    public JClass substituteClass(JClass declaredClass) {
        return declaredClass;
    }

    public void startMatch()
    {
        mapByShortName.clear();
        mapByLowercasedShortName.clear();
    }

    private final Map mapByShortName = new HashMap();
    private final Map mapByLowercasedShortName = new HashMap();

    /**
     * Returns false if the name has already been used.
     * Otherwise, indexes the given object by the given Java name.
     * 
     * Note that there is logic to be case-insensitive, yet to
     * remove case-insensitivity for names which differ from
     * each other only by case.
     */ 
    public boolean putJavaName(String key, Object value)
    {
        String shortName = key;
        // System.out.println("JavaNameMatcher.put " + key);
        if (mapByShortName.containsKey(shortName))
            return false;

        mapByShortName.put(shortName, value);

        String lcShortName = shortName.toLowerCase();
        if (mapByLowercasedShortName.containsKey(lcShortName))
            mapByLowercasedShortName.put(lcShortName, null);
        else
            mapByLowercasedShortName.put(lcShortName, value);

        return true;
    }

    /**
     * Attempts to find a Java name similar to the given QName.
     * If found, returns the indexed object; otherwise returns
     * null.
     * 
     * Note that this algorithm searches for the localName to
     * match exactly; then in a case-insensitive way; and then
     * with XML punctuation removed and camel-casing applied;
     * and then in a case-insensitive way again.
     */ 
    public Object getSchemaName(QName name)
    {
        Object result = null;
        String localName = name.getLocalPart();
        // System.out.println("JavaNameMatcher.getLocalPart " + localName);

        result = mapByShortName.get(localName);
        if (result != null) {
            // System.out.println("javaTypeByShortName.get(localName): "+ localName);
            return result;
        }

        String lcLocalName = localName.toLowerCase();
        // System.out.println("JavaNameMatcher.lcLocalName " + lcLocalName);
        result = mapByLowercasedShortName.get(lcLocalName);
        if (result != null) {
            // System.out.println("javaTypeByLowercasedShortName.get(lcLocalName): " + lcLocalName);
            return result;
        }

        String niceName = NameUtil.upperCamelCase(localName);
        // System.out.println("JavaNameMatcher.jaxbName " + jaxbName);
        result = mapByShortName.get(niceName);
        if (result != null) {
            // System.out.println("javaTypeByShortName.get(jaxbName): " + jaxbName);
            return result;
        }

        String lowercaseNiceName = niceName.toLowerCase();
        // System.out.println("JavaNameMatcher.lcJaxbName " + lcJaxbName);
        result = mapByLowercasedShortName.get(lowercaseNiceName);
        if (result != null) {
            // System.out.println("javaTypeByShortName.get(lcJaxbName): " + lcJaxbName);
            return result;
        }

        // System.out.println("javaTypeByShortName.get() no match found: " + localName);

        return null;
    }

}
