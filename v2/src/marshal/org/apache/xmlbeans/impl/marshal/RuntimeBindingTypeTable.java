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

package org.apache.xmlbeans.impl.marshal;

import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.binding.bts.BindingTypeName;
import org.apache.xmlbeans.impl.binding.bts.BuiltinBindingLoader;
import org.apache.xmlbeans.impl.binding.bts.JavaName;
import org.apache.xmlbeans.impl.binding.bts.XmlName;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Table of TypeMarshaller and TypeUnmarshaller objects keyed by BindingType
 */
class RuntimeBindingTypeTable
{

    private final Map typeMap = new HashMap();

    protected static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";

    RuntimeBindingTypeTable()
    {
        addBuiltins();
    }

    TypeUnmarshaller getTypeUnmarshaller(BindingType key)
    {
        TTEntry e = (TTEntry)typeMap.get(key);
        if (e == null) return null;
        return e.typeUnmarshaller;
    }

    TypeMarshaller getTypeMarshaller(BindingType key)
    {
        TTEntry e = (TTEntry)typeMap.get(key);
        if (e == null) return null;
        return e.typeMarshaller;
    }

    void putTypeUnmarshaller(BindingType key, TypeUnmarshaller um)
    {
        TTEntry e = (TTEntry)typeMap.get(key);
        if (e == null) {
            e = new TTEntry();
            typeMap.put(key, e);
        }
        e.typeUnmarshaller = um;
    }

    void putTypeMarshaller(BindingType key, TypeMarshaller m)
    {
        TTEntry e = (TTEntry)typeMap.get(key);
        if (e == null) {
            e = new TTEntry();
            typeMap.put(key, e);
        }
        e.typeMarshaller = m;
    }

    //debugging only
    Set getKeys()
    {
        return Collections.unmodifiableSet(typeMap.keySet());
    }

    public void initUnmarshallers(BindingLoader loader)
    {
        for (Iterator iterator = typeMap.values().iterator(); iterator.hasNext();) {
            TTEntry entry = (TTEntry)iterator.next();
            entry.typeUnmarshaller.initialize(this, loader);
        }
    }

    private void addBuiltins()
    {
        addXsdBuiltin("float", float.class.getName(),
                      new AtomicSimpleTypeConverter(new FloatLexerPrinter()));
        addXsdBuiltin("float", Float.class.getName(),
                      new AtomicSimpleTypeConverter(new FloatLexerPrinter()));

        addXsdBuiltin("long", long.class.getName(),
                      new AtomicSimpleTypeConverter(new LongLexerPrinter()));
        addXsdBuiltin("long", Long.class.getName(),
                      new AtomicSimpleTypeConverter(new LongLexerPrinter()));

        addXsdBuiltin("string", String.class.getName(),
                      new AtomicSimpleTypeConverter(new StringLexerPrinter()));

        addXsdBuiltin("token", String.class.getName(),
                      new AtomicSimpleTypeConverter(new StringLexerPrinter()));
    }


    protected void addXsdBuiltin(String xsdType, String javaType, TypeConverter converter)
    {
        final BindingLoader bindingLoader = BuiltinBindingLoader.getInstance();

        QName xml_type = new QName(XSD_NS, xsdType);
        JavaName jName = JavaName.forString(javaType);
        XmlName xName = XmlName.forTypeNamed(xml_type);
        BindingType btype = bindingLoader.getBindingType(BindingTypeName.forPair(jName, xName));
        if (btype == null) {
            throw new AssertionError("failed to find builtin for java:" + jName +
                                     " - xsd:" + xName);
        }
        putTypeMarshaller(btype, converter);
        putTypeUnmarshaller(btype, converter);

        assert getTypeMarshaller(btype) == converter;
        assert getTypeUnmarshaller(btype) == converter;
    }


    private static class TTEntry
    {
        TypeMarshaller typeMarshaller;
        TypeUnmarshaller typeUnmarshaller;
    }


}
