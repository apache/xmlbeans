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

import org.apache.xmlbeans.EncodingStyle;
import org.apache.xmlbeans.SoapMarshaller;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlRuntimeException;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.marshal.util.ArrayUtils;
import org.apache.xmlbeans.impl.marshal.util.collections.EmptyIterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.util.Iterator;

//this class is not thread safe and doesn't have to be per javadocs

final class SoapMarshallerImpl
    implements SoapMarshaller
{
    //per binding context constants
    private final BindingLoader loader;
    private final RuntimeBindingTypeTable typeTable;
    private final EncodingStyle encodingStyle;
    private final InstanceVisitor instanceVisitor = new InstanceVisitor();

    private ObjectRefTable objectRefTable;


    SoapMarshallerImpl(BindingLoader loader,
                       RuntimeBindingTypeTable typeTable,
                       EncodingStyle encodingStyle)
    {
        this.loader = loader;
        this.typeTable = typeTable;
        this.encodingStyle = encodingStyle;
    }


    public void marshalType(XMLStreamWriter writer,
                            Object obj,
                            QName elementName,
                            QName schemaType,
                            String javaType,
                            XmlOptions options)
        throws XmlException
    {
        final RuntimeGlobalProperty prop =
            createGlobalProperty(schemaType, javaType, elementName, obj);

        if (prop.getRuntimeBindingType().hasElementChildren()) {
            objectRefTable = new ObjectRefTable();
        }

        final PushSoapMarshalResult result =
            createPushSoapResult(writer, options);

        addObjectGraphToRefTable(obj, prop.getRuntimeBindingType(),
                                 prop, result);

        result.marshalType(obj, prop);

    }

    private RuntimeGlobalProperty createGlobalProperty(QName schemaType, String javaType, QName elementName, Object obj) throws XmlException
    {
        final BindingType btype =
            MarshallerImpl.lookupBindingType(schemaType, javaType,
                                             elementName, obj, loader);

        assert btype != null;

        final RuntimeBindingType runtime_type =
            typeTable.createRuntimeType(btype, loader);

        runtime_type.checkInstance(obj);

        RuntimeGlobalProperty prop =
            new RuntimeGlobalProperty(elementName, runtime_type);
        return prop;
    }

    private PushSoapMarshalResult createPushSoapResult(XMLStreamWriter writer,
                                                       XmlOptions options)
        throws XmlException
    {
        final PushSoapMarshalResult result;
        if (EncodingStyle.SOAP11.equals(encodingStyle)) {
            result = new PushSoap11MarshalResult(loader,
                                                 typeTable,
                                                 writer,
                                                 options,
                                                 objectRefTable);
        } else if (EncodingStyle.SOAP12.equals(encodingStyle)) {
            throw new AssertionError("UNIMP");
        } else {
            throw new AssertionError("UNKNOWN ENCODING: " + encodingStyle);
        }
        return result;
    }

    public void marshalReferenced(XMLStreamWriter writer,
                                  XmlOptions options)
        throws XmlException
    {
        if (objectRefTable == null || !objectRefTable.hasMultiplyRefdObjects())
            return;

        final PushSoapMarshalResult result =
            createPushSoapResult(writer, options);
        result.writeIdParts();
    }

    public XMLStreamReader marshalType(Object obj,
                                       QName elementName,
                                       QName schemaType,
                                       String javaType,
                                       XmlOptions options)
        throws XmlException
    {
        final RuntimeGlobalProperty prop =
            createGlobalProperty(schemaType, javaType, elementName, obj);

        if (prop.getRuntimeBindingType().hasElementChildren()) {
            objectRefTable = new ObjectRefTable();
        }

        final NamespaceContext nscontext =
            MarshallerImpl.getNamespaceContextFromOptions(options);
        final PullSoapMarshalResult retval =
            createPullMarshalResult(nscontext, prop, obj, options, false);

        addObjectGraphToRefTable(obj, prop.getRuntimeBindingType(),
                                 prop, retval);

        return retval;
    }

    private PullSoapMarshalResult createPullMarshalResult(NamespaceContext nscontext,
                                                          RuntimeBindingProperty prop,
                                                          Object obj,
                                                          XmlOptions options,
                                                          boolean doing_id_parts)
        throws XmlException
    {
        final PullSoapMarshalResult retval;

        if (EncodingStyle.SOAP11.equals(encodingStyle)) {
            retval = new Soap11MarshalResult(loader, typeTable,
                                             nscontext, prop, obj, options,
                                             objectRefTable, doing_id_parts);
        } else if (EncodingStyle.SOAP12.equals(encodingStyle)) {
            throw new AssertionError("UNIMP");
        } else {
            throw new AssertionError("UNKNOWN ENCODING: " + encodingStyle);
        }
        return retval;
    }

    private void addObjectGraphToRefTable(Object obj,
                                          RuntimeBindingType runtime_type,
                                          RuntimeBindingProperty prop,
                                          MarshalResult result)
        throws XmlException
    {
        if (objectRefTable == null) return;

        //traverse graph...
        instanceVisitor.setCurrObject(obj, prop);
        instanceVisitor.setMarshalResult(result);
        runtime_type.accept(instanceVisitor);
    }

    public Iterator marshalReferenced(XmlOptions options)
        throws XmlException
    {
        if (objectRefTable == null || !objectRefTable.hasMultiplyRefdObjects())
            return EmptyIterator.getInstance();

        return new ReaderIterator(options);
    }

    private final class ReaderIterator
        implements Iterator
    {
        private final XmlOptions options;
        private final NamespaceContext nscontext;
        private final Iterator tblItr = objectRefTable.getMultipleRefTableEntries();
        private PullSoapMarshalResult marshalResult;

        public ReaderIterator(XmlOptions options)
        {
            this.options = options;
            nscontext = MarshallerImpl.getNamespaceContextFromOptions(options);
        }

        public boolean hasNext()
        {
            return tblItr.hasNext();
        }

        public Object next()
        {
            final ObjectRefTable.Value cur_val =
                (ObjectRefTable.Value)tblItr.next();
            assert cur_val.getCnt() > 1;

            try {
                if (marshalResult == null) {
                    marshalResult = createPullMarshalResult(nscontext,
                                                            cur_val.getProp(),
                                                            cur_val.object,
                                                            options, true);
                } else {
                    marshalResult.reset(cur_val.getProp(), cur_val.object, true);
                }
                return marshalResult;
            }
            catch (XmlException e) {
                //TODO: REVIEW: is there a better way to do this?
                //maybe write out own typesafe iterator...
                throw new XmlRuntimeException(e);
            }

        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

    }


    private final class InstanceVisitor
        implements RuntimeTypeVisitor
    {
        private Object currObject;
        private RuntimeBindingProperty currProp;
        private MarshalResult marshalResult;

        public void setCurrObject(Object obj,
                                  RuntimeBindingProperty prop)
        {
            this.currObject = obj;
            this.currProp = prop;
        }

        public void setMarshalResult(MarshalResult marshalResult)
        {
            this.marshalResult = marshalResult;
        }

        public void visit(BuiltinRuntimeBindingType builtinRuntimeBindingType)
            throws XmlException
        {
            initialVisit(currObject, currProp);
            //no element children so we are finished.
        }

        public void visit(ByNameRuntimeBindingType byNameRuntimeBindingType)
            throws XmlException
        {
            final Object curr_obj = currObject;
            final RuntimeBindingProperty curr_prop = currProp;

            if (initialVisit(curr_obj, curr_prop)) {
                return;
            }

            final int elem_cnt =
                byNameRuntimeBindingType.getElementPropertyCount();
            for (int i = 0; i < elem_cnt; i++) {
                final RuntimeBindingProperty prop =
                    byNameRuntimeBindingType.getElementProperty(i);

                if (prop.getRuntimeBindingType().isJavaPrimitive()) continue;
                if (!prop.isSet(curr_obj, marshalResult)) continue;

                final Object prop_val = prop.getValue(curr_obj, marshalResult);
                if (prop.isMultiple()) {
                    //NOTE: we don't add the array itself becuase there is no
                    //actual element on which to put the id or href attribute.
                    final Iterator itr = ArrayUtils.getCollectionIterator(prop_val);
                    while (itr.hasNext()) {
                        visitProp(itr.next(), prop, curr_obj, curr_prop);
                    }
                } else {
                    visitProp(prop_val, prop, curr_obj, curr_prop);
                }
            }
        }

        private void visitProp(final Object prop_val,
                               final RuntimeBindingProperty prop,
                               final Object curr_obj,
                               final RuntimeBindingProperty curr_prop)
            throws XmlException
        {
            setCurrObject(prop_val, prop);
            RuntimeBindingType rtt = prop.getRuntimeBindingType();
            if (rtt.hasElementChildren()) {
                prop.getActualRuntimeType(prop_val, marshalResult).accept(this);
            } else {
                rtt.accept(this);
            }
            setCurrObject(curr_obj, curr_prop);
        }

        public void visit(SimpleContentRuntimeBindingType simpleContentRuntimeBindingType)
            throws XmlException
        {
            initialVisit(currObject, currProp);
            //no element children so we are finished.
        }

        public void visit(SimpleRuntimeBindingType simpleRuntimeBindingType)
            throws XmlException
        {
            initialVisit(currObject, currProp);
            //no element children so we are finished.
        }

        public void visit(JaxrpcEnumRuntimeBindingType jaxrpcEnumRuntimeBindingType)
            throws XmlException
        {
            initialVisit(currObject, currProp);
            //no element children so we are finished.
        }

        public void visit(WrappedArrayRuntimeBindingType wrappedArrayRuntimeBindingType)
            throws XmlException
        {
            final Object curr_obj = currObject;
            final RuntimeBindingProperty curr_prop = currProp;

            if (initialVisit(curr_obj, curr_prop)) {
                return;
            }

            final RuntimeBindingProperty elem_prop =
                wrappedArrayRuntimeBindingType.getElementProperty();

            if (elem_prop.getRuntimeBindingType().isJavaPrimitive()) return;


            //REVIEW: consider direct array access
            final Iterator itr = ArrayUtils.getCollectionIterator(curr_obj);
            while (itr.hasNext()) {
                final Object item = itr.next();
                setCurrObject(item, elem_prop);
                elem_prop.getActualRuntimeType(item, marshalResult).accept(this);
            }
            setCurrObject(curr_obj, curr_prop);
        }

        public void visit(SoapArrayRuntimeBindingType soapArrayRuntimeBindingType)
            throws XmlException
        {
            throw new AssertionError("UNIMP");
        }

        public void visit(ListArrayRuntimeBindingType listArrayRuntimeBindingType)
            throws XmlException
        {
            initialVisit(currObject, currProp);
            //no element children so we are finished.
        }

        //return true if we should stop processing!
        private boolean initialVisit(Object obj,
                                     RuntimeBindingProperty property)
        {
            return objectRefTable.incrementRefCount(obj, property) > 1;
        }
    }

}
