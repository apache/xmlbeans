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

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlRuntimeException;
import org.apache.xmlbeans.impl.common.InvalidLexicalValueException;
import org.apache.xmlbeans.impl.common.XmlWhitespace;
import org.apache.xmlbeans.impl.richParser.XMLStreamReaderExt;
import org.apache.xmlbeans.impl.util.XsTypeConverter;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

final class MarshalStreamUtils
{
    static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";
    static final String XSI_TYPE_ATTR = "type";
    static final String XSI_NIL_ATTR = "nil";
    static final String XSI_SCHEMA_LOCATION_ATTR = "schemaLocation";
    static final String XSI_NO_NS_SCHEMA_LOCATION_ATTR =
        "noNamespaceSchemaLocation";

    static final QName XSI_NIL_QNAME = new QName(XSI_NS, XSI_NIL_ATTR);
    static final QName XSI_TYPE_QNAME = new QName(XSI_NS, XSI_TYPE_ATTR);


    static void getXsiAttributes(XsiAttributeHolder holder,
                                 XMLStreamReaderExt reader,
                                 Collection errors)
        throws XMLStreamException
    {
        assert reader.isStartElement();

        holder.reset();

        final int att_cnt = reader.getAttributeCount();
        for (int att_idx = 0; att_idx < att_cnt; att_idx++) {
            if (!XSI_NS.equals(reader.getAttributeNamespace(att_idx)))
                continue;

            try {
                final String lname = reader.getAttributeLocalName(att_idx);
                if (XSI_TYPE_ATTR.equals(lname)) {
                    holder.xsiType = reader.getAttributeQNameValue(att_idx);
                } else if (XSI_NIL_ATTR.equals(lname)) {
                    holder.hasXsiNil = reader.getAttributeBooleanValue(att_idx);
                } else if (XSI_SCHEMA_LOCATION_ATTR.equals(lname)) {
                    holder.schemaLocation =
                        reader.getAttributeStringValue(att_idx,
                                                       XmlWhitespace.WS_COLLAPSE);
                } else if (XSI_NO_NS_SCHEMA_LOCATION_ATTR.equals(lname)) {
                    holder.noNamespaceSchemaLocation =
                        reader.getAttributeStringValue(att_idx,
                                                       XmlWhitespace.WS_COLLAPSE);
                }
            }
                //nothing should have been assigned, so keep going
                //TODO: use real location (maybe just pass context to this method).
            catch (InvalidLexicalValueException ilve) {
                addError(errors, ilve.getMessage(),
                         ilve.getLocation(), "<unknown>");
            }
        }
    }

    static QName getXsiType(XMLStreamReader reader, Collection errors)
    {
        assert reader.isStartElement();

        final int att_cnt = reader.getAttributeCount();
        for (int att_idx = 0; att_idx < att_cnt; att_idx++) {
            if (!XSI_NS.equals(reader.getAttributeNamespace(att_idx)))
                continue;

            final String lname = reader.getAttributeLocalName(att_idx);
            if (XSI_TYPE_ATTR.equals(lname)) {
                final String type_str = reader.getAttributeValue(att_idx);
                return XsTypeConverter.lexQName(type_str, errors,
                                                reader.getNamespaceContext());
            }
        }

        return null;
    }

    /**
     * go to next start element.  if reader is sitting on a start element
     * then no advancing will be done.  returns false if we hit an end element,
     * or the end of the steam, otherwise returns true
     *
     * @param reader
     * @return
     */
    static boolean advanceToNextStartElement(XMLStreamReader reader)
        throws XMLStreamException
    {
        for (int state = reader.getEventType();
             reader.hasNext();
             state = reader.next()) {
            switch (state) {
                case XMLStreamReader.START_ELEMENT:
                    return true;
                case XMLStreamReader.END_ELEMENT:
                    return false;
                case XMLStreamReader.END_DOCUMENT:
                    throw new XmlRuntimeException("unexpected end of XML");

                case XMLStreamReader.PROCESSING_INSTRUCTION:
                case XMLStreamReader.CHARACTERS:
                case XMLStreamReader.COMMENT:
                case XMLStreamReader.SPACE:
                case XMLStreamReader.ENTITY_REFERENCE:
                case XMLStreamReader.DTD:
                case XMLStreamReader.NOTATION_DECLARATION:
                case XMLStreamReader.ENTITY_DECLARATION:
                    break;
                default:
                    throw new AssertionError("unexpected xml state " + state);
            }
        }


        //end of the steam
        return false;
    }

