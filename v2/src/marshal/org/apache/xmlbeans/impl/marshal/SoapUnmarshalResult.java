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

import org.apache.xmlbeans.ObjectFactory;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.common.InvalidLexicalValueException;
import org.apache.xmlbeans.impl.common.XmlStreamUtils;
import org.apache.xmlbeans.impl.common.XmlWhitespace;
import org.apache.xmlbeans.impl.richParser.XMLStreamReaderExt;
import org.apache.xmlbeans.impl.richParser.XMLStreamReaderExtImpl;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

abstract class SoapUnmarshalResult
    extends UnmarshalResult
{
    private final SoapAttributeHolder soapAttributeHolder =
        new SoapAttributeHolder();
    private final RefObjectTable refObjectTable;
    private final StreamRefNavigator streamRefNavigator;
    private List fillEvents;
    private IdentityHashMap interToFinalMap;

    SoapUnmarshalResult(BindingLoader loader,
                        RuntimeBindingTypeTable typeTable,
                        RefObjectTable refObjectTable,
                        StreamRefNavigator refNavigator,
                        XmlOptions options)
    {
        super(loader, typeTable, options);
        this.refObjectTable = refObjectTable;
        assert refNavigator != null;
        this.streamRefNavigator = refNavigator;
    }

    protected XMLStreamReader getValidatingStream(XMLStreamReader reader)
    {
        return reader;
    }

    protected XMLStreamReader getValidatingStream(QName schemaType,
                                                  XMLStreamReader reader)
        throws XmlException
    {
        return reader;
    }

    protected Object unmarshalBindingType(BindingType bindingType)
        throws XmlException
    {
        updateSoapAttributes();

        {
            final String ref = soapAttributeHolder.ref;
            if (ref != null) {
                baseReader = relocateStreamToRef(ref);
                updateSoapAttributes();
            }
        }

        final String id = soapAttributeHolder.id;
        updateAttributeState();


        final TypeUnmarshaller um;
        final ObjectFactory of = extractObjectFactory();

        Object retval;

        try {
            final RuntimeBindingType rtt = getRuntimeType(bindingType);

            //we have a 2x2 matrix of choices here (factory yes/no, id yes/no)
            //would be nice to clean up this code
            if (of == null) {
                if (id == null) {
                    if (hasXsiNil())
                        um = NullUnmarshaller.getInstance();
                    else
                        um = rtt.getUnmarshaller();
                    retval = um.unmarshal(this);
                } else {
                    if (!hasXsiNil() && rtt.hasElementChildren()) {
                        final Object inter = rtt.createIntermediary(this);
                        retval = umarshalComplexElementWithId(rtt, inter, id);
                    } else {
                        retval = unmarshalSimpleElementWithId(rtt, id);
                    }
                }
            } else {
                final Object initial_obj = of.createObject(rtt.getJavaType());
                um = rtt.getUnmarshaller();
                final Object inter = rtt.createIntermediary(this, initial_obj);
                if (id == null) {
                    um.unmarshalIntoIntermediary(inter, this);
                    retval = rtt.getFinalObjectFromIntermediary(inter, this);
                } else {
                    retval = umarshalComplexElementWithId(rtt, inter, id);
                }
            }
        }
        catch (InvalidLexicalValueException ilve) {
            //top level simple types can end up here for invalid lexical values
            assert !errors.isEmpty();
            retval = null;
        }

        fireFillEvents();

        return retval;
    }

    private void fireFillEvents()

    {
        if (fillEvents == null || fillEvents.isEmpty()) return;

        for (int i = 0, len = fillEvents.size(); i < len; i++) {
            final FillEvent event = (FillEvent)fillEvents.get(i);
            final Object prop_val = refObjectTable.getObjectForRef(event.ref);
            assert prop_val != null; //TODO: unmask null values

            event.prop.fill(getFinalObjForInter(event.inter),
                            event.index, prop_val);
        }
    }

    private Object getFinalObjForInter(Object inter)
    {
        assert interToFinalMap != null;
        final Object final_obj = interToFinalMap.get(inter);
        assert final_obj != null;
        return final_obj;
    }


    void extractAndFillElementProp(final RuntimeBindingProperty prop,
                                   Object inter)
        throws XmlException
    {
        assert baseReader != null;

        final XMLStreamReaderExt curr_reader = baseReader;

        if (!processRef(prop, inter)) {
            basicExtractAndFill(prop, inter);
        }

        if (baseReader != curr_reader) {
            baseReader = curr_reader;
            skipElement();
        }
    }

    //beware this method  has side effects.
    //returns true iff this method filled the prop value (from ref cache).
    private boolean processRef(final RuntimeBindingProperty prop,
                               final Object inter)
        throws XmlException
    {
        updateSoapAttributes();

        final String ref = soapAttributeHolder.ref;
        if (ref == null) return false;

        final RefObjectTable.RefEntry entry = refObjectTable.getEntryForRef(ref);

        //TODO: cleanup all the null checking...
        //TODO: mask null values in refObjectTable

        final Object tmpval = entry == null ? null : entry.final_obj;
        if (tmpval == null) {
            final Object tmp_inter = entry == null ? null : entry.inter;
            if (tmp_inter == null) {
                baseReader = relocateStreamToRef(ref);
                updateSoapAttributes();
                updateAttributeState();
                return false;
            } else {
                enqueueFillEvent(inter, ref, prop);
                prop.fillPlaceholder(inter);
                skipElement();
                return true;
            }
        } else {
            prop.fill(inter, tmpval);
            skipElement();
            return true;
        }
    }

    private void enqueueFillEvent(Object inter,
                                  String ref,
                                  RuntimeBindingProperty prop)
    {
        if (fillEvents == null) {
            fillEvents = new ArrayList();
        }
        fillEvents.add(new FillEvent(ref, prop, prop.getSize(inter), inter));
    }

    private void basicExtractAndFill(RuntimeBindingProperty prop,
                                     Object inter)
        throws XmlException
    {
        try {
            final Object this_val =
                unmarshalElementProperty(prop, inter);

            prop.fill(inter, this_val);
        }
        catch (InvalidLexicalValueException ilve) {
            //unlike attributes, the error has been added to the this
            //already via BaseSimpleTypeConveter...
        }
    }

    private XMLStreamReaderExt relocateStreamToRef(String ref)
        throws XmlException
    {
        assert streamRefNavigator != null;

        final XMLStreamReader reader = streamRefNavigator.lookupRef(ref);

        if (reader == null) {
            //TODO: better error handling in this case!!
            throw new XmlException("failed to deref " + ref);
        }

        return new XMLStreamReaderExtImpl(reader);
    }

    private void updateSoapAttributes()
        throws XmlException
    {
        final QName idname = getIdAttributeName();
        final QName refname = getRefAttributeName();

        //for perf, we assume that the id and ref attributes are
        //in the same namespace.  This is true for soap 1.1 and 1.2
        assert idname.getNamespaceURI().equals(refname.getNamespaceURI());

        final String soap_uri = idname.getNamespaceURI();
        assert soap_uri != null;
        final String id_lname = idname.getLocalPart();
        final String ref_lname = refname.getLocalPart();

        final XMLStreamReaderExt reader = baseReader;

        assert reader.isStartElement() :
            " illegal state: " + XmlStreamUtils.printEvent(reader);

        soapAttributeHolder.clear();


        final int att_cnt = reader.getAttributeCount();
        for (int att_idx = 0; att_idx < att_cnt; att_idx++) {
            final String uri = reader.getAttributeNamespace(att_idx);
            if (!soap_uri.equals(uri == null ? "" : uri))
                continue;

            try {
                final String lname = reader.getAttributeLocalName(att_idx);
                if (id_lname.equals(lname)) {
                    String attval =
                        reader.getAttributeStringValue(att_idx,
                                                       XmlWhitespace.WS_COLLAPSE);
                    soapAttributeHolder.id = getIdFromAttributeValue(attval);
                    return; //no point in looking at the rest of the attributes
                } else if (ref_lname.equals(lname)) {
                    String attval =
                        reader.getAttributeStringValue(att_idx,
                                                       XmlWhitespace.WS_COLLAPSE);
                    soapAttributeHolder.ref =
                        getReferencedIdFromAttributeValue(attval);
                    return; //no point in looking at the rest of the attributes
                }
            }

                //nothing should have been assigned, so keep going
            catch (InvalidLexicalValueException ilve) {
                addError(ilve.getMessage(), ilve.getLocation());
            }
            catch (XMLStreamException e) {
                throw new XmlException(e);
            }
        }

    }


    /**
     * extract ref'd id from attribute value.
     *
     * E.g. for soap 1.1, given "#ID_10", will return "ID_10"
     *
     * @param attval
     * @return
     * @throws InvalidLexicalValueException  for bad ref
     */
    protected abstract String getReferencedIdFromAttributeValue(String attval);

    protected abstract String getIdFromAttributeValue(String attval);

    protected abstract QName getIdAttributeName();

    protected abstract QName getRefAttributeName();

    private Object unmarshalElementProperty(final RuntimeBindingProperty prop,
                                            final Object inter)
        throws XmlException
    {
        final RuntimeBindingType actual_rtt =
            this.determineActualRuntimeType(prop.getRuntimeBindingType());
        {
            final String lexical_default = prop.getLexicalDefault();
            if (lexical_default != null) {
                setNextElementDefault(lexical_default);
            }
        }

        final String id = soapAttributeHolder.id;

        final Object this_val;

        if (!hasXsiNil() && actual_rtt.hasElementChildren()) {
            final Object prop_inter = prop.createIntermediary(inter, actual_rtt, this);
            this_val = umarshalComplexElementWithId(actual_rtt, prop_inter, id);
        } else {
            this_val = unmarshalSimpleElementWithId(actual_rtt, id);
        }
        return this_val;
    }

    //simple means xsi:nil == true or no element children
    private Object unmarshalSimpleElementWithId(final RuntimeBindingType actual_rtt,
                                                final String id)
        throws XmlException
    {
        final TypeUnmarshaller um = getUnmarshaller(actual_rtt);
        final Object this_val = um.unmarshal(this);
        if (id != null) {
            refObjectTable.putForRef(id, this_val, this_val);
        }
        return this_val;
    }

    //by "complex" I mean can have element children
    private Object umarshalComplexElementWithId(final RuntimeBindingType actual_rtt,
                                                final Object prop_inter,
                                                final String id)
        throws XmlException
    {
        final Object this_val;
        final boolean update_again = updateRefTable(actual_rtt, prop_inter, id);
        actual_rtt.getUnmarshaller().unmarshalIntoIntermediary(prop_inter, this);
        this_val = actual_rtt.getFinalObjectFromIntermediary(prop_inter, this);
        interToFinalMap().put(prop_inter, this_val);
        if (update_again) {
            refObjectTable.putObjectForRef(id, this_val);
        }
        return this_val;
    }

    private IdentityHashMap interToFinalMap()
    {
        if (interToFinalMap == null) {
            interToFinalMap = new IdentityHashMap();
        }
        return interToFinalMap;
    }

    private boolean updateRefTable(RuntimeBindingType actual_rtt,
                                   Object prop_inter,
                                   String id)
    {
        boolean update_again = false;
        if (id != null) {
            if (actual_rtt.isObjectFromIntermediateIdempotent()) {
                refObjectTable.putForRef(id,
                                         prop_inter,
                                         actual_rtt.getObjectFromIntermediate(prop_inter));
            } else {
                refObjectTable.putIntermediateForRef(id, prop_inter);
                update_again = true;
            }
        }
        return update_again;
    }


    private static final class SoapAttributeHolder
    {
        String id;
        String ref;

        void clear()
        {
            id = null;
            ref = null;
        }
    }


    private static final class FillEvent
    {
        final String ref;
        final RuntimeBindingProperty prop;
        final int index;
        final Object inter;

        public FillEvent(String ref, RuntimeBindingProperty prop, int index, Object inter)
        {
            this.ref = ref;
            this.prop = prop;
            this.index = index;
            this.inter = inter;
        }
    }


}
