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
* originally based on software copyright (c) 2000-2003 BEA Systems 
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/

package org.apache.xmlbeans.impl.jaxb.compiler;

import java.util.*;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.SchemaComponent;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaLocalElement;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.impl.common.NameUtil;
import org.apache.xmlbeans.impl.common.XmlErrorWatcher;
import org.apache.xmlbeans.impl.schema.SchemaTypeCodePrinter;
import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;

public final class JaxbJavaizer
{
    public static boolean javaize(SchemaTypeSystem sts, Map componentData, Map packages, XmlErrorWatcher errors)
    {
        final List allComponents = new ArrayList();
        allComponents.addAll(Arrays.asList(sts.globalElements()));
        allComponents.addAll(Arrays.asList(sts.globalTypes()));

        assignGlobalNames(allComponents, componentData, packages);

        // Now fully javaize everything deeply

        for (int i = 0 , len = allComponents.size() ; i < len ; i++)
        {
            SchemaComponent sc = (SchemaComponent)allComponents.get(i);
            javaizeComponent(sc, componentData, packages);
            // allSeenTypes.addAll(Arrays.asList(gType.getAnonymousTypes()));
        }

        //TODO: check for any real errors:
        return true;
    }

    static void assignGlobalNames(List allComponents, Map componentData, Map packages)
    {
        Set usedNames = new HashSet();

        for (int i = 0 , len = allComponents.size() ; i < len ; i++)
        {
            SchemaComponent sc = (SchemaComponent)allComponents.get(i);
            QName name = sc.getName();

            ComponentInfo ci = getComponentInfo(sc, componentData);

            ci.setFullJavaIntfName(pickFullJavaIntfName(usedNames, name));
            ci.setFullJavaImplName(pickFullJavaImplName(usedNames, ci.getFullJavaIntfName()));

            // Construct a new PackageInfo object for each unique package name
            String pkgname = getPackageName(ci.getFullJavaIntfName());
            if (! packages.containsKey(pkgname))
            {
                PackageInfo pi = new PackageInfo(pkgname);
                packages.put(pkgname, pi);
                pi.setIndexClassName(SchemaTypeCodePrinter.indexClassForSystem(sc.getTypeSystem()));
            }
        }

    }

    static void javaizeComponent(SchemaComponent sc, Map componentData, Map packages)
    {
        switch(sc.getComponentType()) 
        {
            case SchemaComponent.ELEMENT:
                javaizeElement((SchemaGlobalElement)sc, componentData, packages);
                break;

            case SchemaComponent.TYPE:
                javaizeType((SchemaType)sc, componentData, packages);

            default:
                throw new IllegalStateException();
        }

    }

