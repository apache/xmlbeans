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

import org.apache.xmlbeans.XmlException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Basic XmlStreamReader based impl that can handle converting
 * simple types of the form <a>4.54</a>.
 */
class AtomicSimpleTypeConverter
    implements TypeConverter
{
    private final AtomicLexerPrinter lexerPrinter;

    AtomicSimpleTypeConverter(AtomicLexerPrinter lexerPrinter)
    {
        this.lexerPrinter = lexerPrinter;
    }

    public Object unmarshall(UnmarshallContext context)
    {
        final XMLStreamReader reader = context.getXmlStream();

        final CharSequence content;
        try {
            reader.next(); //move past start element
            content = getContent(context);
        }
        catch (XmlException ex) {
            //TODO: better error handling
            throw new AssertionError(ex);
        }
        catch (XMLStreamException xse) {
            //TODO: better error handling
            throw new AssertionError(xse);
        }

        assert (content != null);

        //TODO: better error handling
        Collection errors = new ArrayList();

        return lexerPrinter.lex(content, errors);
    }

    private CharSequence getContent(UnmarshallContext context)
        throws XmlException, XMLStreamException
    {
        final XMLStreamReader rdr = context.getXmlStream();
        String content = null;
        StringBuffer buf = null;

        FOL:
        for (int state = rdr.getEventType(); ; state = rdr.next()) {
            switch (state) {
                case XMLStreamReader.END_DOCUMENT:
                    throw new XmlException("unexpected end of XML");
                case XMLStreamReader.END_ELEMENT:
                    if (content == null) {
                        content = "";
                    }
                    rdr.next(); // eat the matching end elem
                    break FOL;
                case XMLStreamReader.START_ELEMENT:
                    //TODO: better error handling
                    throw new XmlException("unexpected start element");
                    //UnmarshallUtils.skipElement(rdr);
                    //break;
                case XMLStreamReader.CHARACTERS:
                    if (content == null) {
                        content = rdr.getText();
                    } else {
                        if (buf == null) {
                            buf = new StringBuffer(content);
                        }
                        buf.append(rdr.getText());
                    }
                    break;
                case XMLStreamReader.PROCESSING_INSTRUCTION:
                case XMLStreamReader.COMMENT:
                    break;
                default:
                    throw new AssertionError("unexpected xml state " + state);
            }

            if (!rdr.hasNext()) {
                throw new XmlException("unexpected end of xml stream");
            }

        }

        if (buf == null) {
            assert (content != null) ;
            return content;
        } else {
            return buf.toString();
        }

    }


}
