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

import org.apache.xmlbeans.Marshaller;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlRuntimeException;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.binding.bts.BindingTypeName;
import org.apache.xmlbeans.impl.binding.bts.JavaTypeName;
import org.apache.xmlbeans.impl.binding.bts.XmlTypeName;
import org.apache.xmlbeans.impl.common.XmlReaderToWriter;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.util.Collection;

final class MarshallerImpl
    implements Marshaller
{
    private final BindingLoader loader;
    private final RuntimeBindingTypeTable typeTable;
    private final ScopedNamespaceContext namespaceContext;
    private final RuntimeTypeFactory runtimeTypeFactory =
        new RuntimeTypeFactory();
    private final Collection errors;
    private final XmlOptions options;

    private int prefixCnt = 0;

    private static final String NSPREFIX = "n";
    private static final String XML_VERSION = "1.0";

    MarshallerImpl(NamespaceContext root_nsctx,
                   BindingLoader loader,
                   RuntimeBindingTypeTable typeTable,
                   XmlOptions options)
    {
        this.namespaceContext = new ScopedNamespaceContext(root_nsctx);
        this.loader = loader;
        this.typeTable = typeTable;
        this.errors = BindingContextImpl.extractErrorHandler(options);
        this.options = options;

        namespaceContext.openScope();
    }

    public XMLStreamReader marshal(Object obj,
                                   NamespaceContext nscontext)
        throws XmlException
    {
        JavaTypeName jname = determineJavaType(obj);
        BindingTypeName root_elem_btype = loader.lookupElementFor(jname);
        if (root_elem_btype == null) {
            final String msg = "failed to find root " +
                "element corresponding to " + jname;
            throw new XmlRuntimeException(msg);
        }

        final XmlTypeName elem = root_elem_btype.getXmlName();
        assert elem.getComponentType() == XmlTypeName.ELEMENT;
        final QName elem_qn = elem.getQName();

        //get type for this element/object pair
        final BindingTypeName type_name = loader.lookupTypeFor(jname);
        if (type_name == null) {
            String msg = "failed to lookup type for " + jname;
            throw new XmlException(msg);
        }
        final BindingType btype = loader.getBindingType(type_name);
        if (btype == null) {
            String msg = "failed to load type " + type_name;
            throw new XmlException(msg);
        }

        RuntimeGlobalProperty prop = new RuntimeGlobalProperty(btype, elem_qn);
        return new MarshalResult(prop, obj, this);
    }

    private static JavaTypeName determineJavaType(Object obj)
    {
        return determineJavaType(obj.getClass());
    }

    private static JavaTypeName determineJavaType(Class clazz)
    {
        return JavaTypeName.forString(clazz.getName());
    }

    public void marshal(XMLStreamWriter writer, Object obj)
        throws XmlException
    {
        XMLStreamReader rdr = marshal(obj, writer.getNamespaceContext());
        try {
            XmlReaderToWriter.writeAll(rdr, writer);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    public void marshal(OutputStream out, Object obj)
        throws XmlException
    {
        String encoding = (String)options.get(XmlOptions.CHARACTER_ENCODING);
        if (encoding != null) {
            marshal(out, obj, encoding);
        } else {
            try {
                final XMLOutputFactory xof = XMLOutputFactory.newInstance();
                final XMLStreamWriter writer = xof.createXMLStreamWriter(out);
                marshal(writer, obj);
            }
            catch (XMLStreamException e) {
                throw new XmlException(e);
            }
        }
    }

    public void marshal(OutputStream out, Object obj, String encoding)
        throws XmlException
    {
        if (encoding == null) {
            throw new IllegalArgumentException("null encoding");
        }

        try {
            XMLOutputFactory output_factory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer =
                output_factory.createXMLStreamWriter(out, encoding);
            writer.writeStartDocument(encoding, XML_VERSION);
            marshal(writer, obj);
            writer.writeEndDocument();
            writer.close();
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    public XMLStreamReader marshalType(Object obj,
                                       QName elementName,
                                       QName schemaType,
                                       String javaType,
                                       NamespaceContext namespaceContext)
        throws XmlException
    {
        BindingType type = lookupBindingType(obj.getClass(),
                                             JavaTypeName.forString(javaType),
                                             XmlTypeName.forTypeNamed(schemaType),
                                             loader);
        if (type == null) {
            final String msg = "failed to find a suitable binding type for" +
                " use in marshalling \"" + elementName + "\". " +
                " instance type: " + obj.getClass().getName() +
                " expected java type: " + javaType +
                " schema type: " + schemaType;
            throw new XmlException(msg);
        }
        RuntimeGlobalProperty prop = new RuntimeGlobalProperty(type, elementName);
        return new MarshalResult(prop, obj, this);
    }

    public void marshalType(XMLStreamWriter writer,
                            Object obj,
                            QName elementName,
                            QName schemaType,
                            String javaType)
        throws XmlException
    {
        XMLStreamReader rdr = marshalType(obj, elementName, schemaType,
                                          javaType,
                                          writer.getNamespaceContext());
        try {
            XmlReaderToWriter.writeAll(rdr, writer);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }


    //TODO: refine this algorithm to deal better
    //with primitives/interfaces/other oddities
    //we are basically just walking up the super types
    //till we hit a class that we can deal with.

    //returns null if we fail
    private static BindingType lookupBindingType(Class instance_type,
                                                 JavaTypeName java_type,
                                                 XmlTypeName xml_type,
                                                 BindingLoader loader)
    {
        //look first for exact match
        {
            JavaTypeName jname = determineJavaType(instance_type);
            BindingType bt = loadBindingType(xml_type, jname, loader);
            if (bt != null) return bt;  //success!
        }


        BindingType binding_type = null;
        Class curr_class = instance_type;
        Class super_type = null;

        while (true) {
            JavaTypeName jname = determineJavaType(curr_class);

            BindingTypeName btype_name = loader.lookupTypeFor(jname);
            if (btype_name != null) {
                binding_type = loader.getBindingType(btype_name);
                if (binding_type == null) {
                    String e = "binding configuration inconsistency: found " +
                        btype_name + " defined for " + jname + " but failed " +
                        "to load the type";
                    throw new XmlRuntimeException(e);
                } else {
                    return binding_type; //success!
                }
            }

            super_type = curr_class.getSuperclass();

            //note that we check that this super-super type check is to avoid
            //getting a match on java.lang.Object, which doesn't do us any good
            if (super_type == null || (super_type.getSuperclass() == null)) {
                break;
            }

            curr_class = super_type;
        }

        //reaching here means that we've failed using the actual instance,
        //so let's try the expected type
        assert (binding_type == null);
        return loadBindingType(xml_type, java_type, loader);
    }


    private static BindingType loadBindingType(XmlTypeName xname,
                                               JavaTypeName jname,
                                               BindingLoader loader)
    {
        BindingTypeName btname = BindingTypeName.forPair(jname, xname);
        return loader.getBindingType(btname);
    }


    Collection getErrorCollection()
    {
        return errors;
    }

    BindingLoader getLoader()
    {
        return loader;
    }

    RuntimeBindingTypeTable getTypeTable()
    {
        return typeTable;
    }

    ScopedNamespaceContext getNamespaceContext()
    {
        return namespaceContext;
    }

    RuntimeBindingType createRuntimeBindingType(BindingType type, Object instance)
    {
        final BindingTypeName type_name = type.getName();
        String expectedJavaClass = type_name.getJavaName().toString();
        String actualJavaClass = instance.getClass().getName();
        if (!actualJavaClass.equals(expectedJavaClass)) {
            final BindingType actual_type = lookupBindingType(instance.getClass(),
                                                              type_name.getJavaName(),
                                                              type_name.getXmlName(),
                                                              loader);
            if (actual_type != null) {
                System.out.println("****** USING: " + actual_type + " SUBT for " + type);
                type = actual_type;          //redefine type param
            }
            //else go with original type and hope for the best...
        }
        return runtimeTypeFactory.createRuntimeType(type, typeTable, loader);
    }

    String ensurePrefix(String uri)
    {
        String prefix = namespaceContext.getPrefix(uri);
        if (prefix == null) {
            prefix = bindNextPrefix(uri);
        }
        assert prefix != null;
        return prefix;
    }

    private String bindNextPrefix(final String uri)
    {
        assert uri != null;
        String testuri;
        String prefix;
        do {
            prefix = NSPREFIX + (++prefixCnt);
            testuri = namespaceContext.getNamespaceURI(prefix);
        }
        while (testuri != null);
        assert prefix != null;
        namespaceContext.bindNamespace(prefix, uri);
        return prefix;
    }

}
