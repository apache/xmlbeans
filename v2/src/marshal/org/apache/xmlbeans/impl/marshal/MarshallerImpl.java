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

package org.apache.xmlbeans.impl.marshal;

import org.apache.xmlbeans.Marshaller;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
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
import java.io.IOException;
import java.io.OutputStream;

final class MarshallerImpl
    implements Marshaller
{
    //per binding context constants
    private final BindingLoader loader;
    private final RuntimeBindingTypeTable typeTable;
    private final RuntimeTypeFactory runtimeTypeFactory;

    private static final XMLOutputFactory XML_OUTPUT_FACTORY =
        XMLOutputFactory.newInstance();

    private static final String XML_VERSION = "1.0";

    public MarshallerImpl(BindingLoader loader,
                          RuntimeBindingTypeTable typeTable,
                          RuntimeTypeFactory runtimeTypeFactory)
    {
        this.loader = loader;
        this.typeTable = typeTable;
        this.runtimeTypeFactory = runtimeTypeFactory;
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
            throw new XmlException(msg);
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

        final RuntimeBindingType runtime_type =
            runtimeTypeFactory.createRuntimeType(btype, typeTable, loader);

        RuntimeGlobalProperty prop =
            new RuntimeGlobalProperty(elem_qn, runtime_type);

        //TODO: review null options param
        return new MarshalResult(runtimeTypeFactory, loader, typeTable,
                                 nscontext, prop, obj, null);
    }

    public XMLStreamReader marshal(Object obj,
                                   XmlOptions options)
        throws XmlException
    {
        //TODO: actually use the options!
        return marshal(obj, EmptyNamespaceContext.getInstance());
    }

    private static JavaTypeName determineJavaType(Object obj)
    {
        return determineJavaType(obj.getClass());
    }

    private static JavaTypeName determineJavaType(Class clazz)
    {
        return JavaTypeName.forClassName(clazz.getName());
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

    public void marshal(XMLStreamWriter writer, Object obj, XmlOptions options)
        throws XmlException
    {
        //TODO: javadoc that pretty is not supported here.

        String encoding = getEncoding(options);
        marshalToOutputStream(obj, encoding, writer);
    }

    private static String getEncoding(XmlOptions options)
    {
        return (String)XmlOptions.safeGet(options,
                                          XmlOptions.CHARACTER_ENCODING);
    }

    public void marshal(OutputStream out, Object obj)
        throws XmlException
    {
        try {
            XMLStreamWriter writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(out);
            XMLStreamReader rdr = marshal(obj, writer.getNamespaceContext());
            XmlReaderToWriter.writeAll(rdr, writer);
        }
        catch (XMLStreamException xse) {
            throw new XmlException(xse);
        }
    }

    public void marshal(OutputStream out, Object obj, XmlOptions options)
        throws XmlException
    {
        if (options != null && options.hasOption(XmlOptions.SAVE_PRETTY_PRINT)) {
            marshalPretty(out, obj, options);
        } else {
            final String encoding = getEncoding(options);
            final XMLStreamWriter writer;
            try {
                writer = createXmlStreamWriter(out, encoding);
            }
            catch (XMLStreamException e) {
                throw new XmlException(e);
            }
            marshalToOutputStream(obj, encoding, writer);
        }
    }

    private void marshalPretty(OutputStream out,
                               Object obj,
                               XmlOptions options)
        throws XmlException
    {
        XMLStreamReader rdr = marshal(obj, EmptyNamespaceContext.getInstance());
        XmlObject xobj = XmlObject.Factory.parse(rdr);
        try {
            xobj.save(out, options);
        }
        catch (IOException e) {
            throw new XmlException(e);
        }
    }

    private void marshalToOutputStream(Object obj,
                                       final String encoding,
                                       XMLStreamWriter writer)
        throws XmlException
    {
        try {
            if (encoding != null)
                writer.writeStartDocument(encoding, XML_VERSION);

            marshal(writer, obj);
            writer.writeEndDocument();
            writer.close();
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    private XMLStreamWriter createXmlStreamWriter(OutputStream out,
                                                  final String encoding)
        throws XMLStreamException
    {
        XMLStreamWriter writer;
        if (encoding != null) {
            writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(out, encoding);
        } else {
            writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(out);
        }
        return writer;
    }

    public void marshal(OutputStream out, Object obj, String encoding)
        throws XmlException
    {
        if (encoding == null) {
            throw new IllegalArgumentException("null encoding");
        }

        XmlOptions opts = new XmlOptions();
        opts.setCharacterEncoding(encoding);

        marshal(out, obj, opts);
    }


    public XMLStreamReader marshalType(Object obj,
                                       QName elementName,
                                       QName schemaType,
                                       String javaType,
                                       NamespaceContext namespaceContext)
        throws XmlException
    {
        final BindingType type =
            loadBindingType(XmlTypeName.forTypeNamed(schemaType),
                            JavaTypeName.forClassName(javaType),
                            loader);

        if (type == null) {
            final String msg = "failed to find a suitable binding type for" +
                " use in marshalling \"" + elementName + "\". " +
                " using java type: " + javaType +
                " schema type: " + schemaType +
                " instance type: " + obj.getClass().getName();
            throw new XmlException(msg);
        }

        final RuntimeBindingType runtime_type =
            runtimeTypeFactory.createRuntimeType(type, typeTable, loader);

        RuntimeGlobalProperty prop =
            new RuntimeGlobalProperty(elementName, runtime_type);

        return new MarshalResult(runtimeTypeFactory, loader, typeTable,
                                 namespaceContext, prop, obj, null);
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

    public void marshalType(XMLStreamWriter writer,
                            Object obj,
                            QName elementName,
                            QName schemaType,
                            String javaType,
                            XmlOptions options)
        throws XmlException
    {
        //TODO: actually use the options!!
        marshalType(writer, obj, elementName, schemaType, javaType);
    }

    public XMLStreamReader marshalType(Object obj,
                                       QName elementName,
                                       QName schemaType,
                                       String javaType,
                                       XmlOptions options)
        throws XmlException
    {
        //TODO: actually use the options!!
        return marshalType(obj, elementName, schemaType, javaType, EmptyNamespaceContext.getInstance());
    }


    //TODO: refine this algorithm to deal better
    //with primitives/interfaces/other oddities
    //we are basically just walking up the super types
    //till we hit a class that we can deal with.

    //returns null if we fail
    static BindingType lookupBindingType(Class instance_type,
                                         JavaTypeName java_type,
                                         XmlTypeName xml_type,
                                         BindingLoader loader)
        throws XmlException
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
                    throw new XmlException(e);
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

    BindingLoader getLoader()
    {
        return loader;
    }

    RuntimeBindingTypeTable getTypeTable()
    {
        return typeTable;
    }

}
