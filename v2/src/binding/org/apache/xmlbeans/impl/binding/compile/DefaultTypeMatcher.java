/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xmlbeans.impl.binding.compile;

import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JProperty;
import org.apache.xmlbeans.impl.common.NameUtil;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaTypeSystem;

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
public class DefaultTypeMatcher implements TypeMatcher
{
    private TypeMatcherContext mContext;

    public void init(TypeMatcherContext ctx) {
      mContext = ctx;
    }

    public MatchedType[] matchTypes(JClass[] jClasses, SchemaTypeSystem sts) {
        SchemaType[] types = sts.globalTypes();
        
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
        verbose("JavaNameMatcher.put " + key);
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
        verbose("JavaNameMatcher.getLocalPart " + localName);

        result = mapByShortName.get(localName);
        if (result != null) {
            verbose("javaTypeByShortName.get(localName): "+ localName);
            return result;
        }

        String lcLocalName = localName.toLowerCase();
        verbose("JavaNameMatcher.lcLocalName " + lcLocalName);
        result = mapByLowercasedShortName.get(lcLocalName);
        if (result != null) {
            verbose("javaTypeByLowercasedShortName.get(lcLocalName): " + lcLocalName);
            return result;
        }

        String niceName = NameUtil.upperCamelCase(localName);
        verbose("JavaNameMatcher.jaxbName " + niceName);
        result = mapByShortName.get(niceName);
        if (result != null) {
            verbose("javaTypeByShortName.get(jaxbName): " + niceName);
            return result;
        }

        String lowercaseNiceName = niceName.toLowerCase();
        verbose("JavaNameMatcher.lcJaxbName " + lowercaseNiceName);
        result = mapByLowercasedShortName.get(lowercaseNiceName);
        if (result != null) {
            verbose("javaTypeByShortName.get(lcJaxbName): " + lowercaseNiceName);
            return result;
        }

        verbose("javaTypeByShortName.get() no match found: " + localName);

        return null;
    }

    private void verbose(String w) {
      mContext.getLogger().logVerbose(w);
    }
}