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

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlRuntimeException;
import org.apache.xmlbeans.impl.common.InvalidLexicalValueException;
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
                    //TODO: apply collapse whitespace facet somehow
                    holder.schemaLocation =
                        reader.getAttributeStringValue(att_idx);
                } else if (XSI_NO_NS_SCHEMA_LOCATION_ATTR.equals(lname)) {
                    //TODO: apply collapse whitespace facet somehow
                    holder.noNamespaceSchemaLocation =
                        reader.getAttributeStringValue(att_idx);
                }
            }
                //nothing should have been assigned, so keep going
                //TODO: use real location (maybe just pass context to this method).
            catch (InvalidLexicalValueException ilve) {
                addError(errors, ilve.getMessage(),
                         reader.getLocation(), "<unknown>");
            }
            catch (IllegalArgumentException iax) {   //thrown by lexBoolean
                addError(errors, iax.getMessage(),
                         reader.getLocation(), "<unknown>");
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
                    //TODO: what about mixed content models
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


    private static boolean isXsiNilTrue(XMLStreamReader reader,
                                        int att_idx,
                                        Collection errors)
    {
        final String lname = reader.getAttributeLocalName(att_idx);
        if (!XSI_NIL_ATTR.equals(lname))
            return false;

        if (!XSI_NS.equals(reader.getAttributeNamespace(att_idx)))
            return false;

        final String att_val = reader.getAttributeValue(att_idx);
        return XsTypeConverter.lexBoolean(att_val, errors);
    }

    public static boolean isXsiNilTrue(XMLStreamReader reader,
                                       Collection errors)
    {
        assert reader.isStartElement();
        for (int i = 0, len = reader.getAttributeCount(); i < len; i++) {
            if (isXsiNilTrue(reader, i, errors)) return true;
        }
        return false;
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
