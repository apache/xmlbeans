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

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.marshal.util.ArrayUtils;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Iterator;

abstract class PushMarshalResult
    extends MarshalResult
    implements RuntimeTypeVisitor
{
    //state fields
    private final XMLStreamWriter writer;
    private Object currObject;
    private RuntimeBindingProperty currProp;

    PushMarshalResult(BindingLoader bindingLoader,
                      RuntimeBindingTypeTable typeTable,
                      XMLStreamWriter writer,
                      XmlOptions options)
        throws XmlException
    {
        super(bindingLoader,
              typeTable,
              options);
        this.writer = writer;
    }


    final void marshalType(final Object obj,
                           final RuntimeBindingProperty prop)
        throws XmlException
    {
        final RuntimeBindingType actual_rtt =
            prop.getActualRuntimeType(obj, this);
        marshalType(obj, prop, actual_rtt);
    }

    //this method can be called recursively
    private void marshalType(final Object obj,
                             final RuntimeBindingProperty prop,
                             final RuntimeBindingType actual_rtt)
        throws XmlException
    {
        marshalTypeWithName(obj, prop, actual_rtt, prop.getName());
    }


    //this method can be called recursively
    private void marshalTypeWithName(final Object obj,
                                     final RuntimeBindingProperty prop,
                                     final RuntimeBindingType actual_rtt,
                                     QName name)
        throws XmlException
    {
        try {
            writeStartElement(name);
            updateState(obj, prop);
            writeContents(actual_rtt);
            writeEndElement();
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    protected final void writeEndElement() throws XMLStreamException
    {
        writer.writeEndElement();
    }

    protected final void updateState(final Object obj,
                                     final RuntimeBindingProperty prop)
    {
        currObject = obj;
        currProp = prop;
    }

    private void writeXsiAttributes(final RuntimeBindingType actual_rtt)
        throws XmlException
    {
        if (currObject == null) {
            addXsiNilAttribute();
        } else if (actual_rtt != currProp.getRuntimeBindingType()) {
            addXsiTypeAttribute(actual_rtt);
        }
    }

    protected void writeContents(final RuntimeBindingType actual_rtt)
        throws XmlException
    {
        writeXsiAttributes(actual_rtt);
        actual_rtt.accept(this);
    }

    protected final Object getCurrObject()
    {
        return currObject;
    }

    protected void writeStartElement(QName name)
        throws XMLStreamException, XmlException
    {
        final String uri = name.getNamespaceURI();
        if (uri.length() > 0) {
            String prefix = getNamespaceContext().getPrefix(uri);
            final String new_prefix;
            if (prefix == null) {
                new_prefix = findNextPrefix(uri);
                prefix = new_prefix;
            } else {
                new_prefix = null;
            }
            assert prefix != null;
            writer.writeStartElement(prefix,
                                     name.getLocalPart(),
                                     name.getNamespaceURI());
            if (new_prefix != null) {
                bindNamespace(new_prefix, uri);
            }
        } else {
            writer.writeStartElement(name.getLocalPart());
        }
    }


    public NamespaceContext getNamespaceContext()
    {
        return writer.getNamespaceContext();
    }

    protected void bindNamespace(String prefix, String uri)
        throws XmlException
    {
        try {
            writer.writeNamespace(prefix, uri);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    protected void addAttribute(String lname,
                                String value)
        throws XmlException
    {
        try {
            writer.writeAttribute(lname, value);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    protected void addAttribute(String uri,
                                String lname,
                                String prefix,
                                String value)
        throws XmlException
    {
        assert uri != null;
        assert lname != null;
        assert prefix != null;
        assert value != null;

        try {
            writer.writeAttribute(prefix, uri, lname, value);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }


    //====== visitor methods =======

    public void visit(BuiltinRuntimeBindingType builtinRuntimeBindingType)
        throws XmlException
    {
        writeCharData();
    }

    public void visit(ByNameRuntimeBindingType byNameRuntimeBindingType)
        throws XmlException
    {
        final Object curr_obj = currObject;

        writeAttributes(byNameRuntimeBindingType);


        final int elem_cnt =
            byNameRuntimeBindingType.getElementPropertyCount();
        for (int i = 0; i < elem_cnt; i++) {
            final RuntimeBindingProperty prop =
                byNameRuntimeBindingType.getElementProperty(i);

            if (!prop.isSet(curr_obj, this)) continue;

            final Object prop_val = prop.getValue(curr_obj, this);
            if (prop.isMultiple()) {
                final Iterator itr = ArrayUtils.getCollectionIterator(prop_val);
                while (itr.hasNext()) {
                    visitProp(itr.next(), prop);
                }
            } else {
                visitProp(prop_val, prop);
            }
        }
    }

    public void visit(SimpleContentRuntimeBindingType simpleContentRuntimeBindingType)
        throws XmlException
    {
        writeAttributes(simpleContentRuntimeBindingType);
        writeCharData();
    }

    public void visit(SimpleRuntimeBindingType simpleRuntimeBindingType)
        throws XmlException
    {
        writeCharData();
    }

    public void visit(JaxrpcEnumRuntimeBindingType jaxrpcEnumRuntimeBindingType)
        throws XmlException
    {
        writeCharData();
    }

    public void visit(WrappedArrayRuntimeBindingType wrappedArrayRuntimeBindingType)
        throws XmlException
    {
        final RuntimeBindingProperty elem_prop =
            wrappedArrayRuntimeBindingType.getElementProperty();

        //REVIEW: consider direct array access
        final Iterator itr = ArrayUtils.getCollectionIterator(currObject);
        while (itr.hasNext()) {
            final Object item = itr.next();
            final RuntimeBindingType actual_rtt =
                elem_prop.getActualRuntimeType(item, this);
            marshalType(item, elem_prop, actual_rtt);
        }
    }

    public void visit(ListArrayRuntimeBindingType listArrayRuntimeBindingType)
        throws XmlException
    {
        writeCharData();
    }

    private void writeAttributes(AttributeRuntimeBindingType att_rtt)
        throws XmlException
    {
        final Object curr_obj = currObject;
        final int att_cnt = att_rtt.getAttributePropertyCount();
        for (int i = 0; i < att_cnt; i++) {
            final RuntimeBindingProperty prop =
                att_rtt.getAttributeProperty(i);
            if (!prop.isSet(curr_obj, this)) continue;

            final Object prop_val = prop.getValue(curr_obj, this);
            final CharSequence att_val = prop.getLexical(prop_val,
                                                         this);

            if (att_val == null) continue;
            fillAndAddAttribute(prop.getName(), att_val.toString());
        }
    }

    private void visitProp(final Object prop_val,
                           final RuntimeBindingProperty prop)
        throws XmlException
    {
        RuntimeBindingType rtt = prop.getActualRuntimeType(prop_val, this);
        marshalType(prop_val, prop, rtt);
    }

    private void writeCharData() throws XmlException
    {
        if (currObject == null) return;

        final CharSequence lexical = currProp.getLexical(currObject, this);
        try {
            //too bad writer can't take a CharSequence.
            writer.writeCharacters(lexical.toString());
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

}