    static void setJavaImplClass(SchemaType sType, ComponentInfo ci) {
        SchemaType pType = sType.getPrimitiveType();
        boolean isBuiltin = sType.isBuiltinType();
        String baseclass = null;
        int argcount = isBuiltin ? 0 : 2;

        switch (pType.getBuiltinTypeCode())
        {
            case SchemaType.BTC_ANY_SIMPLE:
                baseclass =  "org.apache.xmlbeans.impl.values.XmlAnySimpleTypeImpl";
                if (pType != sType)
                    argcount = 2;
                break;
            case SchemaType.BTC_BOOLEAN:
                if (!isBuiltin)
                    baseclass =  "org.apache.xmlbeans.impl.values.JavaBooleanHolderEx";
                else
                    baseclass =  "org.apache.xmlbeans.impl.values.JavaBooleanHolder";
                break;
            case SchemaType.BTC_BASE_64_BINARY:
                if (!isBuiltin)
                    baseclass =  "org.apache.xmlbeans.impl.values.JavaBase64HolderEx";
                else
                    baseclass =  "org.apache.xmlbeans.impl.values.JavaBase64Holder";
                break;
            case SchemaType.BTC_HEX_BINARY:
                if (!isBuiltin)
                    baseclass =  "org.apache.xmlbeans.impl.values.JavaHexBinaryHolderEx";
                else
                    baseclass =  "org.apache.xmlbeans.impl.values.JavaHexBinaryHolder";
                break;
            case SchemaType.BTC_ANY_URI:
                if (!isBuiltin)
                    baseclass =  "org.apache.xmlbeans.impl.values.JavaUriHolderEx";
                else
                    baseclass =  "org.apache.xmlbeans.impl.values.JavaUriHolder";
                break;
            case SchemaType.BTC_QNAME:
                if (!isBuiltin)
                    baseclass =  "org.apache.xmlbeans.impl.values.JavaQNameHolderEx";
                else
                    baseclass =  "org.apache.xmlbeans.impl.values.JavaQNameHolder";
                break;
            case SchemaType.BTC_NOTATION:
                if (!isBuiltin)
                    baseclass =  "org.apache.xmlbeans.impl.values.JavaNotationHolderEx";
                else
                    baseclass =  "org.apache.xmlbeans.impl.values.JavaNotationHolder";
                break;
            case SchemaType.BTC_FLOAT:
                if (!isBuiltin)
                    baseclass =  "org.apache.xmlbeans.impl.values.JavaFloatHolderEx";
                else
                    baseclass =  "org.apache.xmlbeans.impl.values.JavaFloatHolder";
                break;
            case SchemaType.BTC_DOUBLE:
                if (!isBuiltin)
                    baseclass =  "org.apache.xmlbeans.impl.values.JavaDoubleHolderEx";
                else
                    baseclass =  "org.apache.xmlbeans.impl.values.JavaDoubleHolder";
                break;
            case SchemaType.BTC_STRING:
                /*
                if (sType.hasStringEnumValues())
                    return "org.apache.xmlbeans.impl.values.JavaStringEnumerationHolderEx";
                else
                */
                if (pType != sType)
                {
                    baseclass =  "org.apache.xmlbeans.impl.values.JavaStringHolderEx";
                    argcount = 2;
                }
                else
                {
                    baseclass =  "org.apache.xmlbeans.impl.values.JavaStringHolder";
                    argcount = 0;
                }
                break;

            case SchemaType.BTC_DATE_TIME:
            case SchemaType.BTC_TIME:
            case SchemaType.BTC_DATE:
            case SchemaType.BTC_G_YEAR_MONTH:
            case SchemaType.BTC_G_YEAR:
            case SchemaType.BTC_G_MONTH_DAY:
            case SchemaType.BTC_G_DAY:
            case SchemaType.BTC_G_MONTH:
                baseclass =  "org.apache.xmlbeans.impl.values.JavaGDateHolderEx";
                argcount = 2;
                break;

            case SchemaType.BTC_DURATION:
                baseclass =  "org.apache.xmlbeans.impl.values.JavaGDurationHolderEx";
                argcount = 2;
                break;

            case SchemaType.BTC_DECIMAL:
                // Decimal types are handled below
                break;

            default:
                assert(false) : "unrecognized primitive type";
                return;
        }

        while (baseclass == null)
        {
            argcount = 2;
            switch (sType.getBuiltinTypeCode())
            {
                case SchemaType.BTC_DECIMAL:
                    baseclass = "org.apache.xmlbeans.impl.values.JavaDecimalHolderEx";
                case SchemaType.BTC_INTEGER:
                    baseclass = "org.apache.xmlbeans.impl.values.JavaIntegerHolderEx";
                case SchemaType.BTC_LONG:
                case SchemaType.BTC_UNSIGNED_INT:
                    baseclass = "org.apache.xmlbeans.impl.values.JavaLongHolderEx";
                case SchemaType.BTC_UNSIGNED_SHORT:
                case SchemaType.BTC_INT:
                    baseclass = "org.apache.xmlbeans.impl.values.JavaIntHolderEx";
                default:
                    sType = sType.getBaseType();
            }
        }

        ci.setBaseImplName(baseclass);
        ci.setSuperCtrArgCount(argcount);

    }

    static void javaizeElement(SchemaLocalElement elt, Map componentData, Map packages)
    {
        // TODO: deal with local elements - only gen interface when part of a 
        // general content model. See 5.7.1

        if (! ( elt instanceof SchemaGlobalElement) )
            return;

        ElementInfo ei = (ElementInfo)componentData.get(elt);
        PackageInfo pi = getPackageInfo(packages, ei.getFullJavaIntfName());

        // TODO: deal with anonymous complex type definition see 5.7.2
        if (elt.getType().getContentType() != SchemaType.NOT_COMPLEX_TYPE )
        {
            // For an element whose type definition is complex, the Java Element interface
            // extends the Java content interface representing the type definition.
            TypeInfo ti = (TypeInfo)componentData.get(elt.getType());
            ei.setBaseIntfName(ti.getFullJavaIntfName());
            ei.setBaseImplName(ti.getFullJavaImplName());
            ei.setSuperCtrArgCount(1);

        }
        else
        {
            // For an element whose type definition is simple, The generated element
            // interface has a Java simple content-property named "value"
            ei.setValueType( getJavaPropertyType(elt.getType(), elt.isNillable()) );
            setJavaImplClass(elt.getType(), ei);

        }

    }

    static void javaizeType(SchemaType st, Map componentData, Map packages)
    {
        TypeInfo ti = (TypeInfo)componentData.get(st);

        if (st.getBaseType().equals(BuiltinSchemaTypeSystem.ST_ANY_TYPE))
        {
            // ti
        }

    }

