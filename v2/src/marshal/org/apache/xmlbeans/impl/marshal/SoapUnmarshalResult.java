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
import org.apache.xmlbeans.impl.common.XmlWhitespace;
import org.apache.xmlbeans.impl.richParser.XMLStreamReaderExt;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

abstract class SoapUnmarshalResult
    extends UnmarshalResult
{
    private final SoapAttributeHolder soapAttributeHolder =
        new SoapAttributeHolder();
    private final RefObjectTable refObjectTable;

    SoapUnmarshalResult(BindingLoader loader,
                        RuntimeBindingTypeTable typeTable,
                        RefObjectTable refObjectTable,
                        XmlOptions options)
    {
        super(loader, typeTable, options);
        this.refObjectTable = refObjectTable;
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
        if (ref != null) {
            baseReader = relocateStreamToRef(ref);
            updateSoapAttributes();
            updateAttributeState();
        }

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

        if (baseReader != curr_reader) {
            baseReader = curr_reader;
            updateAttributeState(); //TODO: do we really need this here?
        }
    }

    private XMLStreamReaderExt relocateStreamToRef(String ref)
    {
        throw new AssertionError("UNIMP");

//        XMLStreamReaderExtImpl impl = (XMLStreamReaderExtImpl)baseReader;
//        final XMLStreamReader underlyingXmlStream = impl.getUnderlyingXmlStream();
//        final Node node = Public2.getNode(underlyingXmlStream);
//        final XmlCursor cursor = Public2.getCursor(node);
//        final XMLStreamReader reader = cursor.newXMLStreamReader();
//        return new XMLStreamReaderExtImpl(reader);
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

        assert reader.isStartElement();

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
        if (prop.hasFactory()) {
            this_val = prop.createObjectViaFactory(inter, actual_rtt);
            addIdToTable(this_val);
            actual_rtt.getUnmarshaller().unmarshal(this_val, this);
        } else if (actual_rtt.hasElementChildren()) {
            final Object intermediary = actual_rtt.createIntermediary(this);
            this_val = actual_rtt.getObjectFromIntermediate(intermediary);
            addIdToTable(this_val);
            actual_rtt.getUnmarshaller().unmarshalIntoIntermediary(intermediary, this);
        } else {
            TypeUnmarshaller um = getUnmarshaller(actual_rtt);
            this_val = um.unmarshal(this);
        }
        return this_val;
    }

    private void addIdToTable(final Object this_val)
    {
        final String id = soapAttributeHolder.id;
        if (id != null)
            refObjectTable.putObjectForRef(id, this_val);
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