    /**
     * Skip current element node and all its contents.
     * Reader must be on start element.
     * Skips just past the matching end element.
     * We are just counting start/end -- the parser is
     * dealing with well-formedness.
     *
     * @param reader
     */
    static void skipElement(XMLStreamReader reader)
        throws XMLStreamException
    {
        assert reader.isStartElement();

        int cnt = -1;

        //TODO: seem to be rechecking assertion, why not skip one ahead...


        for (int state = reader.getEventType(); reader.hasNext();
             state = reader.next()) {
            switch (state) {
                case XMLStreamReader.START_ELEMENT:
                    cnt++;
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (cnt == 0) {
                        assert reader.hasNext();
                        reader.next();
                        return;
                    } else {
                        cnt--;
                    }
                    break;
                case XMLStreamReader.END_DOCUMENT:
                    //should not happen for well-formed xml
                    throw new XmlRuntimeException("unexpected end of xml document");
                default:
                    break;
            }
        }


        //should not happen for well-formed xml
        throw new XmlRuntimeException("unexpected end of xml stream");
    }


    static void advanceToFirstItemOfInterest(XMLStreamReader rdr)
        throws XMLStreamException
    {
        for (int state = rdr.getEventType(); rdr.hasNext(); state = rdr.next()) {
            switch (state) {
                case XMLStreamReader.START_ELEMENT:
                    return;
                case XMLStreamReader.END_ELEMENT:
                    throw new XmlRuntimeException("unexpected end of XML");

                case XMLStreamReader.PROCESSING_INSTRUCTION:
                    break;
                case XMLStreamReader.CHARACTERS:
                    if (rdr.isWhiteSpace()) break;
                    throw new AssertionError("NAKED CHARDATA UNIMPLEMENTED");
                case XMLStreamReader.COMMENT:
                case XMLStreamReader.SPACE:
                case XMLStreamReader.START_DOCUMENT:
                    break;
                case XMLStreamReader.END_DOCUMENT:
                    throw new XmlRuntimeException("unexpected end of XML");

                case XMLStreamReader.ENTITY_REFERENCE:
                    break;

                case XMLStreamReader.ATTRIBUTE:
                    throw new AssertionError("NAKED ATTRIBUTE UNIMPLEMENTED");

                case XMLStreamReader.DTD:
                case XMLStreamReader.CDATA:
                case XMLStreamReader.NAMESPACE:
                case XMLStreamReader.NOTATION_DECLARATION:
                case XMLStreamReader.ENTITY_DECLARATION:
                    break;

                default:
                    throw new XmlRuntimeException("unexpected xml state:" + state +
                                                  "in" + rdr);
            }
        }
        throw new XmlRuntimeException("unexpected end of xml stream");
    }

    static void addError(Collection errors,
                         String msg,
                         Location location,
                         String sourceName)
    {
        final XmlError err;
        if (location != null) {
            err = XmlError.forLocation(msg,
                                       sourceName,
                                       location.getLineNumber(),
                                       location.getColumnNumber(),
                                       location.getCharacterOffset());
        } else {
            err = XmlError.forSource(msg, sourceName);
        }
        errors.add(err);
    }

    static Object inputStreamToBytes(final InputStream val)
        throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int b;

        while ((b = val.read()) != -1) {
            baos.write(b);
        }

        return baos.toByteArray();
    }


}