    static ComponentInfo getComponentInfo(SchemaComponent sc, Map componentData)
    {
        ComponentInfo ci = (ComponentInfo)componentData.get(sc);
        if (ci != null) return ci;

        // Could not find - create a new record
        switch (sc.getComponentType())
        {
            case SchemaComponent.ELEMENT:
                ci = new ElementInfo();
                break;

            case SchemaComponent.TYPE:
                ci = new TypeInfo();

            default:
                throw new IllegalStateException("Cannot handle component of type: " + sc.getComponentType());
        }

        componentData.put(sc, ci);
        return ci;
    }

    static PackageInfo getPackageInfo(Map packages, String fqcn)
    {
        String pkgname = getPackageName(fqcn);
        return (PackageInfo)packages.get(pkgname);
    }

    static String getPackageName(String fqcn)
    {
        // remove class name
        int lastdot = fqcn.lastIndexOf('.');
        if (lastdot < 0)
            return "";

        // remove outer package names
        return fqcn.substring(0, lastdot);
    }

    static String pickFullJavaIntfName(Set usedNames, QName qName)
    {
        // BUGBUG: This util does not correctly implement JAXB naming rules
        String name = NameUtil.getClassNameFromQName(qName);

        if (usedNames.contains(name))
            throw new IllegalStateException("Name collision on qname: " + qName);

        if (reservedGlobalClassName(name))
            throw new IllegalStateException("QName " + qName + " conflicts with generated JAXB class name.");
            
        usedNames.add(name);

        return name;
    }

    static String pickFullJavaImplName(Set usedNames, String intfName)
    {
        // Strip off the package from the class name so we can replace it
        String className = intfName;
        String pkgName = null;
        int index = intfName.lastIndexOf('.');
        if (index >= 0)
        {
            className = intfName.substring(index + 1);
            pkgName = intfName.substring(0, index);
        }

        // Form the new qualified class name from the new package name
        // and the old class name
        String name = pkgName + ".impl." + className + "Impl";

        if (usedNames.contains(name))
            throw new IllegalStateException("QName conflict");

        return name;
    }

    static int getBuiltinTypeCode(SchemaType xmltype)
    {
        while (! xmltype.isBuiltinType())
            xmltype = xmltype.getBaseType();

        return xmltype.getBuiltinTypeCode();
    }

    static int getJavaPropertyType(SchemaType xmltype, boolean nillable)
    {
        assert xmltype.getContentType() == SchemaType.NOT_COMPLEX_TYPE;

        // TODO: handle lists, unions, enums

        while (true)
        {
            switch (xmltype.getBuiltinTypeCode())
            {
                case SchemaType.BTC_ANY_SIMPLE:
                case SchemaType.BTC_STRING:
                    return SchemaProperty.JAVA_STRING;

                case SchemaType.BTC_INTEGER:
                    return SchemaProperty.JAVA_BIG_INTEGER;

                case SchemaType.BTC_INT:
                case SchemaType.BTC_UNSIGNED_SHORT:
                    return SchemaProperty.JAVA_INT;

                case SchemaType.BTC_LONG:
                case SchemaType.BTC_UNSIGNED_INT:
                    return SchemaProperty.JAVA_LONG;

                case SchemaType.BTC_SHORT:
                case SchemaType.BTC_UNSIGNED_BYTE:
                    return SchemaProperty.JAVA_SHORT;

                case SchemaType.BTC_DECIMAL:
                    return SchemaProperty.JAVA_BIG_DECIMAL;

                case SchemaType.BTC_FLOAT:
                    return SchemaProperty.JAVA_FLOAT;

                case SchemaType.BTC_DOUBLE:
                    return SchemaProperty.JAVA_DOUBLE;

                case SchemaType.BTC_BOOLEAN:
                    return SchemaProperty.JAVA_BOOLEAN;

                case SchemaType.BTC_BYTE:
                    return SchemaProperty.JAVA_BYTE;

                case SchemaType.BTC_QNAME:
                    return SchemaProperty.JAVA_QNAME;

                case SchemaType.BTC_TIME:
                case SchemaType.BTC_DATE:
                case SchemaType.BTC_DATE_TIME:
                    return SchemaProperty.JAVA_CALENDAR;

                case SchemaType.BTC_BASE_64_BINARY:
                    return SchemaProperty.JAVA_BYTE_ARRAY;

                case SchemaType.BTC_HEX_BINARY:
                    return SchemaProperty.JAVA_BYTE;

                default:
                    xmltype = xmltype.getBaseType();
            }
        }
    }

    static boolean reservedGlobalClassName(String fqcn)
    {
        int i = fqcn.lastIndexOf('.');
        String lastSegment = fqcn.substring(i + 1);

        if (lastSegment.equals("ObjectFactory"))
            return true;

        return false;
    }
}
