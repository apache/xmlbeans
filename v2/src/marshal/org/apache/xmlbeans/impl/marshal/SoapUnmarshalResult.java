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
import org.apache.xmlbeans.impl.common.InvalidLexicalValueException;
import org.apache.xmlbeans.impl.common.XmlStreamUtils;
import org.apache.xmlbeans.impl.common.XmlWhitespace;
import org.apache.xmlbeans.impl.richParser.XMLStreamReaderExt;
import org.apache.xmlbeans.impl.richParser.XMLStreamReaderExtImpl;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

abstract class SoapUnmarshalResult
    extends UnmarshalResult
{
    private final SoapAttributeHolder soapAttributeHolder =
        new SoapAttributeHolder();
    private final RefObjectTable refObjectTable;
    private final StreamRefNavigator streamRefNavigator;

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

    void extractAndFillElementProp(final RuntimeBindingProperty prop,
                                   Object inter)
        throws XmlException
    {
        assert baseReader != null;

        final XMLStreamReaderExt curr_reader = baseReader;

        updateSoapAttributes();

        final String ref = soapAttributeHolder.ref;
        Object tmpval = null;
        if (ref != null) {
            tmpval = getObjectForRefFromTable(ref);
            if (tmpval == null) {
                Object tmp_inter = getInterForRefFromTable(ref);
                if (tmp_inter == null) {
                    baseReader = relocateStreamToRef(ref);
                    updateSoapAttributes();
                    updateAttributeState();
                } else {
                    throw new AssertionError("FIXME");
                }
            } else {
                prop.fill(inter, tmpval);
                skipElement();
            }
        }

        if (tmpval == null) {
            try {
                final RuntimeBindingType actual_rtt =
                    this.determineActualRuntimeType(prop.getRuntimeBindingType());

                final Object this_val =
                    unmarshalElementProperty(prop, inter, actual_rtt);

                prop.fill(inter, this_val);
            }
            catch (InvalidLexicalValueException ilve) {
                //unlike attributes, the error has been added to the this
                //already via BaseSimpleTypeConveter...
            }
        }

        if (baseReader != curr_reader) {
            baseReader = curr_reader;
            skipElement();
        }
    }

    private Object getObjectForRefFromTable(String ref)
    {
        //TODO: mask null values

        final Object obj = refObjectTable.getObjectForRef(ref);
        return obj;
    }

    private Object getInterForRefFromTable(String ref)
    {
        //TODO: mask null values

        final Object obj = refObjectTable.getInterForRef(ref);
        return obj;
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

        //for perf, we assume that the id and ref attirbutes are
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

    private Object unmarshalElementProperty(RuntimeBindingProperty prop,
                                            Object inter,
                                            RuntimeBindingType actual_rtt)
        throws XmlException
    {
        {
            final String lexical_default = prop.getLexicalDefault();
            if (lexical_default != null) {
                setNextElementDefault(lexical_default);
            }
        }

        final Object this_val;


        if (actual_rtt.hasElementChildren()) {
            final Object prop_inter = prop.createIntermediary(inter, actual_rtt, this);

            final String id = soapAttributeHolder.id;
            final boolean update_again = updateRefTable(actual_rtt, prop_inter, id);

            actual_rtt.getUnmarshaller().unmarshalIntoIntermediary(prop_inter, this);
            this_val = actual_rtt.getFinalObjectFromIntermediary(prop_inter, this);
            if (update_again) {
                refObjectTable.putObjectForRef(id, this_val);
            }
        } else {
            TypeUnmarshaller um = getUnmarshaller(actual_rtt);
            this_val = um.unmarshal(this);
        }
        return this_val;
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

}
